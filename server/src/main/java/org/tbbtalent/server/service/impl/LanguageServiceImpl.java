package org.tbbtalent.server.service.impl;

import java.util.List;

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
import org.tbbtalent.server.service.LanguageService;
import org.tbbtalent.server.service.TranslationService;

import io.jsonwebtoken.lang.Collections;

@Service
public class LanguageServiceImpl implements LanguageService {

    private static final Logger log = LoggerFactory.getLogger(LanguageServiceImpl.class);

    private final LanguageRepository languageRepository;
    private final CandidateLanguageRepository candidateLanguageRepository;
    private final SystemLanguageRepository systemLanguageRepository;
    private final TranslationService translationService;

    @Autowired
    public LanguageServiceImpl(CandidateLanguageRepository candidateLanguageRepository,
                               LanguageRepository languageRepository,
                               SystemLanguageRepository systemLanguageRepository,
                               TranslationService translationService) {
        this.candidateLanguageRepository = candidateLanguageRepository;
        this.languageRepository = languageRepository;
        this.systemLanguageRepository = systemLanguageRepository;
        this.translationService = translationService;
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

    private void checkDuplicates(Long id, String name) {
        Language existing = languageRepository.findByNameIgnoreCase(name);
        if (existing != null && !existing.getId().equals(id) || (existing != null && id == null)){
            throw new EntityExistsException("country");
        }
    }
}
