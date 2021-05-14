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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.repository.db.CandidateLanguageRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.LanguageLevelRepository;
import org.tbbtalent.server.repository.db.LanguageRepository;
import org.tbbtalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tbbtalent.server.request.candidate.language.UpdateCandidateLanguageRequest;
import org.tbbtalent.server.request.candidate.language.UpdateCandidateLanguagesRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CandidateLanguageService;
import org.tbbtalent.server.service.db.CandidateService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CandidateLanguageServiceImpl implements CandidateLanguageService {

    private final CandidateLanguageRepository candidateLanguageRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final LanguageRepository languageRepository;
    private final LanguageLevelRepository languageLevelRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateLanguageServiceImpl(CandidateLanguageRepository candidateLanguageRepository,
                                        LanguageRepository languageRepository,
                                        CandidateRepository candidateRepository,
                                        CandidateService candidateService,
                                        LanguageLevelRepository languageLevelRepository,
                                        UserContext userContext) {
        this.candidateLanguageRepository = candidateLanguageRepository;
        this.languageRepository = languageRepository;
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.languageLevelRepository = languageLevelRepository;
        this.userContext = userContext;
    }

    /**
     * Used in the admin portal only to create candidate language.
     * @param request
     * @return
     */
    @Override
    public CandidateLanguage createCandidateLanguage(CreateCandidateLanguageRequest request) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Candidate candidate = getCandidateFromRequest(request.getCandidateId());

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
        setAuditFieldsFromUser(candidate, loggedInUser);
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
        candidate.setAuditFields(candidate.getUser());
        candidateService.save(candidate, true);

        return candidateLanguage;
    }

    /**
     * Used in admin portal only to delete candidate language
     * @param id Candidate Language Id to delete
     */
    @Override
    public void deleteCandidateLanguage(Long id) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateLanguage candidateLanguage = candidateLanguageRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateLanguage.class, id));

        Candidate candidate = candidateLanguage.getCandidate();
        candidateLanguageRepository.delete(candidateLanguage);
        setAuditFieldsFromUser(candidate, loggedInUser);
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
        Candidate candidate = userContext.getLoggedInCandidate();
        List<CandidateLanguage> updatedLanguages = new ArrayList<>();
        List<Long> updatedLanguageIds = new ArrayList<>();

        List<CandidateLanguage> candidateLanguages = candidateLanguageRepository.findByCandidateId(candidate.getId());
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

    /**
     * Depending on if the request came from the admin or candidate portal, sets audit fields with correct user. If
     * User and Candidate are the same, then it's candidate portal. If User and Candidate are different comes from admin.
     * @param candidate Candidate being altered
     * @param user Logged in User
     */
    private void setAuditFieldsFromUser(Candidate candidate, User user) {
        if (candidate.getUser() == user ) {
            candidate.setAuditFields(candidate.getUser());
        } else {
            candidate.setAuditFields(user);
        }
    }

    /**
     * Depending on where the request comes from (candidate or admin portal) need to get the candidate differently.
     * @param candidateId If from admin portal, id will be present from the request.
     *                    If null, it will come from candidate portal and candidate will be loggedInCandidate.
     * @return The candidate that is being populated/updated.
     */
    private Candidate getCandidateFromRequest(@Nullable Long candidateId) {
        Candidate candidate;
        if (candidateId != null) {
            // Coming from Admin Portal
            candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));
        } else {
            // Coming from Candidate Portal
            candidate = userContext.getLoggedInCandidate();
            if (candidate == null) {
                throw new InvalidSessionException("Not logged in");
            }
        }
        return candidate;
    }
}
