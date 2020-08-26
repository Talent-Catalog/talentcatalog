package org.tbbtalent.server.service.db.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.*;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.exception.FileDownloadException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class S3ResourceHelper {
    private static final Logger log = LoggerFactory.getLogger(S3ResourceHelper.class);
    public static long FILE_PART_SIZE = 5 * 1024 * 1024; // 5MB

    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String s3Bucket;

    private TransferManager transferManager;

    @Autowired
    public S3ResourceHelper(@Value("${aws.credentials.accessKey}") String accessKey,
                            @Value("${aws.credentials.secretKey}") String secretKey,
                            @Value("${aws.s3.region}") String s3Region) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        amazonS3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(s3Region).build();
        transferManager = TransferManagerBuilder.standard().withS3Client(amazonS3).build();
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public File downloadFile(String key) throws FileDownloadException {
        return downloadFile(s3Bucket, key);
    }

    public File downloadFile(String bucket,
                             String key)
            throws FileDownloadException {
        File downloadFile = null;
        try {
            downloadFile = getTmpFile();
            Download download = transferManager.download(bucket, key, downloadFile);
            // blocking wait
            download.waitForCompletion();

            log.debug("downloaded key {} to {}", key, downloadFile.getAbsolutePath());

        } catch (Exception e) {
            log.error("Error downloading file to: " + key, e);
            throw new FileDownloadException("Error downloading file from: " + key, e);
        }
        return downloadFile;
    }

    public Resource downloadResource(String key) throws FileDownloadException {
        Resource resource = null;
        try {
            File downloadFile = getTmpFile();
            Download download = transferManager.download(s3Bucket, key, downloadFile);
            // blocking wait
            download.waitForCompletion();

            log.debug("downloaded key {} to {}", key, downloadFile.getAbsolutePath());

            // convert to resource
            resource = new FileSystemResource(downloadFile);
        } catch (Exception e) {
            log.error("Error downloading file to: " + key, e);
            throw new FileDownloadException("Error downloading file from: " + key, e);
        }
        return resource;
    }

    public Resource downloadResource(String key,
                                     File targetLocation)
            throws FileDownloadException {
        Resource resource = null;
        try {
            File downloadFile = targetLocation;
            Download download = transferManager.download(s3Bucket, key, downloadFile);
            // blocking wait
            download.waitForCompletion();

            log.debug("downloaded key {} to {}", key, downloadFile.getAbsolutePath());

            // convert to resource
            resource = new FileSystemResource(downloadFile);
        } catch (Exception e) {
            log.error("Error downloading file to: " + key, e);
            throw new FileDownloadException("Error downloading file from: " + key, e);
        }
        return resource;
    }

    public void uploadFile(File file,
                           String key,
                           String contentType)
            throws FileUploadException {
        uploadFile(s3Bucket, file, key, contentType);
    }

    public void uploadFile(String bucket,
                           File file,
                           String key,
                           String contentType)
            throws FileUploadException {
        try {
            Upload upload = transferManager.upload(bucket, key, file);
            // blocking wait, though we could let uploads just run in background
            upload.waitForCompletion();

            log.debug("uploaded file {} to {}", file.getAbsolutePath(), key);

        } catch (Exception e) {
            log.error("Error uploading file to: " + key, e);
            throw new FileUploadException("Error uploading file to: " + key, e);
        }
    }

    public void uploadFile(String key,
                           MultipartFile source)
            throws FileUploadException {
        try {
            uploadFile(s3Bucket, key, source);

        } catch (Exception e) {
            log.error("Error uploading file to: " + key, e);
            throw new FileUploadException("Error uploading file to: " + key, e);
        }
    }

    public void uploadFile(String bucket,
                           String key,
                           MultipartFile source)
            throws FileUploadException {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(source.getSize());
            objectMetadata.setContentType(source.getContentType());
            Upload upload = transferManager.upload(bucket, key, source.getInputStream(), objectMetadata);

            TransferManagerConfiguration configuration = transferManager.getConfiguration();
            configuration.setMultipartUploadThreshold(FILE_PART_SIZE);

            // blocking wait, though we could let uploads just run in background
            upload.waitForCompletion();

            log.debug("uploaded MultipartFile to {}", key);

        } catch (Exception e) {
            log.error("Error uploading file to: " + key, e);
            throw new FileUploadException("Error uploading file to: " + key, e);
        }
    }

    public void uploadFile(String content,
                           String key,
                           String contentType)
            throws FileUploadException {
        try {
            uploadFile(s3Bucket, content, key, contentType);

        } catch (Exception e) {
            log.error("Error uploading file to: " + key, e);
            throw new FileUploadException("Error uploading file to: " + key, e);
        }
    }

    public void uploadFile(String bucket,
                           String content,
                           String key,
                           String contentType)
            throws FileUploadException {
        try {
            byte[] bytes = content.getBytes("UTF-8");
            InputStream is = new ByteArrayInputStream(bytes);
            uploadFile(bucket, is, bytes.length, key, contentType);
        } catch (FileUploadException e) {
            throw e;
        } catch (Exception e) {
            throw new FileUploadException("Error uploading string content to: " + key, e);
        }
    }

    public void uploadFile(String bucket,
                           InputStream content,
                           long contentLength,
                           String key,
                           String contentType)
            throws FileUploadException {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(contentLength);
            objectMetadata.setContentType(contentType);
            Upload upload = transferManager.upload(bucket, key, content, objectMetadata);
            // blocking wait, though we could let uploads just run in background
            upload.waitForCompletion();

            log.debug("uploaded string content to {}", key);

        } catch (Exception e) {
            log.error("Error uploading string content to: " + key, e);
            throw new FileUploadException("Error uploading string content to: " + key, e);
        }
    }

    public void deleteFile(String key) {
        amazonS3.deleteObject(s3Bucket, key);
    }

    public void copyObject(String sourceKey,
                           String destinationKey) {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(s3Bucket, sourceKey, s3Bucket, destinationKey);
        transferManager.copy(copyObjectRequest);
    }

    public void copyObject(String sourceBucket,
                           String sourceKey,
                           String destinationBucket,
                           String destinationKey) {

        sourceBucket = sourceBucket != null ? sourceBucket : s3Bucket;
        destinationBucket = destinationBucket != null ? destinationBucket : s3Bucket;
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(sourceBucket, sourceKey, destinationBucket,
                                                                    destinationKey);
        transferManager.copy(copyObjectRequest);
    }

    public void copyBucketContents(String sourceBucket,
                                   String sourcePrefix,
                                   String destinationBucket,
                                   String destinationPrefix)
            throws AmazonS3Exception {
        ListObjectsRequest req = new ListObjectsRequest();
        req.setBucketName(sourceBucket);
        req.withPrefix(sourcePrefix);
        List<S3ObjectSummary> contents = amazonS3.listObjects(req).getObjectSummaries();
        if (contents.size() <= 0) {
            throw new AmazonS3Exception("No files found under " + sourceBucket + " and prefix " + sourcePrefix);
        }
        for (S3ObjectSummary summary : contents) {
            String newKey = destinationPrefix + summary.getKey().replace(sourcePrefix, "");
            CopyObjectRequest copyObjectRequest = new CopyObjectRequest(sourceBucket, summary.getKey(),
                                                                        destinationBucket, newKey);
            transferManager.copy(copyObjectRequest);
        }
    }

    public File getTmpFile() throws IOException {
        File tmpFile = File.createTempFile("lola-", ".tmp");
        tmpFile.deleteOnExit();
        return tmpFile;
    }

    public boolean doesBucketExist(String bucket) {
        return amazonS3.doesBucketExist(s3Bucket + "bucket");
    }

    public void getObjectsListed() {
        List<S3ObjectSummary> objectSummaries = new ArrayList<S3ObjectSummary>();
        ObjectListing objects = amazonS3.listObjects
                ("dev.files.tbbtalent.org", "candidate/");
        objectSummaries.addAll(objects.getObjectSummaries());
        while (objects.isTruncated()) {
            objects = amazonS3.listNextBatchOfObjects(objects);
            objectSummaries.addAll(objects.getObjectSummaries());
        }
        System.out.println("Got all objects");
        List<S3ObjectSummary> filteredBucket = objectSummaries.stream().filter(s -> !s.getKey().contains("/migrated/"))
                .collect(Collectors.toList());
//        for(S3ObjectSummary summary : buckets ) {
//            if (summary.getKey().contains("/migrated/")){
//                buckets.remove(summary);
//            }
//        }
        System.out.println("filtered out migrated");
        for(S3ObjectSummary s : filteredBucket) {
            //if (regexForPrefix(s.getKey())) {
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                Mimetypes mimetypes = Mimetypes.getInstance();
                metadata.setContentType(mimetypes.getMimetype(s.getKey()));
                final CopyObjectRequest request = new CopyObjectRequest(s.getBucketName(), s.getKey(), s.getBucketName(), s.getKey())
                        .withSourceBucketName(s.getBucketName())
                        .withSourceKey(s.getKey())
                        .withNewObjectMetadata(metadata);
                amazonS3.copyObject(request);
            } catch (Exception e) {
                System.out.println("Error with object: " + s.getKey());
            }

            //}
        }
    }

    private static String getFileExtension(String fileName) {
        // Checks that a . exists and that it isn't at the start of the filename (indication there is no file name just a file type e.g. ".pdf"
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    private boolean regexForPrefix(String location) {
        Pattern p = Pattern.compile("\\w+\\/\\d+\\/");
        Matcher m = p.matcher(location);
        if (m.find()) {
            return true;
        } else {
            return false;
        }
    }
}
