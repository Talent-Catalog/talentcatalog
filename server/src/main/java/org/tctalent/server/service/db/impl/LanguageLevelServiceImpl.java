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

import io.jsonwebtoken.lang.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.CandidateLanguageRepository;
import org.tctalent.server.repository.db.LanguageLevelRepository;
import org.tctalent.server.repository.db.LanguageLevelSpecification;
import org.tctalent.server.request.language.level.CreateLanguageLevelRequest;
import org.tctalent.server.request.language.level.SearchLanguageLevelRequest;
import org.tctalent.server.request.language.level.UpdateLanguageLevelRequest;
import org.tctalent.server.service.db.LanguageLevelService;
import org.tctalent.server.service.db.TranslationService;

@Service
@Slf4j
public class LanguageLevelServiceImpl implements LanguageLevelService {

    private final CandidateLanguageRepository candidateLanguageRepository;
    private final LanguageLevelRepository languageLevelRepository;
    private final TranslationService translationService;

    @Autowired
    public LanguageLevelServiceImpl(CandidateLanguageRepository candidateLanguageRepository,
                                    LanguageLevelRepository languageLevelRepository,
                                    TranslationService translationService) {
        this.candidateLanguageRepository = candidateLanguageRepository;
        this.languageLevelRepository = languageLevelRepository;
        this.translationService = translationService;
    }

    @Override
    public List<LanguageLevel> listLanguageLevels() {
        List<LanguageLevel> languageLevels = languageLevelRepository.findByStatus(Status.active);
        translationService.translate(languageLevels, "language_level");
        return languageLevels;
    }

    @Override
    public Page<LanguageLevel> searchLanguageLevels(SearchLanguageLevelRequest request) {
        Page<LanguageLevel> languageLevels = languageLevelRepository.findAll(
                LanguageLevelSpecification.buildSearchQuery(request), request.getPageRequest());
        LogBuilder.builder(log)
            .action("SearchLanguageLevels")
            .message("Found " + languageLevels.getTotalElements() + " language levels in search")
            .logInfo();

        if (!StringUtils.isBlank(request.getLanguage())){
            translationService.translate(languageLevels.getContent(), "language_level", request.getLanguage());
        }
        return languageLevels;
    }

    @Override
    public LanguageLevel findByLevel(int level) {
        return languageLevelRepository.findByLevel(level)
            .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, level));
    }

    @Override
    public LanguageLevel getLanguageLevel(long id) {
        return this.languageLevelRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, id));
    }

    @Override
    @Transactional
    public LanguageLevel createLanguageLevel(
        CreateLanguageLevelRequest request) throws EntityExistsException {
        LanguageLevel languageLevel = new LanguageLevel(
                request.getName(), request.getStatus(), request.getLevel(), request.getCefrLevel());
        checkDuplicates(null, request.getName(), request.getLevel());
        return this.languageLevelRepository.save(languageLevel);
    }

    @Override
    @Transactional
    public LanguageLevel updateLanguageLevel(long id, UpdateLanguageLevelRequest request) throws EntityExistsException {
        LanguageLevel languageLevel = this.languageLevelRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, id));
        checkDuplicates(id, request.getName(), request.getLevel());

        languageLevel.setName(request.getName());
        languageLevel.setLevel(request.getLevel());
        languageLevel.setStatus(request.getStatus());
        languageLevel.setCefrLevel(request.getCefrLevel());
        return languageLevelRepository.save(languageLevel);
    }

    @Override
    @Transactional
    public boolean deleteLanguageLevel(long id) throws EntityReferencedException {
        LanguageLevel languageLevel = languageLevelRepository.findById(id).orElse(null);
        List<CandidateLanguage> candidateLanguages = candidateLanguageRepository.findByLanguageLevelId(id);
        if (!Collections.isEmpty(candidateLanguages)){
            throw new EntityReferencedException("language level");
        }
        if (languageLevel != null) {
            languageLevel.setStatus(Status.deleted);
            languageLevelRepository.save(languageLevel);
            return true;
        }
        return false;
    }

    private void checkDuplicates(Long id, String name, int level) {
        LanguageLevel existingName = languageLevelRepository.findByNameIgnoreCase(name);
        if (existingName != null && !existingName.getId().equals(id) || (existingName != null && id == null)){
            throw new EntityExistsException("language level");
        }

        LanguageLevel existingLevel = languageLevelRepository.findByLevelIgnoreCase(level);
        if (existingLevel != null && !existingLevel.getId().equals(id) || (existingLevel != null && id == null)){
            throw new EntityExistsException("language level");
        }

    }
}
