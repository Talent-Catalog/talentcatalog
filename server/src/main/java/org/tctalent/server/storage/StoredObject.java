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
package org.tctalent.server.storage;

import lombok.Getter;
import lombok.Setter;

/**
 * Metadata about a stored object
 *
 * @author John Cameron
 */
@Getter
@Setter
public class StoredObject {

    /**
     * Opaque internal S3 key.
     * Example: /55/0e/550e8400e29b41d4a716446655440000
     */
    private String storageKey;

    /**
     * Bucket actually used.
     */
    private String bucket;

    /**
     * Original filename as metadata only.
     */
    private String originalFilename;

    /**
     * MIME type.
     */
    private String contentType;

    /**
     * Stored object length in bytes.
     */
    private Long contentLength;

    /**
     * SHA-256 checksum if known/computed.
     */
    private String sha256Hex;

}

