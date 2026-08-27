/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.html;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;

class HtmlTemplateWellFormednessTest {

  private static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

  @Test
  void termsAndMailTemplatesAreWellFormedXhtml() throws Exception {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] terms = resolver.getResources("classpath:/terms/*.html");
    Resource[] mail = resolver.getResources("classpath:/mail/*.html");

    assertTrue(terms.length > 0, "No terms templates found");
    assertTrue(mail.length > 0, "No mail templates found");

    List<Resource> templates = Stream.concat(Arrays.stream(terms), Arrays.stream(mail))
        .sorted(Comparator.comparing(Resource::getFilename))
        .toList();

    assertAll(templates.stream().map(this::wellFormedXhtmlCheck));
  }

  private Executable wellFormedXhtmlCheck(Resource template) {
    String filename = template.getFilename();
    return () -> {
      Document document = assertDoesNotThrow(() -> parse(template), filename);
      assertEquals(
          XHTML_NAMESPACE,
          document.getDocumentElement().getNamespaceURI(),
          filename + " must declare the XHTML namespace"
      );
    };
  }

  private Document parse(Resource template) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

    try (InputStream input = template.getInputStream()) {
      return factory.newDocumentBuilder().parse(input);
    }
  }
}
