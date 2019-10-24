package org.tbbtalent.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbbtalent.server.service.aws.S3ResourceHelper;

@Configuration
public class AwsConfiguration {
    
    @Value("${s3.region.name}")
    private String s3Region;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Bean
    public S3ResourceHelper s3ResourceHelper() {
        return new S3ResourceHelper(accessKey, secretKey, s3Region);
    }


}
