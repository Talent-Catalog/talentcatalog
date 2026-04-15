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

package org.tctalent.server.api.pub;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.files.AttachmentAccessService;
import org.tctalent.server.files.FinalFileAccessUrl;

/**
 * Controller for serving files through their public access URLs.
 * <p>
 * The incoming public url contains the public attachment id which is used to look up the 
 * attachment info on the Postgres database and then generate a url to the file on
 * AWS S3 storage. The controller then redirects the user to that url - which will be served
 * directly by S3 (via Cloudfront).
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class CandidateFilesController {

    private final AttachmentAccessService attachmentAccessService;

    /**
     * Resolves the public file access URL based on the provided attachment ID and filename, 
     * and redirects the client to the resolved URL.
     * The method supports optional parameters for secure and time-limited access.
     *
     * @param publicAttachmentId the unique identifier of the public attachment 
     *                           used to locate the file in the storage system.
     * @param filename           the name of the file to be resolved and accessed.
     * @param e                  optional expiration time for access, represented as 
     *                           a Unix epoch timestamp in seconds.
     * @param t                  optional security token for validating the file access request.
     * @return a {@link ResponseEntity} that redirects the client to the resolved file URL 
     *         with HTTP status 302 (Found).
     * @throws Exception if the file access resolution fails due to invalid input, permissions, 
     *                   or other service-related issues.
     */
    @GetMapping("/{publicAttachmentId}/{filename:.+}")
    public ResponseEntity<Void> getFile(
        @PathVariable String publicAttachmentId,
        @PathVariable String filename,
        @RequestParam(required = false) Long e,
        @RequestParam(required = false) String t) throws Exception {

        //Calculate final access URL to S3 storage.
        FinalFileAccessUrl accessUrl = attachmentAccessService.resolveAccessUrl(
            publicAttachmentId, filename, e, t);

        //Redirect user to the S3 storage url
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(accessUrl.getUrl()))
            .build();
    }

    /**
     * Resolves and redirects to the file's public access URL based on the provided attachment ID.
     * This method does not require the filename as part of the path and supports optional expiration 
     * and token parameters for secure access.
     *
     * @param publicAttachmentId the unique identifier of the public attachment 
     *                           used to locate the file in the storage system.
     * @param e                  optional expiration time for access, represented as 
     *                           a Unix epoch timestamp in seconds.
     * @param t                  optional security token for validating the file access request.
     * @return a {@link ResponseEntity} that redirects the client to the resolved file URL 
     *         with HTTP status 302 (Found).
     * @throws Exception if the file access resolution fails due to invalid input, permissions, 
     *                   or other service-related issues.
     */
    @GetMapping("/{publicAttachmentId}")
    public ResponseEntity<Void> getFileWithoutFilename(
        @PathVariable String publicAttachmentId,
        @RequestParam(required = false) Long e,
        @RequestParam(required = false) String t) throws Exception {

        FinalFileAccessUrl accessUrl = attachmentAccessService.resolveAccessUrl(
            publicAttachmentId, null, e, t);

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(accessUrl.getUrl()))
            .build();
    }
}
