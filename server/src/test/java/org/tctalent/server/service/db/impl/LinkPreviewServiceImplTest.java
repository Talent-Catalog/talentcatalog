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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.model.db.LinkPreview;
import org.tctalent.server.repository.db.LinkPreviewRepository;

class LinkPreviewServiceImplTest {

  private static final String PAGE_URL = "https://www.requested.example/posts/1";
  private static final String OG_IMAGE_URL = "https://cdn.example.com/og-image.png";
  private static final String FAVICON_URL = "https://cdn.example.com/favicon.ico";
  private static final String QUALIFIED_IMAGE_URL = "https://cdn.example.com/qualified.png";
  private static final String META_FAVICON_URL = "https://cdn.example.com/meta-favicon.png";
  private static final String BAD_IMAGE_URL = "https://cdn.example.com/missing.png";
  private static final String GOOD_IMAGE_URL = "https://cdn.example.com/good.png";

  private LinkPreviewRepository linkPreviewRepository;
  private LinkPreviewServiceImpl service;

  @BeforeEach
  void setUp() {
    linkPreviewRepository = mock(LinkPreviewRepository.class);
    service = new LinkPreviewServiceImpl(linkPreviewRepository);
  }

  @Test
  void deleteLinkPreview_deletesByIdAndReturnsTrue() throws Exception {
    boolean result = service.deleteLinkPreview(42L);

    assertTrue(result);
    verify(linkPreviewRepository).deleteById(42L);
  }

  @Test
  void deleteLinkPreview_returnsFalseWhenEntityReferencedExceptionIsThrown() throws Exception {
    doThrow(new EntityReferencedException("link preview"))
        .when(linkPreviewRepository)
        .deleteById(42L);

    boolean result = service.deleteLinkPreview(42L);

    assertFalse(result);
    verify(linkPreviewRepository).deleteById(42L);
  }

  @Test
  void deleteLinkPreview_returnsFalseWhenInvalidRequestExceptionIsThrown() throws Exception {
    doThrow(new InvalidRequestException("Invalid link preview"))
        .when(linkPreviewRepository)
        .deleteById(42L);

    boolean result = service.deleteLinkPreview(42L);

    assertFalse(result);
    verify(linkPreviewRepository).deleteById(42L);
  }

