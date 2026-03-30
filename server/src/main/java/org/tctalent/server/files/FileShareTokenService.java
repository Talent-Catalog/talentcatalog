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
package org.tctalent.server.files;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.configuration.properties.CandidateFileUrlsProperties;

@Service
@RequiredArgsConstructor
public class FileShareTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final CandidateFileUrlsProperties properties;

    public String createToken(String publicId, String filename, long expiresAtEpochSeconds) {
        String payload = buildPayload(publicId, filename, expiresAtEpochSeconds);
        return hmacBase64Url(payload);
    }

    public void validateToken(String publicId, String filename, long expiresAtEpochSeconds, String token) {
        if (Instant.now().getEpochSecond() > expiresAtEpochSeconds) {
            throw new InvalidFileShareTokenException("File share link has expired");
        }

        String expected = createToken(publicId, filename, expiresAtEpochSeconds);
        if (!constantTimeEquals(expected, token)) {
            throw new InvalidFileShareTokenException("Invalid file share token");
        }
    }

    private String buildPayload(String publicId, String filename, long expiresAtEpochSeconds) {
        return publicId + "|" + filename + "|" + expiresAtEpochSeconds;
    }

    private String hmacBase64Url(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            String shareSecret = properties.getShareSecretKey();
            mac.init(new SecretKeySpec(shareSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create file share token", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
