/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.ServiceException;
import org.tbbtalent.server.model.db.AbstractTranslatableDomainObject;
import org.tbbtalent.server.model.db.Translation;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.TranslationRepository;
import org.tbbtalent.server.request.translation.CreateTranslationRequest;
import org.tbbtalent.server.request.translation.UpdateTranslationRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.TranslationService;
import org.tbbtalent.server.service.db.aws.S3ResourceHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TranslationServiceImpl implements TranslationService {

    private final TranslationRepository translationRepository;
    private final S3ResourceHelper s3ResourceHelper;
    private final AuthService authService;

    @Autowired
    public TranslationServiceImpl(TranslationRepository translationRepository,
                                  S3ResourceHelper s3ResourceHelper,
                                  AuthService authService) {
        this.s3ResourceHelper = s3ResourceHelper;
        this.authService = authService;
        this.translationRepository = translationRepository;
    }

    public <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items,
                                                                             String type) {
        String selectedLanguage = authService.getUserLanguage();
        translate(items, type, selectedLanguage);
    }

    public <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items,
                                                                             String type, String selectedLanguage) {
        // if the selected language is english, no need to load translations at all, just return original data
        if ("en".equals(selectedLanguage) || selectedLanguage == null ) {
            return;
        }

        if (CollectionUtils.isNotEmpty(items)) {
            List<Long> itemIds = items.stream().map(c -> c.getId()).collect(Collectors.toList());
            List<Translation> translations = translationRepository.findByIdsTypeLanguage(itemIds, type, selectedLanguage);
            if (CollectionUtils.isNotEmpty(translations)) {
                Map<Long, Translation> translationsById = translations.stream().collect(Collectors.toMap(Translation::getObjectId, Function.identity()));
                items.forEach(c -> {
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
    @Transactional
    public Translation createTranslation(CreateTranslationRequest request) throws EntityExistsException {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Translation translation = new Translation(user, request.getObjectId(), request.getObjectType(),
                request.getLanguage(), request.getValue());
        Translation existing = translationRepository.findByObjectIdTypeLang(request.getObjectId(), request.getObjectType(), request.getLanguage()).orElse(null);
        if (existing != null){
            throw new EntityExistsException("translation");
        }
        return this.translationRepository.save(translation);
    }


    @Override
    @Transactional
    public Translation updateTranslation(long id, UpdateTranslationRequest request) throws EntityExistsException {
        Translation translation = this.translationRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Translation.class, id));
        translation.setValue(request.getValue());
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
    public void updateTranslationFile(String language, Map translations) {
        try {
            String json = new ObjectMapper().writeValueAsString(translations);
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

            this.s3ResourceHelper.copyObject(
                    "translations/" + language + ".json",
                    "translations/old-versions/" + language + ".json." + fmt.format(new Date()));

            this.s3ResourceHelper.uploadFile(this.s3ResourceHelper.getS3Bucket(), json,
                    "translations/" + language + ".json", "text/json");

        } catch (JsonProcessingException e) {
            throw new ServiceException("invalid_json", "The translation data could not be converted to JSON", e);
        } catch (FileUploadException e) {
            throw new ServiceException("file_upload", "The JSON file could not be uploaded to s3", e);
        }

    }



}


