/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.repository.db.CandidateAttachmentRepository;
import org.tctalent.server.service.db.aws.S3ResourceHelper;
import org.tctalent.server.util.textExtract.TextExtractHelper;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
@Slf4j
public class TextExtractHelperTest {

    @Autowired
    private CandidateAttachmentRepository candidateAttachmentRepository;

    @Autowired
    private S3ResourceHelper s3ResourceHelper;

    /**
     * Test Text Extract Helper on different file types
     * @throws IOException from PDFBox extraction methods
     */
    @Transactional
    @Test
    void testDifferentFilesTextExtractHelper() throws IOException {

        // Test pdf file
        File pdfFile = new File("src/test/resources/text/EnglishPdf.pdf");
        assertNotNull(pdfFile);
        String pdfExtract = TextExtractHelper.getTextExtractFromFile(pdfFile, "pdf");
        assertNotEquals("", pdfExtract);

        // Test pdf file that can't be read (scanned)
        File pdfFileFail = new File("src/test/resources/text/ScannedPdfNoTextExt.pdf");
        assertNotNull(pdfFileFail);
        String pdfExtractFail = TextExtractHelper.getTextExtractFromFile(pdfFileFail, "pdf");
        assertEquals("", pdfExtractFail);

        // Test when wrong params and catch the exception
        try {
            String wrongFileType = TextExtractHelper.getTextExtractFromFile(pdfFile, "docx");
        } catch (Exception e) {
            log.error(e.getMessage());
            assertNotNull(e);
        }

        try {
            String noFile = TextExtractHelper.getTextExtractFromFile(null ,"pdf");
        } catch (Exception e) {
            log.error(e.getMessage());
            assertNotNull(e);
        }

        // Testing Docx files
        File docxFile = new File("src/test/resources/text/EnglishDocx.docx");
        assertNotNull(docxFile);
        String docxExtract = TextExtractHelper.getTextExtractFromFile(docxFile, "docx");
        assertNotEquals("", docxExtract);

        // Testing Doc files
        File docFile = new File("src/test/resources/text/EnglishDoc.doc");
        assertNotNull(docFile);
        String docExtract = TextExtractHelper.getTextExtractFromFile(docFile, "doc");
        assertNotEquals("", docExtract);

        // Testing Txt files
        File txtFile = new File("src/test/resources/text/EnglishTxt.txt");
        assertNotNull(txtFile);
        String txtExtract = TextExtractHelper.getTextExtractFromFile(txtFile, "txt");
        assertNotEquals("", txtExtract);

    }

    /**
     * Test findByFileTypeAndMigrated query to get the file types that were left when the migration was done (newly added files that weren't in the migration S3 Bucket)
     */
    @Transactional
    @Test
    void testRepoFindByTextExtractAndMigrated() {
        List<String> types = Arrays.asList("pdf", "docx", "doc", "txt");
        List<CandidateAttachment> migratedFiles = candidateAttachmentRepository.findByFileTypesAndMigrated(types, true);
        assertNotNull(migratedFiles);
        List<CandidateAttachment> newFiles = candidateAttachmentRepository.findByFileTypesAndMigrated(types, false);
        assertNotNull(newFiles);
    }

    /**
     * Test text extraction on migrated files (in migrated s3 bucket) using Text Extract Helper method
     * @throws IOException
     */
    @Transactional
    @Test
    void testTextExtractMigratedFiles() {
        List<String> types = Arrays.asList("pdf", "docx", "doc", "txt");
        List<CandidateAttachment> files = candidateAttachmentRepository.findByFileTypesAndMigrated(types, true);
        assertNotNull(files);

        // Test with 10 from List
        Set<CandidateAttachment> candidateAttachmentSet = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            CandidateAttachment candidateAttachment = files.get(i);
            candidateAttachmentSet.add(candidateAttachment);
        }
        assertEquals(10, candidateAttachmentSet.size());

