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
 * AWS S3 configuration
 *
 * @author John Cameron
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "s3")
public class S3Properties {

    /**
     * AWS access key
     */
    private String accessKey;

    /**
     * AWS secret key
     */
    private String secretKey;

    /**
     * Max file size
     */
    private long maxSize;

    /**
     * Temporary upload folder. eg temp
     */
    private String uploadFolder;

    /**
     * AWS region - eg us-east-1
     */
    private String region;

    /**
     * S3 bucket for candidate files storage.
     * Example: candidate-files.globalrefugee.net
     */
    private String candidateFilesBucket;

    /**
     * Bucket used for translations and old attachments
     */
    private String otherFilesBucket;
}
