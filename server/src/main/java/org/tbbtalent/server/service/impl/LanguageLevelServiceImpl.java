package org.tbbtalent.server.service.impl;

import io.jsonwebtoken.lang.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.model.LanguageLevel;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.CandidateLanguageRepository;
import org.tbbtalent.server.repository.LanguageLevelRepository;
import org.tbbtalent.server.repository.LanguageLevelSpecification;
import org.tbbtalent.server.request.language.level.CreateLanguageLevelRequest;
import org.tbbtalent.server.request.language.level.SearchLanguageLevelRequest;
import org.tbbtalent.server.request.language.level.UpdateLanguageLevelRequest;
import org.tbbtalent.server.service.LanguageLevelService;

import java.util.List;

@Service
public class LanguageLevelServiceImpl implements LanguageLevelService {


    private static final Logger log = LoggerFactory.getLogger(LanguageLevelServiceImpl.class);

    private final CandidateLanguageRepository candidateLanguageRepository;
    private final LanguageLevelRepository languageLevelRepository;

    @Autowired
    public LanguageLevelServiceImpl(CandidateLanguageRepository candidateLanguageRepository, LanguageLevelRepository languageLevelRepository) {
        this.candidateLanguageRepository = candidateLanguageRepository;
        this.languageLevelRepository = languageLevelRepository;
    }

    @Override
    public List<LanguageLevel> listLanguageLevels() {
        return languageLevelRepository.findByStatus(Status.active);
    }

    @Override
    public Page<LanguageLevel> searchLanguageLevels(SearchLanguageLevelRequest request) {
        Page<LanguageLevel> languageLevels = languageLevelRepository.findAll(
                LanguageLevelSpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + languageLevels.getTotalElements() + " language levels in search");
        return languageLevels;
    }

    @Override
    public LanguageLevel getLanguageLevel(long id) {
        return this.languageLevelRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, id));
    }

    @Override
    @Transactional
    public LanguageLevel createLanguageLevel(CreateLanguageLevelRequest request) throws EntityExistsException {
        LanguageLevel languageLevel = new LanguageLevel(
                request.getName(), request.getStatus(), request.getLevel());
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
