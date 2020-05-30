package org.tbbtalent.server.util.textExtract;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.repository.CandidateAttachmentRepository;
import org.tbbtalent.server.service.aws.S3ResourceHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
