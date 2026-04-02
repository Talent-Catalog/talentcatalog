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
package org.tctalent.server.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tctalent.server.configuration.properties.S3Properties;
import org.tctalent.server.exception.ServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Service for managing translation JSON files in S3.
 *
 * @author sadatmalik
 */
@Service
@RequiredArgsConstructor
public class S3TranslationStorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Retrieves the translation JSON file for the specified language from S3 and parses it into a Map.
     *
     * @param language The language code (e.g., "en", "fr") for which to retrieve the translation file.
     * @return A Map representing the contents of the translation JSON file.
     * @throws ServiceException if there is an error reading the file from S3 or parsing the JSON.
     */
    public Map<String, Object> getTranslationFile(String language) {
        try (ResponseInputStream<GetObjectResponse> in = s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(getTranslationsBucket())
                .key(getTranslationFileKey(language))
                .build())
        ) {
          return objectMapper.readValue(in, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
          throw new ServiceException("json_error", "Error reading JSON file from s3", e);
        }
    }

    public void updateTranslationFile(String language, Map<String, Object> translations) {
        final byte[] jsonBytes;
        try {
            jsonBytes = objectMapper.writeValueAsString(translations).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new ServiceException("invalid_json",
                "The translation data could not be converted to JSON", e);
        }

        String currentKey = getTranslationFileKey(language);
        String archivedKey = getTranslationsPrefix() + "old-versions/" + language
            + ".json." + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String bucket = getTranslationsBucket();

        try {
            s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(currentKey)
                .destinationBucket(bucket)
                .destinationKey(archivedKey)
                .build());

            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(currentKey)
                    .contentType("text/json")
                    .contentLength((long) jsonBytes.length)
                    .build(),
                RequestBody.fromBytes(jsonBytes)
            );
        } catch (Exception e) {
            throw new ServiceException("file_upload", "The JSON file could not be uploaded to s3", e);
        }
    }

    public void copyBucketContents(String sourceBucket, String sourcePrefix,
        String destinationBucket, String destinationPrefix) {

        String normalizedSourcePrefix = normalizePrefix(sourcePrefix);
        String normalizedDestinationPrefix = normalizePrefix(destinationPrefix);
        String token = null;
        boolean foundObjects = false;

        do {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(sourceBucket)
                .prefix(normalizedSourcePrefix)
                .continuationToken(token)
                .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            for (S3Object obj : response.contents()) {
                foundObjects = true;
                String sourceKey = obj.key();
                String relativePath = sourceKey.startsWith(normalizedSourcePrefix)
                    ? sourceKey.substring(normalizedSourcePrefix.length())
                    : sourceKey;
                String newKey = normalizedDestinationPrefix + relativePath;

                s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(sourceBucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(destinationBucket)
                    .destinationKey(newKey)
                    .build());
            }

            token = response.nextContinuationToken();
        } while (token != null);

        if (!foundObjects) {
            throw S3Exception.builder()
                .message("No files found under " + normalizedSourcePrefix)
                .build();
        }
    }

    private String getTranslationsBucket() {
        return s3Properties.getTranslationsBucket();
    }

    private String getTranslationsPrefix() {
        String configuredFolder = s3Properties.getTranslationsFolder();
        if (!StringUtils.hasText(configuredFolder)) {
            configuredFolder = "translations";
        }
        return configuredFolder.endsWith("/") ? configuredFolder : configuredFolder + "/";
    }

    private String getTranslationFileKey(String language) {
        return getTranslationsPrefix() + language + ".json";
    }

    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }
}
