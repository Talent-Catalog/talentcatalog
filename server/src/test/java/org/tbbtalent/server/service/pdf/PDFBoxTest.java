package org.tbbtalent.server.service.pdf;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.filter.TextRegionEventFilter;
import com.itextpdf.kernel.pdf.canvas.parser.listener.FilteredEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.junit.jupiter.api.Test;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.*;

import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PDFBoxTest {

    @Test
    void testPDFBoxMethods() throws IOException {
        File file = new File("src/test/resources/CV.pdf");

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

        PrintWriter pw = new PrintWriter("src/test/pdf.txt");
        pw.print(parsedText);
        pw.close();

        assertNotNull(pw);

        //SECOND WAY USING PDFBOX

        PDFTextStripper tStripper = new PDFTextStripper();
        tStripper.setSortByPosition(true);
        PDDocument document = PDDocument.load(new File("src/test/resources/CV.pdf"));
        String pdfFileInText = "";
        if (!document.isEncrypted()) {
            pdfFileInText = tStripper.getText(document);
        }
        System.out.println(pdfFileInText.trim());

        assertNotEquals("", pdfFileInText);

    }

    @Test
    void testITextMethods() throws IOException {
        String src = "src/test/resources/CV.pdf";
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(src));

        assertNotNull(pdfDoc);

        String str;
        StringBuffer txt = new StringBuffer();

        for (int i=1; i<= pdfDoc.getNumberOfPages(); i++){
            str = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), new LocationTextExtractionStrategy());
            txt.append(str);
        }

        assertNotEquals("", txt);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("src/test/pdf2.txt")))) {
            writer.write(String.valueOf(txt));
        }

        pdfDoc.close();
    }

    @Test
    void testTxtFileExtraction() throws IOException {
        String data = new String(Files.readAllBytes(Paths.get("src/test/pdf2.txt")));
        System.out.println(data);
        assertNotEquals("",data);
    }

    @Test
    void testFileExtensionExtraction(){
        // Test the File extension methods
        //String type = Files.probeContentType(cv.toPath());
        File cv = new File("src/test/resources/WordCV.docx");
        String type = getFileExtension(cv);
        assertEquals("docx", type);

        File cvPdf = new File("src/test/resources/CV.pdf");
        String typePdf = getFileExtension(cvPdf);
        assertEquals("pdf", typePdf);

        File test = new File(".test");
        String typeTest = getFileExtension(test);
        assertEquals("", typeTest);

        File test2 = new File("..test");
        String typeTest2 = getFileExtension(test2);
        assertEquals("test", typeTest2);
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        // Checks that a . exists and that it isn't at the start of the filename (indication there is no file name just a file type e.g. ".pdf"
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    @Test
    void testApachePoiMethods() throws IOException {
        File cv = new File("src/test/resources/WordCV.docx");
        FileInputStream fis = new FileInputStream(cv);
        XWPFDocument doc = new XWPFDocument(fis);
        XWPFWordExtractor xwe = new XWPFWordExtractor(doc);
        String theText = xwe.getText();
        assertNotEquals("", theText);
        xwe.close();
    }
}