  @Test
  void buildLinkPreview_buildsPreviewFromPreferredMetadata() throws IOException {
    String html = """
      <html>
        <head>
          <title>Document title</title>
          <link rel="canonical" href="https://www.canonical.example/articles/123">
          <meta property="og:description" content="Open Graph description">
          <meta property="og:image" content="%s">
          <link rel="icon" href="%s">
        </head>
        <body>
          <h1>Fallback h1</h1>
          <p>Fallback paragraph</p>
        </body>
      </html>
      """.formatted(OG_IMAGE_URL, FAVICON_URL);

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);
    Connection ogImageConnection = imageConnectionWithStatus(200);
    Connection faviconConnection = imageConnectionWithStatus(200);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);
      jsoup.when(() -> Jsoup.connect(OG_IMAGE_URL)).thenReturn(ogImageConnection);
      jsoup.when(() -> Jsoup.connect(FAVICON_URL)).thenReturn(faviconConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertAll(
          () -> assertEquals(PAGE_URL, result.getUrl()),
          () -> assertEquals("canonical.example", result.getDomain()),
          () -> assertEquals("Document title", result.getTitle()),
          () -> assertEquals("Open Graph description", result.getDescription()),
          () -> assertEquals(OG_IMAGE_URL, result.getImageUrl()),
          () -> assertEquals(FAVICON_URL, result.getFaviconUrl())
      );
    }
  }
  @Test
  void buildLinkPreview_usesFallbackMetadataWhenPreferredMetadataIsMissing() throws IOException {
    String html = """
      <html>
        <head>
          <meta property="og:url" content="https://www.og-domain.example/posts/123">
          <meta itemprop="image" content="%s">
        </head>
        <body>
          <h1>Fallback heading</h1>
          <p>Fallback paragraph description</p>
          <img src="https://cdn.example.com/tiny.png" width="40" height="40">
          <img src="https://cdn.example.com/wide.png" width="400" height="100">
          <img src="%s" width="120px" height="80px">
        </body>
      </html>
      """.formatted(META_FAVICON_URL, QUALIFIED_IMAGE_URL);

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);
    Connection qualifiedImageConnection = imageConnectionWithStatus(200);
    Connection metaFaviconConnection = imageConnectionWithStatus(200);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);
      jsoup.when(() -> Jsoup.connect(QUALIFIED_IMAGE_URL)).thenReturn(qualifiedImageConnection);
      jsoup.when(() -> Jsoup.connect(META_FAVICON_URL)).thenReturn(metaFaviconConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertAll(
          () -> assertEquals("og-domain.example", result.getDomain()),
          () -> assertEquals("Fallback heading", result.getTitle()),
          () -> assertEquals("Fallback paragraph description", result.getDescription()),
          () -> assertEquals(QUALIFIED_IMAGE_URL, result.getImageUrl()),
          () -> assertEquals(META_FAVICON_URL, result.getFaviconUrl())
      );
    }
  }

  @Test
  void buildLinkPreview_skipsInaccessibleImageAndUsesNextAccessibleImage() throws IOException {
    String html = """
      <html>
        <head>
          <title>Image fallback test</title>
          <meta property="og:image" content="%s">
          <meta property="og:image" content="%s">
        </head>
      </html>
      """.formatted(BAD_IMAGE_URL, GOOD_IMAGE_URL);

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);
    Connection badImageConnection = imageConnectionWithStatus(404);
    Connection goodImageConnection = imageConnectionWithStatus(200);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);
      jsoup.when(() -> Jsoup.connect(BAD_IMAGE_URL)).thenReturn(badImageConnection);
      jsoup.when(() -> Jsoup.connect(GOOD_IMAGE_URL)).thenReturn(goodImageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertEquals(GOOD_IMAGE_URL, result.getImageUrl());
    }
  }

  @Test
  void buildLinkPreview_returnsNullWhenNoDomainCanBeResolved() throws IOException {
    String invalidUrl = "not-a-valid-url";
    Document document = Jsoup.parse("""
      <html>
        <head>
          <title>No domain</title>
        </head>
      </html>
      """);

    Connection pageConnection = pageConnection(document);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(invalidUrl)).thenReturn(pageConnection);

      LinkPreview result = service.buildLinkPreview(invalidUrl);

      assertNull(result);
    }
  }
  @Test
  void buildLinkPreview_returnsNullWhenDocumentCannotBeFetched() throws IOException {
    Connection connection = mock(Connection.class, RETURNS_SELF);
    when(connection.get()).thenThrow(new IOException("Could not fetch page"));

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(connection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNull(result);
    }
  }


  private Connection pageConnection(Document document) throws IOException {
    Connection connection = mock(Connection.class, RETURNS_SELF);
    when(connection.get()).thenReturn(document);
    return connection;
  }

  private Connection imageConnectionWithStatus(int statusCode) throws IOException {
    Connection.Response response = mock(Connection.Response.class);
    when(response.statusCode()).thenReturn(statusCode);

    Connection connection = mock(Connection.class, RETURNS_SELF);
    when(connection.execute()).thenReturn(response);
    return connection;
  }

  private Connection imageConnectionThrowingIOException() throws IOException {
    Connection connection = mock(Connection.class, RETURNS_SELF);
    when(connection.execute()).thenThrow(new IOException("Image could not be fetched"));
    return connection;
  }

  @Test
  void buildLinkPreview_usesOgTitleWhenDocumentTitleIsMissing() throws IOException {
    String html = """
      <html>
        <head>
          <meta property="og:title" content="Open Graph title">
        </head>
      </html>
      """;

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertEquals("Open Graph title", result.getTitle());
      assertEquals("requested.example", result.getDomain());
    }
  }

  @Test
  void buildLinkPreview_usesTwitterTitleWhenDocumentAndOgTitlesAreMissing() throws IOException {
    String html = """
      <html>
        <head>
          <meta name="twitter:title" content="Twitter title">
        </head>
      </html>
      """;

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertEquals("Twitter title", result.getTitle());
    }
  }

  @Test
  void buildLinkPreview_usesH2TitleWhenOtherTitlesAreMissing() throws IOException {
    String html = """
      <html>
        <body>
          <h2>Heading two title</h2>
        </body>
      </html>
      """;

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertEquals("Heading two title", result.getTitle());
    }
  }

  @Test
  void buildLinkPreview_setsNullTitleWhenNoTitleFallbackExists() throws IOException {
    String html = """
      <html>
        <head></head>
        <body></body>
      </html>
      """;

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertNull(result.getTitle());
    }
  }

  @Test
  void buildLinkPreview_usesTwitterDescriptionWhenOgDescriptionIsMissing() throws IOException {
    String html = """
      <html>
        <head>
          <meta name="twitter:description" content="Twitter description">
        </head>
      </html>
      """;

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertEquals("Twitter description", result.getDescription());
    }
  }

  @Test
  void buildLinkPreview_usesMetaDescriptionWhenSocialDescriptionsAreMissing() throws IOException {
    String html = """
      <html>
        <head>
          <meta name="description" content="Meta description">
        </head>
      </html>
      """;

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertEquals("Meta description", result.getDescription());
    }
  }

  @Test
  void buildLinkPreview_setsNullDescriptionWhenNoDescriptionFallbackExists() throws IOException {
    String html = """
      <html>
        <body></body>
      </html>
      """;

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertNull(result.getDescription());
    }
  }

  @Test
  void buildLinkPreview_usesImageSrcLinkWhenOgImageIsMissing() throws IOException {
    String imageSrcUrl = "https://cdn.example.com/image-src.png";
    String html = """
      <html>
        <head>
          <link rel="image_src" href="%s">
        </head>
      </html>
      """.formatted(imageSrcUrl);

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);
    Connection imageConnection = imageConnectionWithStatus(200);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);
      jsoup.when(() -> Jsoup.connect(imageSrcUrl)).thenReturn(imageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertEquals(imageSrcUrl, result.getImageUrl());
    }
  }

  @Test
  void buildLinkPreview_usesTwitterImageWhenOgAndImageSrcAreMissing() throws IOException {
    String twitterImageUrl = "https://cdn.example.com/twitter-image.png";
    String html = """
      <html>
        <head>
          <meta name="twitter:image" content="%s">
        </head>
      </html>
      """.formatted(twitterImageUrl);

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);
    Connection imageConnection = imageConnectionWithStatus(200);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);
      jsoup.when(() -> Jsoup.connect(twitterImageUrl)).thenReturn(imageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertEquals(twitterImageUrl, result.getImageUrl());
    }
  }

  @Test
  void buildLinkPreview_setsNullImageWhenImagesAreMissingOrUnusable() throws IOException {
    String html = """
      <html>
        <body>
          <img src="https://cdn.example.com/no-size.png">
          <img src="https://cdn.example.com/tiny.png" width="40" height="40">
          <img src="https://cdn.example.com/too-wide.png" width="400" height="100">
          <img src="/relative.png" width="120" height="120">
        </body>
      </html>
      """;

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertNull(result.getImageUrl());
    }
  }

  @Test
  void buildLinkPreview_skipsImageWhenAccessibilityCheckThrowsIOException() throws IOException {
    String inaccessibleImageUrl = "https://cdn.example.com/throws.png";
    String html = """
      <html>
        <head>
          <meta property="og:image" content="%s">
        </head>
      </html>
      """.formatted(inaccessibleImageUrl);

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);
    Connection inaccessibleImageConnection = imageConnectionThrowingIOException();

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);
      jsoup.when(() -> Jsoup.connect(inaccessibleImageUrl)).thenReturn(inaccessibleImageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertNull(result.getImageUrl());
    }
  }

  @Test
  void buildLinkPreview_setsNullFaviconWhenFaviconIsInaccessible() throws IOException {
    String inaccessibleFaviconUrl = "https://cdn.example.com/favicon.png";
    String html = """
      <html>
        <head>
          <link rel="icon" href="%s">
        </head>
      </html>
      """.formatted(inaccessibleFaviconUrl);

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);
    Connection faviconConnection = imageConnectionWithStatus(500);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);
      jsoup.when(() -> Jsoup.connect(inaccessibleFaviconUrl)).thenReturn(faviconConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertNull(result.getFaviconUrl());
    }
  }

  @Test
  void buildLinkPreview_setsNullFaviconWhenNoFaviconExists() throws IOException {
    String html = """
      <html>
        <head></head>
      </html>
      """;

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertNull(result.getFaviconUrl());
    }
  }

  @Test
  void buildLinkPreview_fallsBackToRequestedUrlDomainWhenCanonicalIsInvalid() throws IOException {
    String html = """
      <html>
        <head>
          <link rel="canonical" href="not-a-valid-url">
        </head>
      </html>
      """;

    Document document = Jsoup.parse(html, PAGE_URL);
    Connection pageConnection = pageConnection(document);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(pageConnection);

      LinkPreview result = service.buildLinkPreview(PAGE_URL);

      assertNotNull(result);
      assertEquals("requested.example", result.getDomain());
    }
  }
}