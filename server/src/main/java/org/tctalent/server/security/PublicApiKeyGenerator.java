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

import java.security.SecureRandom;
import java.util.Base64;

/**
 * A utility class that generates a random API key for use in the authentication process.
 * <p/>
 * The generateApiKey method creates a 256-bit random byte array and encodes it using Base64
 * encoding. The resulting string is a 44-character long API key that can be used to authenticate
 * requests.
 * <p/>
 * The SecureRandom class provides a cryptographically strong random number generator (RNG) that
 * generates random bytes suitable for creating secret keys. The Base64 class provides encoding
 * schemes for converting binary data to printable characters and vice versa.
 *
 * @see <a href="https://stackoverflow.com/questions/14412132/whats-the-best-approach-for-generating-a-new-api-key">
 * @author sadatmalik
 */
public class PublicApiKeyGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public static String generateApiKey() {
        byte[] randomBytes = new byte[32]; // 256 bits for strong entropy
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
