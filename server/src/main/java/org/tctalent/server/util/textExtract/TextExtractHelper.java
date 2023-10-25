/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.util.textExtract;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.tctalent.server.repository.db.CandidateAttachmentRepository;
import org.tctalent.server.service.db.aws.S3ResourceHelper;

public class TextExtractHelper {

        private final CandidateAttachmentRepository candidateAttachmentRepository;
        private final S3ResourceHelper s3ResourceHelper;

        @Autowired
        public TextExtractHelper(CandidateAttachmentRepository candidateAttachmentRepository,
                                 S3ResourceHelper s3ResourceHelper) {
            this.candidateAttachmentRepository = candidateAttachmentRepository;
            this.s3ResourceHelper = s3ResourceHelper;
        }

        public String getTextFromPDFFile(File srcFile) throws IOException {
            PDFTextStripper tStripper = new PDFTextStripper();
            tStripper.setSortByPosition(true);
            PDDocument document = PDDocument.load(srcFile);
            String pdfFileInText = "";
            if (!document.isEncrypted()) {
                pdfFileInText = tStripper.getText(document);
            }
            document.close();
            return pdfFileInText.trim();
        }

        public String getTextFromDocxFile(File srcFile) throws IOException {
            FileInputStream fis = new FileInputStream(srcFile);
            XWPFDocument doc = new XWPFDocument(fis);
            XWPFWordExtractor xwe = new XWPFWordExtractor(doc);
            String docxTxt = xwe.getText();
            xwe.close();
            return docxTxt;
        }

        public String getTextFromDocFile(File srcFile) throws IOException {
            FileInputStream fis = new FileInputStream(srcFile);
            HWPFDocument document = new HWPFDocument(fis);
            WordExtractor we = new WordExtractor(document);
            String docTxt = we.getText();
            we.close();
            return docTxt;
        }

        public String getTextFromTxtFile(File srcFile) throws IOException {
            String txt = new String(Files.readAllBytes(Paths.get(srcFile.getPath())));
            return txt;
        }

        public @Nullable String getTextExtractFromFile(File srcFile, @Nullable String fileType) throws IOException {
            if(fileType == null) {
                return null;
            } else if(fileType.equals("pdf")) {
                return getTextFromPDFFile(srcFile);
            } else if (fileType.equals("docx")) {
                return getTextFromDocxFile(srcFile);
            } else if (fileType.equals("doc")) {
                return getTextFromDocFile(srcFile);
            } else if (fileType.equals("txt")) {
                return getTextFromTxtFile(srcFile);
            } else {
                return null;
            }
        }

        private static String getFileExtension(String fileName) {
            // Checks that a . exists and that it isn't at the start of the filename (indication there is no file name just a file type e.g. ".pdf"
            if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
                return fileName.substring(fileName.lastIndexOf(".")+1);
            else return "";
        }

}
