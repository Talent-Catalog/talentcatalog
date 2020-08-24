package org.tbbtalent.server.service.db.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.tbbtalent.server.service.db.aws.S3ResourceHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class updateS3MimeTypeTest {

    @Autowired
    private S3ResourceHelper s3ResourceHelper;

    private TransferManager transferManager;

    @Value("${aws.credentials.accessKey}") String accessKey;
    @Value("${aws.credentials.secretKey}") String secretKey;
    @Value("${aws.s3.region}") String s3Region;
    private AmazonS3 amazonS3;

//    @BeforeEach
//    void initCase() {
//        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
//        amazonS3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
//                .withRegion(s3Region).build();
//        transferManager = TransferManagerBuilder.standard().withS3Client(amazonS3).build();
//    }


    @Test
    void regexForPrefix() {
        String test = "candidate/9";
    }

    @Test
    void listBuckets() {
        s3ResourceHelper.getObjectsListed();
    }

    //@Test
    void getS3FileData() {
        ListObjectsRequest req = new ListObjectsRequest();

        List<S3ObjectSummary> contents = amazonS3.listObjects(req).getObjectSummaries();
//        final ObjectMetadata metadata = new ObjectMetadata();
//        metadata.addUserMetadata(metadataKey, value);
//        final CopyObjectRequest request = new CopyObjectRequest(bucketName, keyName, bucketName, keyName)
//                .withSourceBucketName(bucketName)
//                .withSourceKey(keyName)
//                .withNewObjectMetadata(metadata);
//
//        s3.copyObject(request);

//        for (S3ObjectSummary summary : objs.getObjectSummaries() )
//        {
//            String key = summary.getKey();
//            if (! key.endsWith(".gz"))
//                continue;
//
//            ObjectMetadata metadata = new ObjectMetadata();
//            metadata.addUserMetadata("Content-Encoding", "gzip");
//            metadata.addUserMetadata("Content-Type", "application/x-gzip");
//            final CopyObjectRequest request = new CopyObjectRequest(bucket, key, bucket, key)
//                    .withSourceBucketName( bucket )
//                    .withSourceKey(key)
//                    .withNewObjectMetadata(metadata);
//
//            s3.copyObject(request);
    }

}
