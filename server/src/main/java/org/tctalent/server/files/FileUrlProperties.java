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

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.files") //TODO JC Where is this in application.yml?
public class FileUrlProperties {

    private String publicBaseUrl;
    private String cloudFrontBaseUrl;

    private String keyPairId;
    private String privateKeyPemPath;

    /**
     * Signed URLs expire after this many minutes.
     * <p>
     * Useful for restricting the validity of links to sensitive documents - eg passports     
     */
    private long signedUrlMinutes = 15;

    // --- Override only where needed ---

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = normalizeUrl(publicBaseUrl);
    }

    public void setCloudFrontBaseUrl(String cloudFrontBaseUrl) {
        this.cloudFrontBaseUrl = normalizeUrl(cloudFrontBaseUrl);
    }

    // --- Helper ---

    private String normalizeUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String trimmed = value.trim();

        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        return trimmed;
    }
}