        // Use test set to loop through
        for(CandidateAttachment file : candidateAttachmentSet) {
            try {
                String uniqueFilename = file.getLocation();
                String destination = "candidate/migrated/" + uniqueFilename;
                File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), destination);
                String extractedText = TextExtractHelper.getTextExtractFromFile(srcFile, file.getFileType());
                if (StringUtils.isNotBlank(extractedText)) {
                    file.setTextExtract(extractedText);
                }
            } catch (Exception e) {
                log.error("Could not extract text from " + file.getLocation(), e.getMessage());
            }
        }
    }

    /**
     * Test text extraction on newly added files (not in migrated s3 bucket) using Text Extract Helper method
     * @throws IOException
     */
    @Transactional
    @Test
    void testTextExtractMigrateNewFiles() throws IOException {
        List<String> types = Arrays.asList("pdf", "docx", "doc", "txt");
        List<CandidateAttachment> files = candidateAttachmentRepository.findByFileTypesAndMigrated(types, false);
        assertNotNull(files);

        // Test with 10 from List
        Set<CandidateAttachment> candidateAttachmentSet = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            CandidateAttachment candidateAttachment = files.get(i);
            candidateAttachmentSet.add(candidateAttachment);
        }
        assertEquals(5, candidateAttachmentSet.size());

        // Use test set to loop through
        for(CandidateAttachment file : candidateAttachmentSet) {
            try {
                String uniqueFilename = file.getLocation();
                String destination = "candidate/" + file.getCandidate().getCandidateNumber() + "/" + uniqueFilename;
                File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), destination);
                String extractedText = TextExtractHelper.getTextExtractFromFile(srcFile, file.getFileType());
                if (StringUtils.isNotBlank(extractedText)) {
                    file.setTextExtract(extractedText);
                }
            } catch (Exception e) {
                log.error("Could not extract text from " + file.getLocation(), e.getMessage());
            }
        }
    }

    /**
     * Test text extraction from migrated Pdf file using PDFBox
     * @throws IOException
     */
    @Transactional
    @Test
    void extractTextFromMigratedPdf() throws IOException {
        // Get all Pdf files
        List<CandidateAttachment> candidatePdfs = candidateAttachmentRepository.findByFileType("pdf");
        assertNotNull(candidatePdfs);

        // Test with 10 from List
        Set<CandidateAttachment> candidateAttachmentSet = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            CandidateAttachment candidateAttachment;
            candidateAttachment = candidatePdfs.get(i);
            candidateAttachmentSet.add(candidateAttachment);
        }
        assertEquals(10, candidateAttachmentSet.size());

        // Loop through test set using pdf text extract helper methods
        for(CandidateAttachment pdf : candidateAttachmentSet){
            try {
                String uniqueFilename = pdf.getLocation();
                String destination = "candidate/migrated/" + uniqueFilename;
                File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), destination);
                String pdfExtract = TextExtractHelper.getTextFromPDFFile(srcFile);
                assertNotNull(pdfExtract);
            } catch (Exception e) {
                log.error("Could not extract text from " + pdf.getLocation(), e.getMessage());
            }
        }
    }

    /**
     * Test text extraction from migrated Docx file using IText
     * @throws IOException
     */
    @Transactional
    @Test
    void extractTextFromMigratedDocx() throws IOException {
        // Get all docx files
        List<CandidateAttachment> candidateDocs = candidateAttachmentRepository.findByFileType("docx");
        assertNotNull(candidateDocs);

        // Create test set of docx files
        Set<CandidateAttachment> candidateAttachmentSet = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            CandidateAttachment candidateAttachment;
            candidateAttachment = candidateDocs.get(i);
            candidateAttachmentSet.add(candidateAttachment);
        }

        // Loop through test set using docx text extract helper methods
        for(CandidateAttachment docx : candidateAttachmentSet) {
            try {
                String uniqueFilename = docx.getLocation();
                String destination = "candidate/migrated/" + uniqueFilename;
                File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), destination);
                String pdfExtract = TextExtractHelper.getTextFromDocxFile(srcFile);
                assertNotNull(pdfExtract);
            } catch (Exception e) {
                log.error("Could not extract text from " + docx.getLocation(), e.getMessage());
            }
        }
    }

    /**
     * Test text extraction from migrated Doc file using IText
     * @throws IOException
     */
    @Transactional
    @Test
    void extractTextFromMigratedDoc() {
        // Get all doc files
        List<CandidateAttachment> candidateDocs = candidateAttachmentRepository.findByFileType("doc");
        assertNotNull(candidateDocs);

        // Create test set of doc files
        Set<CandidateAttachment> candidateAttachmentSet = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            CandidateAttachment candidateAttachment;
            candidateAttachment = candidateDocs.get(i);
            candidateAttachmentSet.add(candidateAttachment);
        }

        // Loop through test set using docx text extract helper methods
        for(CandidateAttachment doc : candidateAttachmentSet) {
            try{
                String uniqueFilename = doc.getLocation();
                String destination = "candidate/migrated/" + uniqueFilename;
                File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), destination);
                String pdfExtract = TextExtractHelper.getTextFromDocFile(srcFile);
                assertNotNull(pdfExtract);
            } catch (Exception e) {
                log.error("Could not extract text from " + doc.getLocation(), e.getMessage());
            }
        }

    }
}
