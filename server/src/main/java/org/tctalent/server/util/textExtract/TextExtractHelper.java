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

package org.tctalent.server.util.textExtract;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.lang.Nullable;

public class TextExtractHelper {

    public static String getTextFromPDFFile(File srcFile) throws IOException {
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

    public static String getTextFromDocxFile(File srcFile) throws IOException {
        FileInputStream fis = new FileInputStream(srcFile);
        XWPFDocument doc = new XWPFDocument(fis);
        XWPFWordExtractor xwe = new XWPFWordExtractor(doc);
        String docxTxt = xwe.getText();
        xwe.close();
        return docxTxt;
    }

    public static String getTextFromDocFile(File srcFile) throws IOException {
        FileInputStream fis = new FileInputStream(srcFile);
        HWPFDocument document = new HWPFDocument(fis);
        WordExtractor we = new WordExtractor(document);
        String docTxt = we.getText();
        we.close();
        return docTxt;
    }

    public static String getTextFromTxtFile(File srcFile) throws IOException {
        return new String(Files.readAllBytes(Paths.get(srcFile.getPath())));
    }

    /**
     * Extracts text from the given file according to the file's type (pdf, docx, doc, txt). The
     * type can be passed in directly or as a full file name, in which case the type is extracted
     * from the file name.
     * <p/>
     * Note that the file may be a temporary file with a random name. That is why we don't use
     * File.getName() to extract the file type.
     *
     * @param file           File to extract text from
     * @param fileTypeOrName File type (pdf, docx, doc, txt) or full file name
     * @return Extracted text
     * @throws IOException If there is a problem reading the file
     */
    public static @Nullable String getTextExtractFromFile(
        File file, @Nullable String fileTypeOrName) throws IOException {

        String fileType;
        int dotIndex = fileTypeOrName == null ? -1 : fileTypeOrName.lastIndexOf(".");
        if (dotIndex > 0) {
            fileType = fileTypeOrName.substring(dotIndex + 1);
        } else {
            fileType = fileTypeOrName;
        }

        String s = null;
        if ("pdf".equals(fileType)) {
            s = getTextFromPDFFile(file);
        } else if ("docx".equals(fileType)) {
            s = getTextFromDocxFile(file);
        } else if ("doc".equals(fileType)) {
            s = getTextFromDocFile(file);
        } else if ("txt".equals(fileType)) {
            s = getTextFromTxtFile(file);
        }
        if (s != null) {
            // Remove any null bytes to avoid problems like
            // PSQLException: ERROR: invalid byte sequence for encoding "UTF8"
            s = Pattern.compile("\\x00").matcher(s).replaceAll("?");
        }
        return s;
    }
}
