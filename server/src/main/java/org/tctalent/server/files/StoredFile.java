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

/**
 * This defines the data associated with a stored file.
 *
 * @author John Cameron
 */
public interface StoredFile {

    /**
     * Whether the file is considered active.
     * If inactive it will be ignored.
     */
    boolean isActive();
    
    /**
     * Opaque internal S3 key.
     */
    String getStorageKey();

    /**
     * Bucket name.
     */
    String getBucket();

    /**
     * Name of file.
     */
    String getName();

    /**
     * MIME type. aka Content-Type. For example: "application/pdf"
     */
    String getFileType();

    /**
     * Size in bytes.
     */
    Long getContentLength();

    /**
     * SHA-256 checksum.
     */
    String getSha256Hex();

    /**
     * Type of document.
     */
    UploadType getUploadType();
}
