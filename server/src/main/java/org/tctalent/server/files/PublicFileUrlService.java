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

import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.CandidateAttachment;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Service
public class PublicFileUrlService {

    private final FileUrlProperties properties;

    public PublicFileUrlService(FileUrlProperties properties) {
        this.properties = properties;
    }

    public String toPublicUrl(CandidateAttachment attachment) {
        requirePublicPath(attachment);
        return properties.getPublicBaseUrl() + "/" + stripLeadingSlash(attachment.getPublicPath());
    }

    public String toCloudFrontResourceUrl(CandidateAttachment attachment) {
        requirePublicPath(attachment);
        return properties.getCloudFrontBaseUrl() + "/" + stripLeadingSlash(attachment.getPublicPath());
    }

    private void requirePublicPath(CandidateAttachment attachment) {
        if (attachment.getPublicPath() == null || attachment.getPublicPath().isBlank()) {
            throw new IllegalStateException("Attachment has no publicPath");
        }
    }

    private String stripLeadingSlash(String value) {
        return value.startsWith("/") ? value.substring(1) : value;
    }
}
