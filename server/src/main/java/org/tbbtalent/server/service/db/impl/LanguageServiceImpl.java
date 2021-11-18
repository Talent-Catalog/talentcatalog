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

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.jsonwebtoken.lang.Collections;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.CandidateLanguage;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.Language;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.SystemLanguage;
import org.tbbtalent.server.repository.db.CandidateLanguageRepository;
import org.tbbtalent.server.repository.db.LanguageRepository;
import org.tbbtalent.server.repository.db.LanguageSpecification;
import org.tbbtalent.server.repository.db.SystemLanguageRepository;
import org.tbbtalent.server.request.language.CreateLanguageRequest;
import org.tbbtalent.server.request.language.SearchLanguageRequest;
import org.tbbtalent.server.request.language.UpdateLanguageRequest;
import org.tbbtalent.server.request.translation.CreateTranslationRequest;
import org.tbbtalent.server.service.db.CountryService;
import org.tbbtalent.server.service.db.LanguageService;
import org.tbbtalent.server.service.db.TranslationService;
import org.tbbtalent.server.util.locale.LocaleHelper;

@Service
public class LanguageServiceImpl implements LanguageService {

    private static final Logger log = LoggerFactory.getLogger(LanguageServiceImpl.class);

    private final LanguageRepository languageRepository;
    private final CandidateLanguageRepository candidateLanguageRepository;
    private final CountryService countryService;
    private final SystemLanguageRepository systemLanguageRepository;
    private final TranslationService translationService;

    @Autowired
    public LanguageServiceImpl(CandidateLanguageRepository candidateLanguageRepository,
        LanguageRepository languageRepository,
        CountryService countryService,
        SystemLanguageRepository systemLanguageRepository,
        TranslationService translationService) {
        this.candidateLanguageRepository = candidateLanguageRepository;
        this.languageRepository = languageRepository;
        this.countryService = countryService;
        this.systemLanguageRepository = systemLanguageRepository;
        this.translationService = translationService;
    }

    @Override
    public SystemLanguage addSystemLanguage(String langCode)
        throws EntityExistsException, NoSuchObjectException {
        if (!LocaleHelper.isKnownLanguageCode(langCode)) {
            throw new NoSuchObjectException("Unknown language code: " + langCode);
        }

        List<SystemLanguage> existing = listSystemLanguages();
        final Optional<SystemLanguage> found = existing.stream()
            .filter(s -> s.getLanguage().equals(langCode))
            .findAny();
        if (found.isPresent()) {
            throw new EntityExistsException("SystemLanguage");
        }

        //Generate the country translations
        Map<String, String> xlCountry = LocaleHelper.getCountryNameTranslations(langCode);

        CreateTranslationRequest request = new CreateTranslationRequest();
        request.setLanguage(langCode);
        request.setObjectType("country");

        List<Country> countries = countryService.listCountries(false);
        for (Country country : countries) {
            String value = xlCountry.get(country.getIsoCode());
            if (value == null) {
                log.warn("Missing translation for country " + country);
            } else {
                request.setObjectId(country.getId());
                request.setValue(value);
                translationService.createTranslation(null, request);
            }
        }

        //Generate the language translations
        Map<String, String> xlLang = LocaleHelper.getLanguageNameTranslations(langCode);

        request = new CreateTranslationRequest();
        request.setLanguage(langCode);
        request.setObjectType("language");

        List<Language> languages = listLanguages();
        for (Language language : languages) {
            String value = xlLang.get(language.getIsoCode());
            if (value == null) {
                log.warn("Missing translation for language " + language);
            } else {
                request.setObjectId(language.getId());
                request.setValue(value);
                translationService.createTranslation(null, request);
            }
        }


        //Create the new system language
        SystemLanguage sl = new SystemLanguage(langCode);
        systemLanguageRepository.save(sl);

        return sl;
    }

