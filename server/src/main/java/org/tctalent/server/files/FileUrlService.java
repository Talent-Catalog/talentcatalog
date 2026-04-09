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

import java.time.Duration;
import org.tctalent.server.model.db.CandidateAttachment;

public interface FileUrlService {

    /**
     * Creates the user-facing application URL for an attachment.
     * <p>
     * Example:
     * </p>
     * <ul>
     *   <li>/files/[publicId]/cv.pdf</li>
     * </ul>
     */
    String createApplicationUrl(CandidateAttachment attachment);

    /**
     * Friendly share URL that expires.
     * <p>
     * Example:
     * </p>
     * <ul>
     *   <li>/files/[publicId]/cv.pdf?e=1774152000&t=...</li>
     * </ul>
     */
    String createExpiringApplicationUrl(CandidateAttachment attachment, Duration duration);

    /**
     * Creates the direct CloudFront object URL for an attachment's opaque storage key.
     * <p>
     * Example:
     * </p>
     * <ul>
     *   <li>/o/a7/3f/550e8400e29b41d4a716446655440000</li>
     * </ul>
     */
    String createObjectUrl(CandidateAttachment attachment);

    /**
     * Creates a signed CloudFront URL for a protected attachment.
     * @throws Exception if there is an error generating the signed URL
     */
    String createSignedObjectUrl(CandidateAttachment attachment, Duration duration)
        throws Exception;

    /**
     * Resolves the final access URL for this attachment.
     * <p>
     * Public files return an unsigned object URL.
     * Protected files return a signed object URL.
     * </p>
     */
    FinalFileAccessUrl createAccessUrl(CandidateAttachment attachment) throws Exception;
}
