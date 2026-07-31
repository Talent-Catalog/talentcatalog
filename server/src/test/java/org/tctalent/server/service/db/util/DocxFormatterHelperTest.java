/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.CTTblLayoutType;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.tctalent.server.exception.CvGenerationException;
class DocxFormatterHelperTest {

  private static final long EXPECTED_LOGO_WIDTH_EMUS = 275L * 9525L;
  private static final long FALLBACK_LOGO_HEIGHT_EMUS = 70L * 9525L;

  private DocxFormatterHelper helper;

  @TempDir
  private Path tempDir;

  @BeforeEach
  void setUp() {
    helper = new DocxFormatterHelper();
  }

  @Test
  void formatXhtmlAsDocxGeneratesDocxBytesUsingEnhancedPath() {
    byte[] result = helper.formatXhtmlAsDocx(
        """
            <html>
              <body>
                <p>Hello DOCX</p>
              </body>
            </html>
            """,
        tempDir.toUri().toString()
    );

    assertNotNull(result);
    assertTrue(result.length > 0);
  }

  @Test
  void formatXhtmlAsDocxThrowsCvGenerationExceptionWhenEnhancedAndFallbackBothFail() {
    RuntimeException primaryException = new RuntimeException("primary failed");
    RuntimeException fallbackException = new RuntimeException("fallback failed");

    try (MockedStatic<WordprocessingMLPackage> wordPackageStatic =
        Mockito.mockStatic(WordprocessingMLPackage.class)) {
      wordPackageStatic.when(WordprocessingMLPackage::createPackage)
          .thenThrow(primaryException)
          .thenThrow(fallbackException);

      CvGenerationException exception = org.junit.jupiter.api.Assertions.assertThrows(
          CvGenerationException.class,
          () -> helper.formatXhtmlAsDocx("<html><body><p>Test</p></body></html>", "")
      );

      assertEquals(DocxFormatterHelper.DOCX_GENERATION_ERROR_MESSAGE, exception.getMessage());
      assertEquals(fallbackException, exception.getCause());
      assertEquals(1, exception.getSuppressed().length);
      assertEquals(primaryException, exception.getSuppressed()[0]);
    }
  }

  @Test
  void saveWordPackageReturnsDocxBytes() throws Exception {
    WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();

    byte[] result = invokeByteArray(
        "saveWordPackage",
        new Class<?>[] {WordprocessingMLPackage.class},
        wordPackage
    );

    assertNotNull(result);
    assertTrue(result.length > 0);
  }

  @Test
  void addFooterAddsFooterReferenceToSectionProperties() throws Exception {
    WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();

    invokeVoid("addFooter", new Class<?>[] {WordprocessingMLPackage.class}, wordPackage);

    SectPr sectionProperties = wordPackage.getMainDocumentPart()
        .getJaxbElement()
        .getBody()
        .getSectPr();

    assertNotNull(sectionProperties);
    assertEquals(1, sectionProperties.getEGHdrFtrReferences().size());
  }

  @Test
  void addHeaderReturnsWithoutAddingReferenceWhenLogoIsNullOrBlank() throws Exception {
    WordprocessingMLPackage nullLogoPackage = WordprocessingMLPackage.createPackage();
    WordprocessingMLPackage blankLogoPackage = WordprocessingMLPackage.createPackage();

    int nullLogoReferenceCountBefore = headerFooterReferenceCount(nullLogoPackage);
    int blankLogoReferenceCountBefore = headerFooterReferenceCount(blankLogoPackage);

    invokeVoid(
        "addHeader",
        new Class<?>[] {WordprocessingMLPackage.class, String.class, String.class},
        nullLogoPackage,
        null,
        tempDir.toUri().toString()
    );
    invokeVoid(
        "addHeader",
        new Class<?>[] {WordprocessingMLPackage.class, String.class, String.class},
        blankLogoPackage,
        "   ",
        tempDir.toUri().toString()
    );

    assertEquals(nullLogoReferenceCountBefore, headerFooterReferenceCount(nullLogoPackage));
    assertEquals(blankLogoReferenceCountBefore, headerFooterReferenceCount(blankLogoPackage));
  }

