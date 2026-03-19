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

/**
 * Url for accessing a stored file.
 *
 * @param url the url for accessing the file
 * @param signed whether the url is signed
 * @param expiresAt when the url expires
 * @author John Cameron
 */
public record FileAccessUrl(
    String url,
    boolean signed,
    Instant expiresAt
) {
}
