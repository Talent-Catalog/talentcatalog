package org.tbbtalent.server.util.textExtract;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.repository.CandidateAttachmentRepository;
import org.tbbtalent.server.service.aws.S3ResourceHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MigrateTextExtract {

    @Autowired
    private final CandidateAttachmentRepository candidateAttachmentRepository;

    @Autowired
    private final S3ResourceHelper s3ResourceHelper;

    public MigrateTextExtract(CandidateAttachmentRepository candidateAttachmentRepository,
                              S3ResourceHelper s3ResourceHelper) {
        this.candidateAttachmentRepository = candidateAttachmentRepository;
        this.s3ResourceHelper = s3ResourceHelper;
    }


    public String extractTextFromMigratedFiles() throws IOException {
        List<CandidateAttachment> candidatePdfs = candidateAttachmentRepository.findByFileType("pdf");

        Set<CandidateAttachment> candidateAttachmentSet = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            CandidateAttachment candidateAttachment;
            candidateAttachment = candidatePdfs.get(i);
            candidateAttachmentSet.add(candidateAttachment);
        }

        for(CandidateAttachment pdf : candidateAttachmentSet){
            String uniqueFilename = pdf.getLocation();
            String destination = "candidate/migrated/" + uniqueFilename;
            File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), destination);

            //SECOND WAY USING PDFBOX
            PDFTextStripper tStripper = new PDFTextStripper();
            tStripper.setSortByPosition(true);
            PDDocument document = PDDocument.load(srcFile);
            String pdfFileInText = "";
            if (!document.isEncrypted()) {
                pdfFileInText = tStripper.getText(document);
                System.out.println(1);
                pdf.setTextExtract(pdfFileInText.trim());
            }
        }
        return "done!";
    }


}
