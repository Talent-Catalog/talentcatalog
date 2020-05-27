package org.tbbtalent.server.repository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.service.aws.S3ResourceHelper;
import org.tbbtalent.server.util.textExtract.TextExtractHelper;

import javax.transaction.Transactional;
import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MigrateTextExtractTest {

    @Autowired
    private CandidateAttachmentRepository candidateAttachmentRepository;

    @Autowired
    private S3ResourceHelper s3ResourceHelper;

    @Transactional
    @Test
    void testTextExtractHelper() throws IOException {
        TextExtractHelper textExtractHelper = new TextExtractHelper(candidateAttachmentRepository, s3ResourceHelper);
        assertNotNull(textExtractHelper);

        // Test CV file
        File pdfFile = new File("src/test/resources/CV.pdf");
        assertNotNull(pdfFile);
        String pdfExtract = textExtractHelper.getTextExtractFromFile(pdfFile, "pdf");
        assertNotEquals("", pdfExtract);

        // Test CV file that can't be read
        File pdfFileFail = new File("src/test/resources/migrationTestFileNoText.pdf");
        assertNotNull(pdfFileFail);
        String pdfExtractFail = textExtractHelper.getTextExtractFromFile(pdfFileFail, "pdf");
        assertEquals("", pdfExtractFail);

        // Test when wrong params and catch the exception
        try {
            String wrongFileType = textExtractHelper.getTextExtractFromFile(pdfFile, "docx");
            String noFile = textExtractHelper.getTextExtractFromFile(null ,"pdf");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Testing Docx files
        File docxFile = new File("src/test/resources/WordCV.docx");
        assertNotNull(docxFile);
        String docxExtract = textExtractHelper.getTextExtractFromFile(docxFile, "docx");
        assertNotEquals("", docxExtract);

        // Testing Doc files
        File docFile = new File("src/test/resources/docCV.doc");
        assertNotNull(docFile);
        String docExtract = textExtractHelper.getTextExtractFromFile(docFile, "doc");
        assertNotEquals("", docExtract);

        // Testing Txt files
        File txtFile = new File("src/test/pdf.txt");
        assertNotNull(txtFile);
        String txtExtract = textExtractHelper.getTextExtractFromFile(txtFile, "txt");
        assertNotEquals("", txtExtract);

    }

    @Transactional
    @Test
    void testRepoFindByFileTypes() {
        List<String> types = Arrays.asList("pdf", "docx", "doc", "txt");
        List<CandidateAttachment> files = candidateAttachmentRepository.findByFileTypes(types);
        assertEquals(2699, files.size());
    }

    @Transactional
    @Test
    void extractTextFromMigratedPdf() throws IOException {
        List<CandidateAttachment> candidatePdfs = candidateAttachmentRepository.findByFileType("pdf");
        assertNotNull(candidatePdfs);

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
            String pdfFileInText;
            if (!document.isEncrypted()) {
                pdfFileInText = tStripper.getText(document);
                System.out.println(1);
                pdf.setTextExtract(pdfFileInText.trim());
            }
            assertNotNull(pdf.getTextExtract());
        }
    }

    @Transactional
    @Test
    void extractTextFromMigratedDocx() throws IOException {
        List<CandidateAttachment> candidateDocs = candidateAttachmentRepository.findByFileType("docx");
        assertNotNull(candidateDocs);

        Set<CandidateAttachment> candidateAttachmentSet = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            CandidateAttachment candidateAttachment;
            candidateAttachment = candidateDocs.get(i);
            candidateAttachmentSet.add(candidateAttachment);
        }

        for(CandidateAttachment doc : candidateAttachmentSet) {
            String uniqueFilename = doc.getLocation();
            String destination = "candidate/migrated/" + uniqueFilename;
            File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), destination);

            FileInputStream fis = new FileInputStream(srcFile);
            XWPFDocument document = new XWPFDocument(fis);
            XWPFWordExtractor xwe = new XWPFWordExtractor(document);
            String theText = xwe.getText();
            doc.setTextExtract(theText);
            xwe.close();
        }

    }

    @Transactional
    @Test
    void extractTextFromMigratedDoc() {
        List<CandidateAttachment> candidateDocs = candidateAttachmentRepository.findByFileType("doc");
        assertNotNull(candidateDocs);

        Set<CandidateAttachment> candidateAttachmentSet = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            CandidateAttachment candidateAttachment;
            candidateAttachment = candidateDocs.get(i);
            candidateAttachmentSet.add(candidateAttachment);
        }

        for(CandidateAttachment doc : candidateAttachmentSet) {
            String uniqueFilename = doc.getLocation();
            String destination = "candidate/migrated/" + uniqueFilename;
            File srcFile;
            try{
                srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), destination);
                FileInputStream fis = new FileInputStream(srcFile);
                HWPFDocument document = new HWPFDocument(fis);
                WordExtractor we = new WordExtractor(document);
                String theText = we.getText();
                doc.setTextExtract(theText);
                we.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
