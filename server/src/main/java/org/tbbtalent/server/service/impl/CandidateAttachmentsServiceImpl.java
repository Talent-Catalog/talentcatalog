package org.tbbtalent.server.service.impl;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.AttachmentType;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.repository.CandidateAttachmentRepository;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.request.PagedSearchRequest;
import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tbbtalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tbbtalent.server.request.attachment.UpdateCandidateAttachmentRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateAttachmentService;
import org.tbbtalent.server.service.aws.S3ResourceHelper;
import org.tbbtalent.server.util.textExtract.TextExtractHelper;

import java.io.*;

@Service
public class CandidateAttachmentsServiceImpl implements CandidateAttachmentService {

    private static final Logger log = LoggerFactory.getLogger(CandidateAttachmentsServiceImpl.class);

    private final CandidateRepository candidateRepository;
    private final CandidateAttachmentRepository candidateAttachmentRepository;
    private final UserContext userContext;
    private final S3ResourceHelper s3ResourceHelper;
    private final TextExtractHelper textExtractHelper;

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
        this.textExtractHelper = new TextExtractHelper(candidateAttachmentRepository, s3ResourceHelper);
    }

    @Override
    public Page<CandidateAttachment> searchCandidateAttachments(SearchCandidateAttachmentsRequest request) {
        return candidateAttachmentRepository.findByCandidateId(request.getCandidateId(), request.getPageRequest());
    }

    @Override
    public Page<CandidateAttachment> searchCandidateAttachmentsForLoggedInCandidate(PagedSearchRequest request) {
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
            if(request.getCv()) {
                try {
                    textExtract = textExtractHelper.getTextExtractFromFile(srcFile, request.getFileType());
                    if(StringUtils.isNotBlank(textExtract)) {
                        attachment.setTextExtract(textExtract);
                        candidateAttachmentRepository.save(attachment);
                    }
                } catch (Exception e) {
                    log.error("Could not extract text from uploaded cv file", e);
                    attachment.setTextExtract(null);
                }
                attachment.setCv(request.getCv());
            }

            // The location is set to the filename because we can derive it's location from the candidate number
            attachment.setLocation(uniqueFilename);
            attachment.setName(request.getName());
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
    public CandidateAttachment updateCandidateAttachment(UpdateCandidateAttachmentRequest request) {
        User user = userContext.getLoggedInUser();

        CandidateAttachment candidateAttachment = candidateAttachmentRepository.findByIdLoadCandidate(request.getId())
                .orElseThrow(() -> new NoSuchObjectException(CandidateAttachment.class, request.getId()));

        // Update the name
        candidateAttachment.setName(request.getName());

        // Run text extraction if attachment changed from not CV to a CV or remove if changed from CV to not CV.
        if(request.getCv() && !candidateAttachment.isCv()) {
            try {
                String uniqueFilename = candidateAttachment.getLocation();
                String destination = "candidate/" + candidateAttachment.getCandidate().getCandidateNumber() + "/" + uniqueFilename;
                File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), destination);
                String extractedText = textExtractHelper.getTextExtractFromFile(srcFile, candidateAttachment.getFileType());
                if (StringUtils.isNotBlank(extractedText)) {
                    candidateAttachment.setTextExtract(extractedText);
                    candidateAttachmentRepository.save(candidateAttachment);
                }
            } catch (Exception e) {
                log.error("Unable to extract text from file " + candidateAttachment.getLocation(), e.getMessage());
                candidateAttachment.setTextExtract(null);
            }
        } else if (!request.getCv() && candidateAttachment.isCv()){
            candidateAttachment.setTextExtract(null);
            candidateAttachmentRepository.save(candidateAttachment);
        }

        // Update the fields related to the file type
        if (candidateAttachment.getType().equals(AttachmentType.link)) {
            candidateAttachment.setLocation(request.getLocation());
            candidateAttachment.setAuditFields(user);
        } else if (candidateAttachment.getType().equals(AttachmentType.file)){
            candidateAttachment.setCv(request.getCv());
            candidateAttachment.setAuditFields(user);
        }

        // Update the candidate audit fields
        Candidate candidate = candidateAttachment.getCandidate();
        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);

        return candidateAttachmentRepository.save(candidateAttachment);
    }

}