  @Test
  void addHeaderSwallowsInvalidLogoAndDoesNotAddHeaderReference() throws Exception {
    WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();

    int referenceCountBefore = headerFooterReferenceCount(wordPackage);

    invokeVoid(
        "addHeader",
        new Class<?>[] {WordprocessingMLPackage.class, String.class, String.class},
        wordPackage,
        "file:/does/not/exist/logo.png",
        tempDir.toUri().toString()
    );

    assertEquals(referenceCountBefore, headerFooterReferenceCount(wordPackage));
  }


  @Test
  void addHeaderAddsHeaderReferenceWhenLogoCanBeRead() throws Exception {
    WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
    Path logoPath = tempDir.resolve("logo.png");
    Files.write(logoPath, pngBytes());

    invokeVoid(
        "addHeader",
        new Class<?>[] {WordprocessingMLPackage.class, String.class, String.class},
        wordPackage,
        logoPath.toUri().toString(),
        tempDir.toUri().toString()
    );

    SectPr sectionProperties = wordPackage.getMainDocumentPart()
        .getJaxbElement()
        .getBody()
        .getSectPr();

    assertNotNull(sectionProperties);
    assertEquals(1, sectionProperties.getEGHdrFtrReferences().size());
  }


  @Test
  void createImageRunCreatesDrawingRun() throws Exception {
    WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
    HeaderPart headerPart = new HeaderPart();
    headerPart.setPackage(wordPackage);
    ObjectFactory factory = Context.getWmlObjectFactory();

    R result = invokeRun(
        "createImageRun",
        new Class<?>[] {
            WordprocessingMLPackage.class,
            HeaderPart.class,
            byte[].class,
            ObjectFactory.class
        },
        wordPackage,
        headerPart,
        pngBytes(),
        factory
    );

    assertNotNull(result);
    assertEquals(1, result.getContent().size());
  }

  @Test
  void calculateLogoDimensionsUsesFallbackForNullEmptyAndUnreadableBytes() throws Exception {
    long[] nullResult = invokeLongArray(
        new Class<?>[] {byte[].class},
        (Object) null
    );
    long[] emptyResult = invokeLongArray(
        new Class<?>[] {byte[].class},
        (Object) new byte[0]
    );
    long[] unreadableResult = invokeLongArray(
        new Class<?>[] {byte[].class},
        (Object) "not an image".getBytes(StandardCharsets.UTF_8)
    );

    assertArrayEquals(new long[] {EXPECTED_LOGO_WIDTH_EMUS, FALLBACK_LOGO_HEIGHT_EMUS}, nullResult);
    assertArrayEquals(new long[] {EXPECTED_LOGO_WIDTH_EMUS, FALLBACK_LOGO_HEIGHT_EMUS}, emptyResult);
    assertArrayEquals(
        new long[] {EXPECTED_LOGO_WIDTH_EMUS, FALLBACK_LOGO_HEIGHT_EMUS},
        unreadableResult
    );
  }

  @Test
  void calculateLogoDimensionsKeepsWidthAndScalesHeightForReadableImage() throws Exception {
    long[] result = invokeLongArray(
        new Class<?>[] {byte[].class},
        (Object) pngBytes()
    );

    assertEquals(EXPECTED_LOGO_WIDTH_EMUS, result[0]);
    assertEquals(Math.round(EXPECTED_LOGO_WIDTH_EMUS * 0.5), result[1]);
  }

  @Test
  void readResourceBytesReadsAbsoluteAndRelativeResources() throws Exception {
    Path absoluteFile = tempDir.resolve("absolute.txt");
    Path relativeFile = tempDir.resolve("relative.txt");

    Files.writeString(absoluteFile, "absolute");
    Files.writeString(relativeFile, "relative");

    byte[] absoluteBytes = invokeByteArray(
        "readResourceBytes",
        new Class<?>[] {String.class, String.class},
        absoluteFile.toUri().toString(),
        tempDir.toUri().toString()
    );
    byte[] relativeBytes = invokeByteArray(
        "readResourceBytes",
        new Class<?>[] {String.class, String.class},
        "relative.txt",
        tempDir.toUri().toString()
    );

    assertEquals("absolute", new String(absoluteBytes, StandardCharsets.UTF_8));
    assertEquals("relative", new String(relativeBytes, StandardCharsets.UTF_8));
  }

