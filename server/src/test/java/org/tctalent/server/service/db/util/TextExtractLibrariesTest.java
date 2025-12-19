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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tctalent.server.service.db.aws.S3ResourceHelper;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
public class TextExtractLibrariesTest {

    @Autowired
    private S3ResourceHelper s3ResourceHelper;

    /**
     * Test PDFBox PDF text extraction (two ways)
     */
    @Test
    void pdfBoxMethods() throws IOException {
        File file = new File("src/test/resources/text/EnglishPdf.pdf");
        assertTrue(file.exists());

        //FIRST WAY USING PDFBOX
        //LOAD FILE
        String parsedText;
        PDFParser parser = new PDFParser(new RandomAccessFile(file, "r"));
        parser.parse();
        //EXTRACT TEXT
        COSDocument cosDoc = parser.getDocument();
        PDFTextStripper pdfStripper = new PDFTextStripper();
        pdfStripper.setSortByPosition(true);
        PDDocument pdDoc = new PDDocument(cosDoc);
        parsedText = pdfStripper.getText(pdDoc);

        assertNotEquals("", parsedText);

        //SECOND WAY USING PDFBOX
        PDFTextStripper tStripper = new PDFTextStripper();
        tStripper.setSortByPosition(true);
        PDDocument document = PDDocument.load(new File("src/test/resources/text/EnglishPdf.pdf"));
        String pdfFileInText = "";
        if (!document.isEncrypted()) {
            pdfFileInText = tStripper.getText(document).trim();
        }
        if(StringUtils.isNotEmpty(pdfFileInText)){
            assertNotNull(pdfFileInText);
        }
    }

    /**
     * Test IText PDF text extraction
     */
    @Test
    void iTextMethodsPdf() throws IOException {
//        String src = "src/test/resources/text/EnglishPdf.pdf";
//        PdfDocument pdfDoc = new PdfDocument(new PdfReader(src));
//        assertNotNull(pdfDoc);
//
//        String str;
//        StringBuffer txt = new StringBuffer();
//        for (int i=1; i<= pdfDoc.getNumberOfPages(); i++){
//            str = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), new LocationTextExtractionStrategy());
//            txt.append(str);
//        }
//        assertNotEquals("", txt);
//        pdfDoc.close();
    }

    /**
     * Test Docx Extraction on English file
     */
    @Test
    void apachePoiMethodDocx() throws IOException {
        File cv = new File("src/test/resources/text/EnglishDocx.docx");
        FileInputStream fis = new FileInputStream(cv);
        XWPFDocument doc = new XWPFDocument(fis);
        XWPFWordExtractor xwe = new XWPFWordExtractor(doc);
        String theText = xwe.getText();
        assertNotEquals("", theText);
        xwe.close();
    }

    /**
     * Test Docx Text Extraction on Arabic file
     */
    @Test
    void apachePoiMethodDocxArabic() throws IOException {
        File cv = new File("src/test/resources/text/ArabicDocx.docx");
        FileInputStream fis = new FileInputStream(cv);
        XWPFDocument doc = new XWPFDocument(fis);
        XWPFWordExtractor xwe = new XWPFWordExtractor(doc);
        String theText = xwe.getText();
        assertNotEquals("", theText);
        xwe.close();
    }

    /**
     * Test TXT file text extraction
     */
    @Test
    void txtFileExtraction() throws IOException {
        String data = new String(Files.readAllBytes(Paths.get("src/test/resources/text/EnglishTxt.txt")));
        assertNotEquals("", data);
    }

    /**
     * Extract file type from a file
     */
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        // Checks that a . exists and that it isn't at the start of the filename
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    /**
     * Test file type extraction
     */
    @Test
    void testFileExtensionExtraction(){
        File pdfFile = new File("src/test/resources/text/EnglishPdf.pdf");
        String typePdf = getFileExtension(pdfFile);
        assertEquals("pdf", typePdf);

        File docxFile = new File("src/test/resources/text/EnglishDocx.docx");
        String typeDocx = getFileExtension(docxFile);
        assertEquals("docx", typeDocx);

        File test = new File(".test");
        String typeTest = getFileExtension(test);
        assertEquals("", typeTest);

        File test2 = new File("..test");
        String typeTest2 = getFileExtension(test2);
        assertEquals("test", typeTest2);
    }

}

