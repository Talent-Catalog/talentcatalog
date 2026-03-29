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

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class CandidateFilesController {

    private final AttachmentAccessService attachmentAccessService;

    @GetMapping("/{attachmentId}/{filename:.+}")
    public ResponseEntity<Void> getFile(
        @PathVariable long attachmentId,
        @PathVariable String filename,
        @RequestParam(required = false) Long e,
        @RequestParam(required = false) String t) throws Exception {

        FinalFileAccessUrl accessUrl = attachmentAccessService.resolveAccessUrl(
            attachmentId, filename, e, t);

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(accessUrl.getUrl()))
            .build();
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<Void> getFileWithoutFilename(
        @PathVariable long attachmentId,
        @RequestParam(required = false) Long e,
        @RequestParam(required = false) String t) throws Exception {

        FinalFileAccessUrl accessUrl = attachmentAccessService.resolveAccessUrl(
            attachmentId, null, e, t);

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(accessUrl.getUrl()))
            .build();
    }
}
