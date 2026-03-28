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

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
/**
 * Url for accessing a stored file.
 * @author John Cameron
 */
@Getter
@Builder
public class FileAccessUrl {

    /**
     * Final URL to redirect the browser to.
     */
    private final String url;

    /**
     * Whether the returned URL is signed and expiring.
     */
    private final boolean signed;

    /**
     * Expiry time for signed URLs. Null for public URLs.
     */
    private final Instant expiresAt;
}

