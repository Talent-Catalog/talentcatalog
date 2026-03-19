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

import java.io.InputStream;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to store a file.
 *
 * @author John Cameron
 */
@Getter
@Setter
public class StoragePutRequest {

    /**
     * Original client filename, used only for metadata/debugging.
     * Not used as the S3 key.
     */
    private String originalFilename;

    /**
     * MIME type, eg application/pdf.
     */
    private String contentType;

    /**
     * Content length if known.
     */
    private Long contentLength;

    /**
     * Input stream for the content.
     */
    private InputStream inputStream;

    /**
     * Optional checksum supplied by caller.
     */
    private String sha256Hex;
}
