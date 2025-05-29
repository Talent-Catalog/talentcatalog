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

package org.tctalent.server.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PublicApiKeyGeneratorTest {

  @Test
  void generateApiKey_producesKeyOfCorrectLength() {
    String apiKey = PublicApiKeyGenerator.generateApiKey();

    assertEquals(43, apiKey.length(), "API key should be 44 characters long (Base64 URL-safe encoding of 32 bytes)");
  }

  @Test
  void generateApiKey_producesValidBase64UrlSafeKey() {
    String apiKey = PublicApiKeyGenerator.generateApiKey();

    assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(apiKey), "API key should be valid Base64 URL-safe encoded");
    assertTrue(apiKey.matches("^[A-Za-z0-9-_]+$"), "API key should only contain Base64 URL-safe characters (A-Z, a-z, 0-9, -, _)");
    assertFalse(apiKey.contains("="), "API key should not contain padding characters");
  }

  @Test
  void generateApiKey_produces32ByteKey() {
    String apiKey = PublicApiKeyGenerator.generateApiKey();

    byte[] decodedBytes = Base64.getUrlDecoder().decode(apiKey);
    assertEquals(32, decodedBytes.length, "Decoded API key should be 32 bytes (256 bits)");
  }

  @Test
  void generateApiKey_producesUniqueKeys() {
    Set<String> apiKeys = new HashSet<>();
    int iterations = 1000;

    for (int i = 0; i < iterations; i++) {
      String apiKey = PublicApiKeyGenerator.generateApiKey();
      assertTrue(apiKeys.add(apiKey), "API key should be unique");
    }

    assertEquals(iterations, apiKeys.size(), "All generated API keys should be unique");
  }

  @Test
  void generateApiKey_isNotNullOrEmpty() {
    String apiKey = PublicApiKeyGenerator.generateApiKey();

    assertNotNull(apiKey, "API key should not be null");
    assertFalse(apiKey.isEmpty(), "API key should not be empty");
  }
}