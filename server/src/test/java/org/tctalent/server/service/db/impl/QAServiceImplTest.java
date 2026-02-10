/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class QAServiceImplTest {

  private QAServiceImpl qaService;

  @BeforeEach
  void setUp() {
    qaService = new QAServiceImpl();
    qaService.initialize();
  }

  @Test
  void loadQAContext_formatsQuestionsAndAnswers() {
    String context = qaService.loadQAContext();

    assertTrue(context.contains("Q: "));
    assertTrue(context.contains("A: "));
  }

  @Test
  void loadQAContext_returnsCachedValueOnSubsequentCalls() {
    String first = qaService.loadQAContext();
    String second = qaService.loadQAContext();

    assertSame(first, second);
  }

  @Test
  void loadQAContext_whenFileInvalid_returnsEmptyString() throws IOException {
    ClassPathResource resource = new ClassPathResource("static/pdf/chatbotQAFile.json");
    Path filePath = resource.getFile().toPath();
    String originalContent = Files.readString(filePath);

    try {
      Files.writeString(filePath, "invalid json", StandardOpenOption.TRUNCATE_EXISTING);

      QAServiceImpl failingService = new QAServiceImpl();
      String context = failingService.loadQAContext();

      assertEquals("", context);
    } finally {
      Files.writeString(filePath, originalContent, StandardOpenOption.TRUNCATE_EXISTING);
    }
  }
}