    @Override
    public SystemLanguage addSystemLanguageTranslations(String langCode, String tableName,
        InputStream translations) throws IOException, NoSuchObjectException {

        List<SystemLanguage> systemLanguages = listSystemLanguages();
        Optional<SystemLanguage> systemLanguage =
            systemLanguages.stream().filter(s->s.getLanguage().equals(langCode)).findAny();

        if (systemLanguage.isEmpty()) {
            throw new NoSuchObjectException("No system language set for " + langCode);
        }

        //Remove any existing translations for this langCode and tableName
        translationService.deleteTranslations(langCode, tableName);

        //Now read new translations from input stream
        CSVReader reader = new CSVReader(new InputStreamReader(translations));
        String [] tokens;
        try {
            CreateTranslationRequest request = new CreateTranslationRequest();
            request.setLanguage(langCode);
            request.setObjectType(tableName);

            while ((tokens = reader.readNext()) != null) {
                //tokens[] is an array of values from the line
                if (tokens.length == 2) {
                    long id = Long.parseLong(tokens[0]);
                    String value = tokens[1];

                    //Add new translation
                    request.setObjectId(id);
                    request.setValue(value);
                    translationService.createTranslation(null, request);

                } else if (tokens.length != 0) {
                    throw new IOException("Bad file format. Found " + tokens.length + " tokens");
                }
            }
        } catch (NumberFormatException ex) {
            throw new IOException("Bad file format. Non numeric id " + ex.getMessage());
        } catch (CsvValidationException ex) {
            throw new IOException("Bad file format: " + ex.getMessage());
        }

        return systemLanguage.get();
    }

    @Override
    public List<Language> listLanguages() {
        List<Language> languages = languageRepository.findByStatus(Status.active);
        translationService.translate(languages, "language");
        return languages;
    }

    @Override
    public List<SystemLanguage> listSystemLanguages() {
        return systemLanguageRepository.findByStatus(Status.active);
    }

    @Override
    public Language getLanguage(String languageName) {
        return languageRepository.findByNameIgnoreCase(languageName);
    }

    @Override
    public Page<Language> searchLanguages(SearchLanguageRequest request) {
        Page<Language> languages = languageRepository.findAll(
                LanguageSpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + languages.getTotalElements() + " languages in search");
        if (!StringUtils.isBlank(request.getLanguage())){
            translationService.translate(languages.getContent(), "language", request.getLanguage());
        }
        return languages;
    }

    @Override
    public Language getLanguage(long id) {
        return this.languageRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Language.class, id));
    }

    @Override
    @Transactional
    public Language createLanguage(CreateLanguageRequest request) throws EntityExistsException {
        Language language = new Language(
                request.getName(), request.getStatus());
        checkDuplicates(null, request.getName());
        return this.languageRepository.save(language);
    }


    @Override
    @Transactional
    public Language updateLanguage(long id, UpdateLanguageRequest request) throws EntityExistsException {
        Language language = this.languageRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Language.class, id));
        checkDuplicates(id, request.getName());

        language.setName(request.getName());
        language.setStatus(request.getStatus());
        return languageRepository.save(language);
    }

    @Override
    @Transactional
    public boolean deleteLanguage(long id) throws EntityReferencedException {
        Language language = languageRepository.findById(id).orElse(null);
        List<CandidateLanguage> candidateLanguages = candidateLanguageRepository.findByLanguageId(id);
        if (!Collections.isEmpty(candidateLanguages)){
            throw new EntityReferencedException("language");
        }
        if (language != null) {
            language.setStatus(Status.deleted);
            languageRepository.save(language);
            return true;
        }
        return false;
    }

    @Override
    public String updateIsoCodes() {
        StringBuilder sb = new StringBuilder();

        List<Language> languages = listLanguages();

        Map<String, String> xlLang = LocaleHelper.getLanguageNameTranslations("en");
        //Create reverse map - English name to code.

        Map<String, String> nameToCode = new HashMap<>();
        for (Entry<String, String> codeNameEntry : xlLang.entrySet()) {
            nameToCode.put(codeNameEntry.getValue(), codeNameEntry.getKey());
        }

        //Now go through languages, using name to look up code
        for (Language language : languages) {
            final String name = language.getName().trim();
            String code = nameToCode.get(name);
            if (code == null) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(name);
            } else {
                //Update iso code of language.
                language.setIsoCode(code);
                languageRepository.save(language);
            }
        }

        return sb.toString();
    }

    private void checkDuplicates(Long id, String name) {
        Language existing = languageRepository.findByNameIgnoreCase(name);
        if (existing != null && !existing.getId().equals(id)){
            throw new EntityExistsException("language");
        }
    }
}
