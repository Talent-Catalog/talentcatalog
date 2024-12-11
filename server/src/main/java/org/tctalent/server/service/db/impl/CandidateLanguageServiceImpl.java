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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateLanguageRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.LanguageLevelRepository;
import org.tctalent.server.repository.db.LanguageRepository;
import org.tctalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tctalent.server.request.candidate.language.UpdateCandidateLanguageRequest;
import org.tctalent.server.request.candidate.language.UpdateCandidateLanguagesRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateLanguageService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.util.audit.AuditHelper;

@Service
public class CandidateLanguageServiceImpl implements CandidateLanguageService {

    private final CandidateLanguageRepository candidateLanguageRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final LanguageRepository languageRepository;
    private final LanguageLevelRepository languageLevelRepository;
    private final AuthService authService;

    @Autowired
    public CandidateLanguageServiceImpl(CandidateLanguageRepository candidateLanguageRepository,
                                        LanguageRepository languageRepository,
                                        CandidateRepository candidateRepository,
                                        CandidateService candidateService,
                                        LanguageLevelRepository languageLevelRepository,
                                        AuthService authService) {
        this.candidateLanguageRepository = candidateLanguageRepository;
        this.languageRepository = languageRepository;
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.languageLevelRepository = languageLevelRepository;
        this.authService = authService;
    }

    /**
     * Used in the admin portal only to create candidate language.
     * @param request
     * @return
     */
    @Override
    public CandidateLanguage createCandidateLanguage(CreateCandidateLanguageRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Candidate candidate = candidateService.getCandidateFromRequest(request.getCandidateId());

        // Load the industry from the database - throw an exception if not found
        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new NoSuchObjectException(Language.class, request.getLanguageId()));

        // Load the languageLevels from the database - throw an exception if not found
        LanguageLevel languageSpeak = languageLevelRepository.findById(request.getSpokenLevelId())
                .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, request.getSpokenLevelId()));

        LanguageLevel languageReadWrite = languageLevelRepository.findById(request.getWrittenLevelId())
                .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, request.getWrittenLevelId()));

        // Create a new candidateLanguage object to insert into the database
        CandidateLanguage candidateLanguage = new CandidateLanguage();
        candidateLanguage.setCandidate(candidate);
        candidateLanguage.setLanguage(language);
        candidateLanguage.setSpokenLevel(languageSpeak);
        candidateLanguage.setWrittenLevel(languageReadWrite);

        // Save the candidateLanguage
        candidateLanguage = candidateLanguageRepository.save(candidateLanguage);
        AuditHelper.setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);
        return candidateLanguage;
    }

    /**
     * Used in admin portal only.
     * @param request
     * @return
     */
    @Override
    public CandidateLanguage updateCandidateLanguage(UpdateCandidateLanguageRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateLanguage candidateLanguage = candidateLanguageRepository.findById(request.getId())
                .orElseThrow(() -> new NoSuchObjectException(CandidateLanguage.class, request.getId()));

        // Load the language from the database - throw an exception if not found
        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new NoSuchObjectException(Language.class, request.getLanguageId()));

        // Load the levels from the database - throw an exception if not found
        LanguageLevel spokenLevel = languageLevelRepository.findById(request.getSpokenLevelId())
                .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, request.getSpokenLevelId()));

        LanguageLevel writtenLevel = languageLevelRepository.findById(request.getWrittenLevelId())
                .orElseThrow(() -> new NoSuchObjectException(LanguageLevel.class, request.getWrittenLevelId()));

        // Update education object to insert into the database
        candidateLanguage.setLanguage(language);
        candidateLanguage.setSpokenLevel(spokenLevel);
        candidateLanguage.setWrittenLevel(writtenLevel);

        candidateLanguage = candidateLanguageRepository.save(candidateLanguage);

        Candidate candidate = candidateLanguage.getCandidate();
        AuditHelper.setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);

        return candidateLanguage;
    }

    /**
     * Used in admin portal only to delete candidate language
     * @param id Candidate Language Id to delete
     */
    @Override
    public void deleteCandidateLanguage(Long id) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateLanguage candidateLanguage = candidateLanguageRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateLanguage.class, id));

        Candidate candidate = candidateLanguage.getCandidate();
        candidateLanguageRepository.delete(candidateLanguage);
        AuditHelper.setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);
    }

    @Override
    public List<CandidateLanguage> list(long id) {
        return candidateLanguageRepository.findByCandidateId(id);
    }

    /**
     * Used in the Candidate portal only to create/delete candidate languages by updating array of requests
     * @param request an array of language requests
     * @return a list of candidate languages
     */
    @Override
    public List<CandidateLanguage> updateCandidateLanguages(UpdateCandidateLanguagesRequest request) {
        Candidate candidate = candidateService.getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        List<CandidateLanguage> updatedLanguages = new ArrayList<>();
        List<Long> updatedLanguageIds = new ArrayList<>();

        List<CandidateLanguage> candidateLanguages = candidateLanguageRepository.findByCandidateId(candidate.getId());

        /* map contains the existing candidate's candidate languages that are currently saved in the database. */
        Map<Long, CandidateLanguage> map = candidateLanguages.stream().collect( Collectors.toMap(CandidateLanguage::getId,
                Function.identity()) );

        Map<Long, LanguageLevel> languageLevels = languageLevelRepository.findByStatus(Status.active).stream()
                .collect(Collectors.toMap(LanguageLevel::getId, Function.identity()));

        for (UpdateCandidateLanguageRequest update : request.getUpdates()) {
            /* Check if language has been previously saved */
            CandidateLanguage candidateLanguage = update.getLanguageId() != null ? map.get(update.getLanguageId()) : null;
            if (candidateLanguage != null){
                /* Check if the language has changed */
                if (!update.getLanguageId().equals(candidateLanguage.getLanguage().getId())){
                    Language language = languageRepository.findById(update.getLanguageId())
                            .orElseThrow(() -> new NoSuchObjectException(Language.class, update.getLanguageId()));
                    candidateLanguage.setLanguage(language);
                }
                candidateLanguage.setSpokenLevel(languageLevels.get(update.getSpokenLevelId()));
                candidateLanguage.setWrittenLevel(languageLevels.get(update.getWrittenLevelId()));
            } else {
                /* Create a new candidate language */
                Language language = languageRepository.findById(update.getLanguageId())
                        .orElseThrow(() -> new NoSuchObjectException(Language.class, update.getLanguageId()));
                candidateLanguage = new CandidateLanguage(
                        candidate,
                        language,
                        languageLevels.get(update.getWrittenLevelId()),
                        languageLevels.get(update.getSpokenLevelId())
                );
            }
            updatedLanguages.add(candidateLanguageRepository.save(candidateLanguage));
            updatedLanguageIds.add(candidateLanguage.getId());
        }

        for (Long existingCandidateLanguageId : map.keySet()) {
            /* Remove existing database entries that aren't present in the request */
            if (!updatedLanguageIds.contains(existingCandidateLanguageId)){
                candidateLanguageRepository.deleteById(existingCandidateLanguageId);
            }
        }

        candidate.setAuditFields(candidate.getUser());
        candidateService.save(candidate, true);

        return candidateLanguages;
    }

}
