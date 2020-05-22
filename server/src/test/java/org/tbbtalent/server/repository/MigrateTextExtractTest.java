package org.tbbtalent.server.repository;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.service.aws.S3ResourceHelper;

import javax.transaction.Transactional;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MigrateTextExtractTest {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateAttachmentRepository candidateAttachmentRepository;

    @Autowired
    private S3ResourceHelper s3ResourceHelper;

    @Transactional
    @Test
    void testCandidateRepository() {
        Candidate candidate = candidateRepository.findById((long) 20702).orElse(null);
        assertNotNull(candidate);
        assertEquals("942", candidate.getCandidateNumber());
        candidate.setWhatsapp("00000000000");
        assertEquals("00000000000", candidate.getWhatsapp());
    }

    @Transactional
    @Test
    void extractTextFromMigratedFiles() throws IOException {
        List<CandidateAttachment> candidatePdfs = candidateAttachmentRepository.findByFileType("pdf");
        assertNotNull(candidatePdfs);
        assertEquals(2341, candidatePdfs.size());

        Set<CandidateAttachment> candidateAttachmentSet = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            CandidateAttachment candidateAttachment;
            candidateAttachment = candidatePdfs.get(i);
            candidateAttachmentSet.add(candidateAttachment);
        }
        assertEquals(10, candidateAttachmentSet.size());

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
            assertNotNull(pdf.getTextExtract());
        }
//        assertEquals("942", candidatePdfs());
//        candidate.setWhatsapp("00000000000");
//        assertEquals("00000000000", candidate.getWhatsapp());
    }
}