  @Test
  void addFieldAddsWordFieldRunsToParagraph() throws Exception {
    ObjectFactory factory = Context.getWmlObjectFactory();
    P paragraph = factory.createP();

    invokeVoid(
        "addField",
        new Class<?>[] {P.class, String.class, ObjectFactory.class},
        paragraph,
        "PAGE",
        factory
    );

    assertEquals(5, paragraph.getContent().size());
  }

  @Test
  void createTextRunCreatesRunWithTextAndFontSize() throws Exception {
    ObjectFactory factory = Context.getWmlObjectFactory();

    R run = invokeRun(
        "createTextRun",
        new Class<?>[] {String.class, ObjectFactory.class},
        "Page ",
        factory
    );

    assertNotNull(run.getRPr());
    assertEquals(BigInteger.valueOf(18), run.getRPr().getSz().getVal());
    assertEquals(1, run.getContent().size());
  }

  @Test
  void getOrCreateSectionPropertiesCreatesWhenMissingAndReturnsExistingWhenPresent()
      throws Exception {
    WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
    ObjectFactory factory = Context.getWmlObjectFactory();

    SectPr created = invokeSectPr(
        new Class<?>[] {WordprocessingMLPackage.class, ObjectFactory.class},
        wordPackage,
        factory
    );
    SectPr existing = invokeSectPr(
        new Class<?>[] {WordprocessingMLPackage.class, ObjectFactory.class},
        wordPackage,
        factory
    );

    assertNotNull(created);
    assertEquals(created, existing);
  }

  @Test
  void clearTableColumnWidthsClearsTableGridTableWidthLayoutAndCellWidth() throws Exception {
    ObjectFactory factory = Context.getWmlObjectFactory();

    Tbl table = factory.createTbl();
    TblGrid tableGrid = factory.createTblGrid();
    TblPr tableProperties = factory.createTblPr();
    TblWidth tableWidth = factory.createTblWidth();
    CTTblLayoutType tableLayout = factory.createCTTblLayoutType();

    table.setTblGrid(tableGrid);
    tableProperties.setTblW(tableWidth);
    tableProperties.setTblLayout(tableLayout);
    table.setTblPr(tableProperties);

    Tc cell = factory.createTc();
    TcPr cellProperties = factory.createTcPr();
    TblWidth cellWidth = factory.createTblWidth();
    cellProperties.setTcW(cellWidth);
    cell.setTcPr(cellProperties);

    table.getContent().add(cell);

    JAXBElement<Tbl> wrappedTable = new JAXBElement<>(
        new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tbl"),
        Tbl.class,
        table
    );

    invokeVoid("clearTableColumnWidths", new Class<?>[] {Object.class}, wrappedTable);


    assertNull(table.getTblGrid());
    assertNull(table.getTblPr().getTblW());
    assertNull(table.getTblPr().getTblLayout());
    assertNull(cell.getTcPr().getTcW());
  }

  @Test
  void clearTableColumnWidthsHandlesNullTableAndCellProperties() throws Exception {
    ObjectFactory factory = Context.getWmlObjectFactory();

    Tbl table = factory.createTbl();
    Tc cell = factory.createTc();

    table.getContent().add(cell);

    invokeVoid("clearTableColumnWidths", new Class<?>[] {Object.class}, table);

    assertNull(table.getTblGrid());
    assertNull(table.getTblPr());
    assertNull(cell.getTcPr());
  }

  @Test
  void unwrapReturnsJaxbValueOrOriginalObject() throws Exception {
    ObjectFactory factory = Context.getWmlObjectFactory();
    Tbl table = factory.createTbl();
    JAXBElement<Tbl> wrappedTable = new JAXBElement<>(
        new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "tbl"),
        Tbl.class,
        table
    );

    Object unwrapped = invokeObject(new Class<?>[] {Object.class}, wrappedTable);
    Object unchanged = invokeObject(new Class<?>[] {Object.class}, table);

