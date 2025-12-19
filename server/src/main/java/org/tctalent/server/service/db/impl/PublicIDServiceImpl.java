/*
 * Copyright (c) 2024 Talent Catalog.
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

import java.util.Base64;
import java.util.UUID;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.service.db.PublicIDService;

/**
 * Implements PublicIDService based on Base64 representation of randomly generated UUIDs.
 */
@Service
public class PublicIDServiceImpl implements PublicIDService {

    @Override
    public String generatePublicID() {
        // Generate a random UUID
        UUID uuid = UUID.randomUUID();

        // Encode the UUID to Base64
        return encodeToBase64(uuid);
    }

    /**
     * Convert UUID to Base64
     * @param uuid UUID to convert
     * @return Base64 String
     */
    private String encodeToBase64(UUID uuid) {
        byte[] uuidBytes = toBytes(uuid);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuidBytes);
    }

    /**
     * Convert UUID to byte array
     * @param uuid UUID
     * @return bytes in UUID
     */
    private byte[] toBytes(@NonNull UUID uuid) {
        byte[] bytes = new byte[16];
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();

        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (mostSigBits >>> (8 * (7 - i)));
            bytes[8 + i] = (byte) (leastSigBits >>> (8 * (7 - i)));
        }
        return bytes;
    }

}
