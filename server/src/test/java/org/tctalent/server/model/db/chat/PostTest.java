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

package org.tctalent.server.model.db.chat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.LinkPreview;

class PostTest {

  @Test
  void constructorInitializesLinkPreviewsToEmptyList() {
    Post post = new Post();

    assertNotNull(post.getLinkPreviews());
    assertTrue(post.getLinkPreviews().isEmpty());
  }

  @Test
  void setContentStoresSanitizedContent() {
    Post post = new Post();

    post.setContent("""
        <p>Hello <script>alert('xss')</script>
        <a href="https://example.com" target="_blank" rel="noopener">link</a></p>
        """);

    assertTrue(post.getContent().contains("Hello"));
    assertTrue(post.getContent().contains("https://example.com"));
    assertTrue(post.getContent().contains("target=\"_blank\""));
    assertTrue(post.getContent().contains("rel=\"noopener\""));
    assertFalse(post.getContent().contains("<script>"));
    assertFalse(post.getContent().contains("alert('xss')"));
  }

  @Test
  void setContentAllowsNull() {
    Post post = new Post();

    post.setContent(null);

    assertNull(post.getContent());
  }

  @Test
  void setLinkPreviewsStoresProvidedList() {
    Post post = new Post();
    List<LinkPreview> linkPreviews = new ArrayList<>();

    post.setLinkPreviews(linkPreviews);

    assertSame(linkPreviews, post.getLinkPreviews());
  }
}