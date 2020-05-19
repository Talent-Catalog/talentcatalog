package org.tbbtalent.server.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.repository.CandidateAttachmentRepository;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.request.SearchRequest;
import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tbbtalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tbbtalent.server.request.attachment.UpdateCandidateAttachmentRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateAttachmentService;
import org.tbbtalent.server.service.aws.S3ResourceHelper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class CandidateAttachmentsServiceImpl implements CandidateAttachmentService {

    private static final Logger log = LoggerFactory.getLogger(CandidateAttachmentsServiceImpl.class);

    private final CandidateRepository candidateRepository;
    private final CandidateAttachmentRepository candidateAttachmentRepository;
    private final UserContext userContext;
    private final S3ResourceHelper s3ResourceHelper;

    @Value("{aws.s3.bucketName}")
    String s3Bucket;

    @Autowired
    public CandidateAttachmentsServiceImpl(CandidateRepository candidateRepository,
                                           CandidateAttachmentRepository candidateAttachmentRepository,
                                           S3ResourceHelper s3ResourceHelper,
                                           UserContext userContext) {
        this.candidateRepository = candidateRepository;
        this.candidateAttachmentRepository = candidateAttachmentRepository;
        this.s3ResourceHelper = s3ResourceHelper;
        this.userContext = userContext;
    }

    @Override
    public Page<CandidateAttachment> searchCandidateAttachments(SearchCandidateAttachmentsRequest request) {
        return candidateAttachmentRepository.findByCandidateId(request.getCandidateId(), request.getPageRequest());
    }

    @Override
    public Page<CandidateAttachment> searchCandidateAttachmentsForLoggedInCandidate(SearchRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        return candidateAttachmentRepository.findByCandidateId(candidate.getId(), request.getPageRequest());
    }

    @Override
    public List<CandidateAttachment> listCandidateAttachmentsForLoggedInCandidate() {
        Candidate candidate = userContext.getLoggedInCandidate();
        return candidateAttachmentRepository.findByCandidateIdLoadAudit(candidate.getId());
    }

    @Override
    public CandidateAttachment createCandidateAttachment(CreateCandidateAttachmentRequest request, Boolean adminOnly) {
        User user = userContext.getLoggedInUser();
        Candidate candidate;
        String textExtract;

        // Handle requests coming from the admin portal
        if (request.getCandidateId() != null) {
            candidate = candidateRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));
        } else {
            candidate = userContext.getLoggedInCandidate();
        }

        // Create a record of the attachment
        CandidateAttachment attachment = new CandidateAttachment();

        if (request.getType().equals(AttachmentType.link)) {

            attachment.setType(AttachmentType.link);
            attachment.setLocation(request.getLocation());
            attachment.setName(request.getName());

        } else if (request.getType().equals(AttachmentType.file)) {
            // Prepend the filename with a UUID to ensure an existing file doesn't get overwritten on S3
            String uniqueFilename = UUID.randomUUID() + "_" + request.getName();
            // Source is temporary folder where the file got uploaded, destination is the candidates unique folder
            String source = "temp/" + request.getFolder() + "/" + request.getName();
            String destination = "candidate/" + candidate.getCandidateNumber() + "/" + uniqueFilename;
            // Download the file from the S3 temporary file before it is copying over
            File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), source);
            // Copy the file from temp folder in s3 into the candidate's folder on S3
            this.s3ResourceHelper.copyObject(source, destination);

            log.info("[S3] Transferred candidate attachment from source [" + source + "] to destination [" + destination + "]");

            // Extract text from the file
            try {
                textExtract = getTextFromFile(request.getFileType(), srcFile);
                attachment.setTextExtract(textExtract);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new InvalidRequestException("Please check valid file type is uploaded.");
            }

            // The location is set to the filename because we can derive it's location from the candidate number
            attachment.setLocation(uniqueFilename);
            attachment.setName(uniqueFilename);
            attachment.setType(AttachmentType.file);
            attachment.setFileType(request.getFileType());
        }

        attachment.setCandidate(candidate);
        attachment.setMigrated(false);
        attachment.setAdminOnly(adminOnly);
        attachment.setAuditFields(user);

        // Update candidate audit fields
        candidate.setAuditFields(user);
        candidateRepository.save(candidate);

        return candidateAttachmentRepository.save(attachment);
    }

    @Override
    @Transactional
    public void deleteCandidateAttachment(Long id) {
        User user = userContext.getLoggedInUser();

        CandidateAttachment candidateAttachment = candidateAttachmentRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateAttachment.class, id));

        Candidate candidate;

        if (!user.getRole().equals(Role.admin)) {
             candidate = userContext.getLoggedInCandidate();
            // Check that the user is deleting their own attachment
            if (!candidate.getId().equals(candidateAttachment.getCandidate().getId())) {
                throw new InvalidCredentialsException("You do not have permission to perform that action");
            }
        } else {
            candidate = candidateRepository.findById(candidateAttachment.getCandidate().getId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateAttachment.getCandidate().getId()));
        }

        // Delete the record from the database
        candidateAttachmentRepository.delete(candidateAttachment);

        // Delete the object on S3
        String folder = BooleanUtils.isTrue(candidateAttachment.isMigrated()) ? "migrated" : candidate.getCandidateNumber();
        s3ResourceHelper.deleteFile("candidate/" + folder + "/" + candidateAttachment.getName());

        // Update the candidate audit fields
        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);
    }

    @Override
    @Transactional
    public CandidateAttachment updateCandidateAttachment(UpdateCandidateAttachmentRequest request) {
        User user = userContext.getLoggedInUser();

        CandidateAttachment candidateAttachment = candidateAttachmentRepository.findByIdLoadCandidate(request.getId())
                .orElseThrow(() -> new NoSuchObjectException(CandidateAttachment.class, request.getId()));

        candidateAttachment.setName(request.getName());
        if (candidateAttachment.getType().equals(AttachmentType.link)) {
            candidateAttachment.setLocation(request.getLocation());
            candidateAttachment.setAuditFields(user);
        }

        // Update the candidate audit fields
        Candidate candidate = candidateAttachment.getCandidate();
        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);

        return candidateAttachmentRepository.save(candidateAttachment);
    }

    public String getTextFromPDFFile(File srcFile) throws IOException {
        String source = srcFile.getAbsolutePath();
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(source));

        String str;
        StringBuffer txt = new StringBuffer();

        for (int i=1; i<= pdfDoc.getNumberOfPages(); i++){
            str = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), new LocationTextExtractionStrategy());
            txt.append(str);
        }

        pdfDoc.close();

        return String.valueOf(txt);
    }

    public String getTextFromWordFile(File srcFile) throws IOException {
        FileInputStream fis = new FileInputStream(srcFile);
        XWPFDocument doc = new XWPFDocument(fis);
        XWPFWordExtractor xwe = new XWPFWordExtractor(doc);
        String txt = xwe.getText();
        xwe.close();
        return txt;
    }

    public String getTextFromTxtFile(File srcFile) throws IOException {
        String txt = new String(Files.readAllBytes(Paths.get(srcFile.getPath())));
        System.out.println(txt);
        return txt;
    }

    public String getTextFromFile(String fileType, File srcFile) throws IOException {
        String textExtract = "";
        if(fileType.equals("pdf")){
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(srcFile.getAbsolutePath()));
            String str;
            StringBuffer txt = new StringBuffer();
            for (int i=1; i<= pdfDoc.getNumberOfPages(); i++){
                str = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), new LocationTextExtractionStrategy());
                txt.append(str);
            }
            textExtract = String.valueOf(txt);
            pdfDoc.close();
        }else if (fileType.equals("doc") || fileType.equals("docx")){
            FileInputStream fis = new FileInputStream(srcFile);
            XWPFDocument doc = new XWPFDocument(fis);
            XWPFWordExtractor xwe = new XWPFWordExtractor(doc);
            textExtract = xwe.getText();
            xwe.close();
        }else if (fileType.equals("txt")){
            textExtract = new String(Files.readAllBytes(Paths.get(srcFile.getPath())));
        }
        return textExtract;
    }

    private static String getFileExtension(String fileName) {
        // Checks that a . exists and that it isn't at the start of the filename (indication there is no file name just a file type e.g. ".pdf"
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

}
