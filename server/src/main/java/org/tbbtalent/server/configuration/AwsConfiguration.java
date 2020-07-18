/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbbtalent.server.service.aws.S3ResourceHelper;

@Configuration
public class AwsConfiguration {
    
    @Value("${aws.s3.region}")
    private String s3Region;

    @Value("${aws.credentials.accessKey}")
    private String accessKey;

    @Value("${aws.credentials.secretKey}")
    private String secretKey;

    @Bean
    public S3ResourceHelper s3ResourceHelper() {
        return new S3ResourceHelper(accessKey, secretKey, s3Region);
    }


}
