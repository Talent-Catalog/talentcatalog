package org.tbbtalent.server.api.system;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Base64.Encoder;

@RestController
@RequestMapping("api/system/upload")
public class SystemUploadApi {

    private static final Logger log = LoggerFactory.getLogger(SystemUploadApi.class);

    @Value("${aws.credentials.accessKey}")
    private String accessKey;

    @Value("${aws.credentials.secretKey}")
    private String secretKey;

    @Value("${aws.s3.bucketName}")
    private String bucket;

    @Value("${aws.s3.upload-folder}")
    private String uploadFolder;

    @Value("${aws.s3.max-size}")
    private long maxSize;

    @GetMapping(value = "policy/{s3Key}")
    public S3UploadData getUploadPolicy(@PathVariable("s3Key") String s3Key) throws Exception {
        log.info("access key"+accessKey);
        log.info("bucket"+bucket);
        if (StringUtils.isNotBlank(uploadFolder)) {
            s3Key = uploadFolder + "/" + s3Key;
        }
        return getPolicyForKey(s3Key);
    }

    private S3UploadData getPolicyForKey(String s3Key) throws Exception {

        Encoder encoder = Base64.getEncoder();

        StringBuilder policyDocument = new StringBuilder();
        policyDocument.append("{\"expiration\": \"" + getExpirationTimestamp() + "Z\",");
        policyDocument.append(" \"conditions\": [ ");
        policyDocument.append("      [\"starts-with\", \"$key\", \"" + s3Key + "\"],");
        policyDocument.append("      {\"acl\": \"private\"},");
        policyDocument.append("      {\"bucket\": \"" + bucket + "\"},");
        policyDocument.append("      [\"starts-with\", \"$Content-Type\", \"\"],");
        policyDocument.append("      [\"content-length-range\", 0, " + maxSize + "]");
        policyDocument.append("   ]");
        policyDocument.append("}");

        String policy = encoder.encodeToString(policyDocument.toString().getBytes("UTF-8"))
                .replaceAll("\n", "")
                .replaceAll("\r", "");

        Mac hmac = Mac.getInstance("HmacSHA1");
        hmac.init(new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA1"));
        String signature = encoder.encodeToString(hmac.doFinal(policy.getBytes("UTF-8"))).replaceAll("\n", "");

        return new S3UploadData(policy, signature, accessKey);
    }

    private String getExpirationTimestamp() {
        LocalDateTime in60min = LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(60);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return in60min.format(formatter);
    }

    // --------------------------
    final class S3UploadData {
        private String policy;
        private String signature;
        private String key;

        public S3UploadData(String policy,
                            String signature,
                            String key) {
            this.policy = policy;
            this.signature = signature;
            this.key = key;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

}
