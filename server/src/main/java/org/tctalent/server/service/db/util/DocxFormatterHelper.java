/*
 * Copyright (c) 2026 Talent Catalog.
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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.xml.bind.JAXBElement;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.springframework.stereotype.Component;
import org.tctalent.server.exception.CvGenerationException;
import org.tctalent.server.logging.LogBuilder;

/**
 * Helper for converting rendered CV XHTML into DOCX format.
 *
 * <p>This class owns all DOCX-specific formatting logic, including:</p>
 *
 * <ul>
 *   <li>removing PDF-only header/footer XHTML from the body,</li>
 *   <li>adding the logo as a real DOCX header,</li>
 *   <li>adding a page-number footer,</li>
 *   <li>importing XHTML into docx4j, and</li>
 *   <li>cleaning imported table widths for better Google Docs behavior.</li>
 * </ul>
 */
@Component
@Slf4j
public class DocxFormatterHelper {

  /*
   * The PDF CSS uses .logo { width: 275px; }.
   * 1 CSS px at 96 dpi is 9525 EMUs in Word.
   */
  private static final int LOGO_WIDTH_PX = 275;
  private static final long EMUS_PER_CSS_PIXEL = 9525L;
  static final String DOCX_GENERATION_ERROR_MESSAGE = "Error generating DOCX CV";

  /**
   * Converts rendered CV XHTML into DOCX bytes.
   *
   * <p>This first tries the enhanced DOCX formatting path. If that fails, it falls back to a basic
   * XHTML-to-DOCX conversion so users still get a DOCX where possible.</p>
   *
   * @param xhtml rendered CV XHTML
   * @param resourceBaseUrl base URL used by docx4j to resolve relative resources
   * @return generated DOCX bytes
   * @throws CvGenerationException if both enhanced and fallback DOCX generation fail
   */
  public byte[] formatXhtmlAsDocx(String xhtml, String resourceBaseUrl) {
    try {
      return formatEnhancedDocx(xhtml, resourceBaseUrl);
    } catch (Exception primaryException) {
      LogBuilder.builder(log)
          .action("formatXhtmlAsDocx")
          .message("Could not generate DOCX with enhanced formatting. Falling back to basic DOCX generation.")
          .logWarn();

      try {
        return formatBasicDocx(xhtml, resourceBaseUrl);
      } catch (Exception fallbackException) {
        CvGenerationException exception =
            new CvGenerationException(DOCX_GENERATION_ERROR_MESSAGE, fallbackException);
        exception.addSuppressed(primaryException);
        throw exception;
      }
    }
  }

  private byte[] formatEnhancedDocx(String xhtml, String resourceBaseUrl) throws Exception {
    String docxXhtml = prepareXhtmlForDocx(xhtml);
    String logoSource = extractLogoSource(xhtml);

    WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
    XHTMLImporterImpl importer = new XHTMLImporterImpl(wordPackage);

    addHeader(wordPackage, logoSource, resourceBaseUrl);
    addFooter(wordPackage);

    wordPackage.getMainDocumentPart()
        .getContent()
        .addAll(importer.convert(docxXhtml, resourceBaseUrl));

    clearTableColumnWidths(wordPackage.getMainDocumentPart().getJaxbElement().getBody());

    return saveWordPackage(wordPackage);
  }

  /**
   * Fallback DOCX generation path.
   *
   * <p>This intentionally keeps the conversion simple and close to the original implementation:
   * create a package, import the rendered XHTML directly, and save the package.</p>
   */
  private byte[] formatBasicDocx(String xhtml, String resourceBaseUrl) throws Exception {
    WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
    XHTMLImporterImpl importer = new XHTMLImporterImpl(wordPackage);

    wordPackage.getMainDocumentPart()
        .getContent()
        .addAll(importer.convert(xhtml, resourceBaseUrl));

    return saveWordPackage(wordPackage);
  }

