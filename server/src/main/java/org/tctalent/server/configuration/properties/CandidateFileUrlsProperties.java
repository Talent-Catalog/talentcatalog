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

package org.tctalent.server.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration of candidate file url handling
 *
 * @author John Cameron
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "candidate-file-urls")
public class CandidateFileUrlsProperties {

    /**
     * Example: https://candidate-files.globalrefugee.net
     * Usually same as publicBaseUrl when CloudFront serves both public and signed paths.
     */
    private String originBaseUrl;

    /**
     * Example: https://candidate-files.globalrefugee.net
     */
    private String publicBaseUrl;
    
    /**
     * Key for signing shared pubic urls.
     */
    private String shareSecretKey;

    /**
     * Shared signed public urls expire after this many minutes
     */
    private long shareExpiryMinutes;

    /**
     * CloudFront public key ID, used in signed URLs.
     */
    private String cloudfrontKeyPairId;

    /**
     * PEM private key path or content source handled elsewhere.
     */
    private String cloudfrontPrivateKeyPemPath;

    /**
     * Signed origin URLs expire after this many minutes.
     * <p>
     * Useful for restricting the validity of links to sensitive documents - eg passports     
     */
    private long originExpiryMinutes = 15;

}
