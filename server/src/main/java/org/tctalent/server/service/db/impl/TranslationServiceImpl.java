/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.ServiceException;
import org.tctalent.server.model.db.AbstractTranslatableDomainObject;
import org.tctalent.server.model.db.Translation;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.TranslationRepository;
import org.tctalent.server.request.translation.CreateTranslationRequest;
import org.tctalent.server.request.translation.UpdateTranslationRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.TranslationService;
import org.tctalent.server.service.db.aws.S3ResourceHelper;
import org.tctalent.server.util.html.HtmlSanitizer;

@Service
public class TranslationServiceImpl implements TranslationService {

    private final TranslationRepository translationRepository;
    private final S3ResourceHelper s3ResourceHelper;
    private final AuthService authService;
    private final Environment environment;

    private Map<String, Object> englishS3Translations;

    @Autowired
    public TranslationServiceImpl(TranslationRepository translationRepository,
                                  S3ResourceHelper s3ResourceHelper,
                                  Environment environment,
                                  AuthService authService) {
        this.s3ResourceHelper = s3ResourceHelper;
        this.authService = authService;
        this.environment = environment;
        this.translationRepository = translationRepository;
        // Skip S3 call in test profile
        if (!List.of(environment.getActiveProfiles()).contains("test")) {
            englishS3Translations = getTranslationFile("en");
        }
    }

    public <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> entities,
                                                                             String objectType) {
        String selectedLanguage = authService.getUserLanguage();
        translate(entities, objectType, selectedLanguage);
    }

    public <T extends AbstractTranslatableDomainObject<Long>> void translate(
        List<T> entities, String objectType, String selectedLanguage) {
        // if the selected language is english, no need to load translations at all, just return
        // original data
        if ("en".equals(selectedLanguage) || selectedLanguage == null ) {
            return;
        }

        if (CollectionUtils.isNotEmpty(entities)) {
            List<Long> itemIds = entities.stream().map(c -> c.getId()).collect(Collectors.toList());
            List<Translation> translations = translationRepository.findByIdsTypeLanguage(itemIds,
                objectType, selectedLanguage);
            if (CollectionUtils.isNotEmpty(translations)) {
                Map<Long, Translation> translationsById = translations.stream().collect(Collectors.toMap(Translation::getObjectId, Function.identity()));
                entities.forEach(c -> {
                    Translation translation = translationsById.get(c.getId());
                    if (translation != null) {
                        c.setTranslatedId(translation.getId());
                        c.setTranslatedName(translation.getValue());
                    }
                });
            }
        }
    }

    @Override
    public Translation createTranslation(User user, CreateTranslationRequest request) {
        String sanitizedValue = HtmlSanitizer.sanitize(request.getValue());

        Translation translation = new Translation(user, request.getObjectId(), request.getObjectType(),
            request.getLanguage(), sanitizedValue);
        Translation existing = translationRepository.findByObjectIdTypeLang(request.getObjectId(), request.getObjectType(), request.getLanguage()).orElse(null);
        if (existing != null){
            throw new EntityExistsException("translation");
        }
        return this.translationRepository.save(translation);
    }

    @Override
    @Transactional
    public Translation createTranslation(CreateTranslationRequest request) throws EntityExistsException {
        return createTranslation(null, request);
    }

    @Override
    @Transactional
    public void deleteTranslations(String langCode, String objectType) {
        translationRepository.deleteTranslations(langCode, objectType);
    }

    @Override
    @Transactional
    public Translation updateTranslation(long id, UpdateTranslationRequest request) throws EntityExistsException {
        String sanitizedValue = HtmlSanitizer.sanitize(request.getValue());
        Translation translation = this.translationRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Translation.class, id));
        translation.setValue(sanitizedValue);
        return translationRepository.save(translation);
    }

    @Override
    public Map<String, Object> getTranslationFile(String language) {
        try {
            File file = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(),
                    "translations/" + language + ".json");
            return new ObjectMapper().readValue(file, Map.class);
        } catch (IOException e) {
            throw new ServiceException("json_error", "Error reading JSON file from s3", e);
        }
    }

    @Override
    public void updateTranslationFile(String language, Map<String, Object> translations) {
        try {
            String json = new ObjectMapper().writeValueAsString(translations);
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

            this.s3ResourceHelper.copyObject(
                    "translations/" + language + ".json",
                    "translations/old-versions/" + language + ".json." + fmt.format(new Date()));

            this.s3ResourceHelper.uploadFile(this.s3ResourceHelper.getS3Bucket(), json,
                    "translations/" + language + ".json", "text/json");

            if ("en".equals(language)) {
                englishS3Translations = translations;
            }
        } catch (JsonProcessingException e) {
            throw new ServiceException("invalid_json", "The translation data could not be converted to JSON", e);
        } catch (FileUploadException e) {
            throw new ServiceException("file_upload", "The JSON file could not be uploaded to s3", e);
        }

    }

    @Override
    public String translateToEnglish(String... keys) {
        return englishS3Translations == null ? null : translate(englishS3Translations, keys);
    }

    @Override
    @Nullable
    public String translate(Map<String, Object> translations, String... keys) {
        return (String) getNestedValue(translations, keys);
    }

    @Nullable
    private static Object getNestedValue(Map<String, Object> map, String... keys) {
        Object value = map;

        for (String key : keys) {
            value = ((Map)value).get(key.toUpperCase());
            if (value == null) {
                break;
            }
        }

        return value;
    }



}


