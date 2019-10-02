package org.tbbtalent.server.service.impl;

import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.LanguageRepository;
import org.tbbtalent.server.repository.LanguageSpecification;
import org.tbbtalent.server.request.language.CreateLanguageRequest;
import org.tbbtalent.server.request.language.SearchLanguageRequest;
import org.tbbtalent.server.request.language.UpdateLanguageRequest;
import org.tbbtalent.server.service.LanguageService;

import java.util.List;

@Service
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;
    private final CandidateRepository candidateRepository;

    @Autowired
    public LanguageServiceImpl(CandidateRepository candidateRepository, LanguageRepository languageRepository) {
        this.candidateRepository = candidateRepository;
        this.languageRepository = languageRepository;
    }

    @Override
    public List<Language> listLanguages() {
        return languageRepository.findByStatus(Status.active);
    }

    @Override
    public Page<Language> searchLanguages(SearchLanguageRequest request) {
        Page<Language> languages = languageRepository.findAll(
                LanguageSpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + languages.getTotalElements() + " languages in search");
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
        List<Candidate> candidates = candidateRepository.findByLanguageId(id);
        if (!Collections.isEmpty(candidates)){
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
