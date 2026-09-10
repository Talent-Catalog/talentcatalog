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

package org.tctalent.server.files;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.configuration.properties.CandidateFileUrlsProperties;

@ExtendWith(MockitoExtension.class)
class FileShareTokenServiceTest {

  private static final String SECRET = "test-share-secret";
  private static final String PUBLIC_ID = "public-123";
  private static final String FILENAME = "candidate.pdf";
  private static final long FIXED_EXPIRY = 1_800_000_000L;

  private CandidateFileUrlsProperties properties;
  private FileShareTokenService service;

  @BeforeEach
  void setUp() {
    properties = new CandidateFileUrlsProperties();
    properties.setShareSecretKey(SECRET);

    service = new FileShareTokenService(properties);
  }

  @Test
  void createTokenReturnsExpectedHmacSha256Token() {
    String result = service.createToken(
        PUBLIC_ID,
        FILENAME,
        FIXED_EXPIRY
    );

    assertEquals(
        "WTtDRuGRkKlJhyRyaH7WTShYwnYewoTmZvLwmOumenU",
        result
    );
  }

  @Test
  void createTokenIsDeterministic() {
    String first = service.createToken(
        PUBLIC_ID,
        FILENAME,
        FIXED_EXPIRY
    );

    String second = service.createToken(
        PUBLIC_ID,
        FILENAME,
        FIXED_EXPIRY
    );

    assertEquals(first, second);
  }

  @Test
  void createTokenChangesWhenPayloadChanges() {
    String original = service.createToken(
        PUBLIC_ID,
        FILENAME,
        FIXED_EXPIRY
    );

    String differentPublicId = service.createToken(
        "different-public-id",
        FILENAME,
        FIXED_EXPIRY
    );

    String differentFilename = service.createToken(
        PUBLIC_ID,
        "different.pdf",
        FIXED_EXPIRY
    );

    String differentExpiry = service.createToken(
        PUBLIC_ID,
        FILENAME,
        FIXED_EXPIRY + 1
    );

    assertAll(
        () -> assertNotEquals(original, differentPublicId),
        () -> assertNotEquals(original, differentFilename),
        () -> assertNotEquals(original, differentExpiry)
    );
  }

  @Test
  void validateTokenAcceptsValidUnexpiredToken() {
    long expiry =
        Instant.now().plusSeconds(3_600).getEpochSecond();

    String token = service.createToken(
        PUBLIC_ID,
        FILENAME,
        expiry
    );

    assertDoesNotThrow(
        () -> service.validateToken(
            PUBLIC_ID,
            FILENAME,
            expiry,
            token
        )
    );
  }

  @Test
  void validateTokenRejectsExpiredToken() {
    long expiry =
        Instant.now().minusSeconds(1).getEpochSecond();

    String token = service.createToken(
        PUBLIC_ID,
        FILENAME,
        expiry
    );

    InvalidFileShareTokenException exception = assertThrows(
        InvalidFileShareTokenException.class,
        () -> service.validateToken(
            PUBLIC_ID,
            FILENAME,
            expiry,
            token
        )
    );

    assertEquals(
        "File share link has expired",
        exception.getMessage()
    );
  }

  @Test
  void validateTokenRejectsIncorrectTokenWithSameLength() {
    long expiry =
        Instant.now().plusSeconds(3_600).getEpochSecond();

    String validToken = service.createToken(
        PUBLIC_ID,
        FILENAME,
        expiry
    );

    // Change one character while preserving the token length.
    String invalidToken =
        (validToken.charAt(0) == 'A' ? "B" : "A")
            + validToken.substring(1);

    InvalidFileShareTokenException exception = assertThrows(
        InvalidFileShareTokenException.class,
        () -> service.validateToken(
            PUBLIC_ID,
            FILENAME,
            expiry,
            invalidToken
        )
    );

    assertEquals(
        "Invalid file share token",
        exception.getMessage()
    );
  }

  @Test
  void validateTokenRejectsTokenWithDifferentLength() {
    long expiry =
        Instant.now().plusSeconds(3_600).getEpochSecond();

    InvalidFileShareTokenException exception = assertThrows(
        InvalidFileShareTokenException.class,
        () -> service.validateToken(
            PUBLIC_ID,
            FILENAME,
            expiry,
            "short-token"
        )
    );

    assertEquals(
        "Invalid file share token",
        exception.getMessage()
    );
  }

  @Test
  void validateTokenRejectsNullToken() {
    long expiry =
        Instant.now().plusSeconds(3_600).getEpochSecond();

    InvalidFileShareTokenException exception = assertThrows(
        InvalidFileShareTokenException.class,
        () -> service.validateToken(
            PUBLIC_ID,
            FILENAME,
            expiry,
            null
        )
    );

    assertEquals(
        "Invalid file share token",
        exception.getMessage()
    );
  }

  @Test
  void constantTimeEqualsCoversNullLengthAndContentBranches() {
    Boolean firstNull = ReflectionTestUtils.invokeMethod(
        service,
        "constantTimeEquals",
        new Object[]{null, "value"}
    );

    Boolean secondNull = ReflectionTestUtils.invokeMethod(
        service,
        "constantTimeEquals",
        "value",
        null
    );

    Boolean differentLengths = ReflectionTestUtils.invokeMethod(
        service,
        "constantTimeEquals",
        "short",
        "longer"
    );

    Boolean equalValues = ReflectionTestUtils.invokeMethod(
        service,
        "constantTimeEquals",
        "same-value",
        "same-value"
    );

    Boolean differentValues = ReflectionTestUtils.invokeMethod(
        service,
        "constantTimeEquals",
        "same-value",
        "same-Valuf"
    );

    Boolean emptyValues = ReflectionTestUtils.invokeMethod(
        service,
        "constantTimeEquals",
        "",
        ""
    );

    assertAll(
        () -> assertNotEquals(Boolean.TRUE, firstNull),
        () -> assertNotEquals(Boolean.TRUE, secondNull),
        () -> assertNotEquals(Boolean.TRUE, differentLengths),
        () -> assertEquals(Boolean.TRUE, equalValues),
        () -> assertNotEquals(Boolean.TRUE, differentValues),
        () -> assertEquals(Boolean.TRUE, emptyValues)
    );
  }

  @Test
  void createTokenWrapsCryptographicConfigurationFailure() {
    properties.setShareSecretKey(null);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> service.createToken(
            PUBLIC_ID,
            FILENAME,
            FIXED_EXPIRY
        )
    );

    assertAll(
        () -> assertEquals(
            "Unable to create file share token",
            exception.getMessage()
        ),
        () -> assertInstanceOf(NullPointerException.class, exception.getCause())
    );
  }
}