  private byte[] saveWordPackage(WordprocessingMLPackage wordPackage) throws Exception {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      wordPackage.save(outputStream);
      return outputStream.toByteArray();
    }
  }

  private void addHeader(
      WordprocessingMLPackage wordPackage,
      String logoSource,
      String resourceBaseUrl
  ) {
    try {
      if (logoSource == null || logoSource.isBlank()) {
        return;
      }

      ObjectFactory factory = Context.getWmlObjectFactory();

      HeaderPart headerPart = new HeaderPart();
      headerPart.setPackage(wordPackage);

      Hdr header = factory.createHdr();

      P logoParagraph = factory.createP();
      PPr paragraphProperties = factory.createPPr();

      Jc alignment = factory.createJc();
      alignment.setVal(JcEnumeration.LEFT);

      paragraphProperties.setJc(alignment);
      logoParagraph.setPPr(paragraphProperties);

      byte[] logoBytes = readResourceBytes(logoSource, resourceBaseUrl);
      R logoRun = createImageRun(wordPackage, headerPart, logoBytes, factory);

      logoParagraph.getContent().add(logoRun);
      header.getContent().add(logoParagraph);

      headerPart.setJaxbElement(header);

      Relationship relationship = wordPackage.getMainDocumentPart()
          .addTargetPart(headerPart);

      SectPr sectionProperties = getOrCreateSectionProperties(wordPackage, factory);

      HeaderReference headerReference = factory.createHeaderReference();
      headerReference.setId(relationship.getId());
      headerReference.setType(HdrFtrRef.DEFAULT);

      sectionProperties.getEGHdrFtrReferences().add(headerReference);
    } catch (Exception e) {
      LogBuilder.builder(log)
          .action("addHeader")
          .message("Could not add DOCX header. The CV will be generated without a header logo.")
          .logWarn();
    }
  }

  private R createImageRun(
      WordprocessingMLPackage wordPackage,
      HeaderPart headerPart,
      byte[] imageBytes,
      ObjectFactory factory
  ) throws Exception {
    BinaryPartAbstractImage imagePart =
        BinaryPartAbstractImage.createImagePart(wordPackage, headerPart, imageBytes);

    long[] dimensions = calculateLogoDimensions(imageBytes);

    Inline inline = imagePart.createImageInline(
        "CV logo",
        "CV logo",
        0,
        1,
        dimensions[0],
        dimensions[1],
        false
    );

    Drawing drawing = factory.createDrawing();
    drawing.getAnchorOrInline().add(inline);

    R run = factory.createR();
    run.getContent().add(drawing);

    return run;
  }

  private long[] calculateLogoDimensions(byte[] imageBytes) {
    long widthEmus = LOGO_WIDTH_PX * EMUS_PER_CSS_PIXEL;
    long heightEmus = 70L * EMUS_PER_CSS_PIXEL;

    if (imageBytes == null || imageBytes.length == 0) {
      LogBuilder.builder(log)
          .action("calculateLogoDimensions")
          .message("Logo image bytes were empty. Using fallback DOCX logo dimensions.")
          .logWarn();

      return new long[] {widthEmus, heightEmus};
    }

    try {
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
      if (image != null && image.getWidth() > 0) {
        heightEmus = Math.round(widthEmus * ((double) image.getHeight() / image.getWidth()));
      } else {
        LogBuilder.builder(log)
            .action("calculateLogoDimensions")
            .message("Could not read logo image dimensions. Using fallback DOCX logo dimensions.")
            .logWarn();
      }
    } catch (IOException e) {
      LogBuilder.builder(log)
          .action("calculateLogoDimensions")
          .message("Error reading logo image dimensions. Using fallback DOCX logo dimensions.")
          .logWarn(e);
    }

    return new long[] {widthEmus, heightEmus};
  }

  private byte[] readResourceBytes(String resourcePath, String resourceBaseUrl) throws Exception {
    URL resourceUrl;

    if (resourcePath.startsWith("http://")
        || resourcePath.startsWith("https://")
        || resourcePath.startsWith("file:/")) {
      resourceUrl = new URL(resourcePath);
    } else {
      resourceUrl = new URL(new URL(resourceBaseUrl), resourcePath);
    }

    try (InputStream inputStream = resourceUrl.openStream()) {
      return inputStream.readAllBytes();
    }
  }

  private void addFooter(WordprocessingMLPackage wordPackage) throws Exception {
    ObjectFactory factory = Context.getWmlObjectFactory();

    FooterPart footerPart = new FooterPart();
    footerPart.setPackage(wordPackage);

    org.docx4j.wml.Ftr footer = factory.createFtr();

    P footerParagraph = factory.createP();
    PPr paragraphProperties = factory.createPPr();

    Jc alignment = factory.createJc();
    alignment.setVal(JcEnumeration.RIGHT);
    paragraphProperties.setJc(alignment);

    PPrBase.PBdr borders = factory.createPPrBasePBdr();

    CTBorder topBorder = factory.createCTBorder();
    topBorder.setVal(STBorder.SINGLE);
    topBorder.setColor("000000");
    topBorder.setSz(BigInteger.valueOf(8));
    topBorder.setSpace(BigInteger.valueOf(4));

    borders.setTop(topBorder);
    paragraphProperties.setPBdr(borders);

    footerParagraph.setPPr(paragraphProperties);

    footerParagraph.getContent().add(createTextRun("Page ", factory));
    addField(footerParagraph, "PAGE", factory);
    footerParagraph.getContent().add(createTextRun(" of ", factory));
    addField(footerParagraph, "NUMPAGES", factory);

    footer.getContent().add(footerParagraph);
    footerPart.setJaxbElement(footer);

    Relationship relationship = wordPackage.getMainDocumentPart()
        .addTargetPart(footerPart);

    SectPr sectionProperties = getOrCreateSectionProperties(wordPackage, factory);

    FooterReference footerReference = factory.createFooterReference();
    footerReference.setId(relationship.getId());
    footerReference.setType(HdrFtrRef.DEFAULT);

    sectionProperties.getEGHdrFtrReferences().add(footerReference);
  }

  private void addField(P paragraph, String instruction, ObjectFactory factory) {
    R beginRun = factory.createR();
    FldChar begin = factory.createFldChar();
    begin.setFldCharType(STFldCharType.BEGIN);
    beginRun.getContent().add(begin);
    paragraph.getContent().add(beginRun);

    R instructionRun = factory.createR();
    Text instructionText = factory.createText();
    instructionText.setSpace("preserve");
    instructionText.setValue(" " + instruction + " ");
    instructionRun.getContent().add(factory.createRInstrText(instructionText));
    paragraph.getContent().add(instructionRun);

    R separateRun = factory.createR();
    FldChar separate = factory.createFldChar();
    separate.setFldCharType(STFldCharType.SEPARATE);
    separateRun.getContent().add(separate);
    paragraph.getContent().add(separateRun);

    paragraph.getContent().add(createTextRun("1", factory));

    R endRun = factory.createR();
    FldChar end = factory.createFldChar();
    end.setFldCharType(STFldCharType.END);
    endRun.getContent().add(end);
    paragraph.getContent().add(endRun);
  }

  private R createTextRun(String value, ObjectFactory factory) {
    R run = factory.createR();

    RPr runProperties = factory.createRPr();

    HpsMeasure fontSize = factory.createHpsMeasure();
    fontSize.setVal(BigInteger.valueOf(18));

    runProperties.setSz(fontSize);
    runProperties.setSzCs(fontSize);

    run.setRPr(runProperties);

    Text text = factory.createText();
    text.setSpace("preserve");
    text.setValue(value);

    run.getContent().add(text);

    return run;
  }

  private SectPr getOrCreateSectionProperties(
      WordprocessingMLPackage wordPackage,
      ObjectFactory factory
  ) {
    SectPr sectionProperties = wordPackage.getMainDocumentPart()
        .getJaxbElement()
        .getBody()
        .getSectPr();

    if (sectionProperties == null) {
      sectionProperties = factory.createSectPr();
      wordPackage.getMainDocumentPart()
          .getJaxbElement()
          .getBody()
          .setSectPr(sectionProperties);
    }

    return sectionProperties;
  }

  /**
   * Clears explicit table and column widths from imported DOCX tables.
   *
   * <p>Google Docs shows "Column width" as checked when the DOCX contains explicit
   * table grid widths or cell widths. Removing those width properties lets Google
   * Docs auto-fit the table content and shows the column width as unchecked / 0 in.</p>
   *
   * @param object DOCX object tree to inspect
   */
  private void clearTableColumnWidths(Object object) {
    Object unwrapped = unwrap(object);

    if (unwrapped instanceof Tbl table) {
      table.setTblGrid(null);

      TblPr tableProperties = table.getTblPr();
      if (tableProperties != null) {
        tableProperties.setTblW(null);
        tableProperties.setTblLayout(null);
      }
    }

    if (unwrapped instanceof Tc cell) {
      TcPr cellProperties = cell.getTcPr();
      if (cellProperties != null) {
        cellProperties.setTcW(null);
      }
    }

    if (unwrapped instanceof ContentAccessor contentAccessor) {
      for (Object child : contentAccessor.getContent()) {
        clearTableColumnWidths(child);
      }
    }
  }

  private Object unwrap(Object object) {
    if (object instanceof JAXBElement<?> jaxbElement) {
      return jaxbElement.getValue();
    }

    return object;
  }

  /**
   * Removes PDF-only paged-media fragments before importing XHTML into DOCX.
   *
   * <p>The PDF renderer understands the header/footer fragments because the CSS moves them into
   * page margin boxes. docx4j does not, so they must not be imported as body content.</p>
   *
   * @param xhtml rendered CV XHTML
   * @return DOCX-friendly XHTML body
   */
  private String prepareXhtmlForDocx(String xhtml) {
    Document document = Jsoup.parse(xhtml);

    document.select("div.header").remove();
    document.select("div.footer").remove();
    document.select(".page-count").remove();

    document.select("table").forEach(table ->
        table.attr("style", appendStyle(
            table.attr("style"),
            "width:100%; table-layout:fixed; border-collapse:collapse;"
        ))
    );

    document.select("td").forEach(cell ->
        cell.attr("style", appendStyle(
            cell.attr("style"),
            "width:100%; vertical-align:top; word-wrap:break-word;"
        ))
    );

    document.outputSettings()
        .syntax(Document.OutputSettings.Syntax.xml)
        .escapeMode(Entities.EscapeMode.xhtml)
        .prettyPrint(false);

    return document.outerHtml();
  }

  private String appendStyle(String existingStyle, String styleToAppend) {
    if (existingStyle == null || existingStyle.isBlank()) {
      return styleToAppend;
    }

    return existingStyle.endsWith(";")
        ? existingStyle + " " + styleToAppend
        : existingStyle + "; " + styleToAppend;
  }

  private String extractLogoSource(String xhtml) {
    Document document = Jsoup.parse(xhtml);
    Element logo = document.selectFirst("div.header img.logo, img.logo");

    return logo == null ? null : logo.attr("src");
  }
}