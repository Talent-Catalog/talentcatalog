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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.tctalent.server.service.db.util.DocxFormatterHelper.DOCX_GENERATION_ERROR_MESSAGE;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.tctalent.server.exception.CvGenerationException;
import org.tctalent.server.model.db.Candidate;

@ExtendWith(MockitoExtension.class)
class DocxHelperTest {

  @Mock
  private CvTemplateHelper cvTemplateHelper;

  @Mock
  private DocxFormatterHelper docxFormatterHelper;

  private DocxHelper docxHelper;

  @BeforeEach
  void setUp() {
    docxHelper = new DocxHelper(cvTemplateHelper, docxFormatterHelper);
  }

  @Test
  void generateDocx_whenSuccessful_returnsByteArrayResource() {
    Candidate candidate = new Candidate();
    String xhtml = "<html><body>CV</body></html>";
    byte[] docxBytes = "generated-docx".getBytes(StandardCharsets.UTF_8);

    when(cvTemplateHelper.renderCvXhtml(candidate, true, false))
        .thenReturn(xhtml);
    when(docxFormatterHelper.formatXhtmlAsDocx(eq(xhtml), anyString()))
        .thenReturn(docxBytes);

    Resource resource = docxHelper.generateDocx(candidate, true, false);

    assertInstanceOf(ByteArrayResource.class, resource);
    assertArrayEquals(docxBytes, ((ByteArrayResource) resource).getByteArray());

    ArgumentCaptor<String> resourceBaseUrlCaptor = ArgumentCaptor.forClass(String.class);
    verify(docxFormatterHelper)
        .formatXhtmlAsDocx(eq(xhtml), resourceBaseUrlCaptor.capture());

    assertNotNull(resourceBaseUrlCaptor.getValue());
  }

  @Test
  void generateDocx_whenFormatterThrowsCvGenerationException_rethrowsSameException() {
    Candidate candidate = new Candidate();
    String xhtml = "<html><body>CV</body></html>";
    CvGenerationException originalException =
        new CvGenerationException("DOCX formatting failed");

    when(cvTemplateHelper.renderCvXhtml(candidate, true, true))
        .thenReturn(xhtml);
    when(docxFormatterHelper.formatXhtmlAsDocx(eq(xhtml), anyString()))
        .thenThrow(originalException);

    CvGenerationException thrown = assertThrows(
        CvGenerationException.class,
        () -> docxHelper.generateDocx(candidate, true, true)
    );

    assertSame(originalException, thrown);
  }

  @Test
  void generateDocx_whenUnexpectedExceptionOccurs_wrapsInCvGenerationException() {
    Candidate candidate = new Candidate();
    String xhtml = "<html><body>CV</body></html>";
    RuntimeException formatterException = new RuntimeException("Unexpected formatter failure");

    when(cvTemplateHelper.renderCvXhtml(candidate, false, false))
        .thenReturn(xhtml);
    when(docxFormatterHelper.formatXhtmlAsDocx(eq(xhtml), anyString()))
        .thenThrow(formatterException);

    CvGenerationException thrown = assertThrows(
        CvGenerationException.class,
        () -> docxHelper.generateDocx(candidate, false, false)
    );

    assertEquals(DOCX_GENERATION_ERROR_MESSAGE, thrown.getMessage());
    assertSame(formatterException, thrown.getCause());
  }

  @Test
  void generateDocx_whenTemplateRenderingThrowsCvGenerationException_doesNotCallFormatter() {
    Candidate candidate = new Candidate();
    CvGenerationException originalException =
        new CvGenerationException("Template rendering failed");

    when(cvTemplateHelper.renderCvXhtml(candidate, true, true))
        .thenThrow(originalException);

    CvGenerationException thrown = assertThrows(
        CvGenerationException.class,
        () -> docxHelper.generateDocx(candidate, true, true)
    );

    assertSame(originalException, thrown);
    verifyNoInteractions(docxFormatterHelper);
  }

  @Test
  void getResourceBaseUrl_whenPdfResourceIsMissing_returnsEmptyString() throws Exception {
    ClassLoader classLoaderWithoutPdfResource =
        new DocxHelperWithoutPdfResourceClassLoader(DocxHelper.class.getClassLoader());

    Class<?> isolatedDocxHelperClass = Class.forName(
        DocxHelper.class.getName(),
        true,
        classLoaderWithoutPdfResource
    );

    Constructor<?> constructor = isolatedDocxHelperClass.getDeclaredConstructor(
        CvTemplateHelper.class,
        DocxFormatterHelper.class
    );

    Object isolatedDocxHelper = constructor.newInstance(null, null);

    Method getResourceBaseUrl =
        isolatedDocxHelperClass.getDeclaredMethod("getResourceBaseUrl");
    getResourceBaseUrl.setAccessible(true);

    assertEquals("", getResourceBaseUrl.invoke(isolatedDocxHelper));
  }

  private static class DocxHelperWithoutPdfResourceClassLoader extends ClassLoader {

    private DocxHelperWithoutPdfResourceClassLoader(ClassLoader parent) {
      super(parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      if (DocxHelper.class.getName().equals(name)) {
        synchronized (getClassLoadingLock(name)) {
          Class<?> loadedClass = findLoadedClass(name);
          if (loadedClass == null) {
            loadedClass = findClass(name);
          }
          if (resolve) {
            resolveClass(loadedClass);
          }
          return loadedClass;
        }
      }

      return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      String classResourceName = name.replace('.', '/') + ".class";

      try (InputStream inputStream = getParent().getResourceAsStream(classResourceName)) {
        if (inputStream == null) {
          throw new ClassNotFoundException(name);
        }

        byte[] classBytes = inputStream.readAllBytes();
        return defineClass(name, classBytes, 0, classBytes.length);
      } catch (Exception e) {
        throw new ClassNotFoundException(name, e);
      }
    }

    @Override
    public URL getResource(String name) {
      if ("pdf/".equals(name) || "/pdf/".equals(name)) {
        return null;
      }

      return super.getResource(name);
    }
  }
}