    assertEquals(table, unwrapped);
    assertEquals(table, unchanged);
  }

  @Test
  void prepareXhtmlForDocxRemovesPdfOnlyContentAndAddsTableCellStyles() throws Exception {
    String result = invokeString(
        "prepareXhtmlForDocx",
        new Class<?>[] {String.class},
        """
            <html>
              <body>
                <div class="header"><img class="logo" src="logo.png"/></div>
                <div class="footer">Footer</div>
                <span class="page-count">1</span>
                <table style="color:red">
                  <tr>
                    <td style="font-weight:bold;">Cell</td>
                  </tr>
                </table>
              </body>
            </html>
            """
    );

    assert result != null;
    assertFalse(result.contains("class=\"header\""));
    assertFalse(result.contains("class=\"footer\""));
    assertFalse(result.contains("page-count"));
    assertTrue(result.contains("color:red; width:100%; table-layout:fixed; border-collapse:collapse;"));
    assertTrue(result.contains(
        "font-weight:bold; width:100%; vertical-align:top; word-wrap:break-word;"
    ));
  }

  @Test
  void appendStyleHandlesNullBlankExistingStyleWithSemicolonAndWithoutSemicolon()
      throws Exception {
    assertEquals(
        "width:100%;",
        invokeString("appendStyle", new Class<?>[] {String.class, String.class}, null, "width:100%;")
    );
    assertEquals(
        "width:100%;",
        invokeString("appendStyle", new Class<?>[] {String.class, String.class}, "   ", "width:100%;")
    );
    assertEquals(
        "color:red; width:100%;",
        invokeString(
            "appendStyle",
            new Class<?>[] {String.class, String.class},
            "color:red",
            "width:100%;"
        )
    );
    assertEquals(
        "color:red; width:100%;",
        invokeString(
            "appendStyle",
            new Class<?>[] {String.class, String.class},
            "color:red;",
            "width:100%;"
        )
    );
  }

  @Test
  void extractLogoSourceReturnsHeaderLogoGenericLogoOrNull() throws Exception {
    assertEquals(
        "header-logo.png",
        invokeString(
            "extractLogoSource",
            new Class<?>[] {String.class},
            "<html><body><div class=\"header\"><img class=\"logo\" src=\"header-logo.png\"/></div></body></html>"
        )
    );
    assertEquals(
        "body-logo.png",
        invokeString(
            "extractLogoSource",
            new Class<?>[] {String.class},
            "<html><body><img class=\"logo\" src=\"body-logo.png\"/></body></html>"
        )
    );
    assertNull(invokeString(
        "extractLogoSource",
        new Class<?>[] {String.class},
        "<html><body><p>No logo</p></body></html>"
    ));
  }

  private byte[] pngBytes() throws Exception {
    BufferedImage image = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }

  private Object invokeObject(Class<?>[] parameterTypes, Object... args)
      throws Exception {
    return invoke("unwrap", parameterTypes, args);
  }

  private void invokeVoid(String methodName, Class<?>[] parameterTypes, Object... args)
      throws Exception {
    invoke(methodName, parameterTypes, args);
  }

  private String invokeString(String methodName, Class<?>[] parameterTypes, Object... args)
      throws Exception {
    Object result = invoke(methodName, parameterTypes, args);
    return result == null ? null : (String) result;
  }

  private byte[] invokeByteArray(String methodName, Class<?>[] parameterTypes, Object... args)
      throws Exception {
    return (byte[]) invoke(methodName, parameterTypes, args);
  }

  private long[] invokeLongArray(Class<?>[] parameterTypes, Object... args)
      throws Exception {
    return (long[]) invoke("calculateLogoDimensions", parameterTypes, args);
  }

  private R invokeRun(String methodName, Class<?>[] parameterTypes, Object... args)
      throws Exception {
    return (R) invoke(methodName, parameterTypes, args);
  }

  private SectPr invokeSectPr(Class<?>[] parameterTypes, Object... args)
      throws Exception {
    return (SectPr) invoke("getOrCreateSectionProperties", parameterTypes, args);
  }

  private Object invoke(String methodName, Class<?>[] parameterTypes, Object... args)
      throws Exception {
    Method method = DocxFormatterHelper.class.getDeclaredMethod(methodName, parameterTypes);
    method.setAccessible(true);
    return method.invoke(helper, args);
  }

  private int headerFooterReferenceCount(WordprocessingMLPackage wordPackage) {
    SectPr sectionProperties = wordPackage.getMainDocumentPart()
        .getJaxbElement()
        .getBody()
        .getSectPr();

    return sectionProperties == null
        ? 0
        : sectionProperties.getEGHdrFtrReferences().size();
  }
}