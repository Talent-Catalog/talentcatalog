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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateOccupationRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.OccupationRepository;
import org.tctalent.server.request.candidate.occupation.CreateCandidateOccupationRequest;
import org.tctalent.server.request.candidate.occupation.UpdateCandidateOccupationRequest;
import org.tctalent.server.request.candidate.occupation.UpdateCandidateOccupationsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateOccupationService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.email.EmailHelper;

@Service
@Slf4j
public class CandidateOccupationServiceImpl implements CandidateOccupationService {

    private final CandidateOccupationRepository candidateOccupationRepository;
    private final OccupationRepository occupationRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final AuthService authService;
    private final EmailHelper emailHelper;

    @Autowired
    public CandidateOccupationServiceImpl(CandidateOccupationRepository candidateOccupationRepository,
                                          OccupationRepository occupationRepository,
                                          CandidateRepository candidateRepository,
                                          CandidateService candidateService,
                                          AuthService authService,
                                          EmailHelper emailHelper) {
        this.candidateOccupationRepository = candidateOccupationRepository;
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.occupationRepository = occupationRepository;
        this.authService = authService;
        this.emailHelper = emailHelper;
    }


    @Override
    public CandidateOccupation createCandidateOccupation(CreateCandidateOccupationRequest request) {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        // Load the industry from the database - throw an exception if not found
        Occupation occupation = occupationRepository.findById(request.getOccupationId())
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, request.getOccupationId()));

        // Create a new candidateOccupation object to insert into the database
        CandidateOccupation candidateOccupation = new CandidateOccupation();

        Candidate candidate;
        /* Check if request is coming from admin user */
        if (authService.hasAdminPrivileges(user.getRole())) {
            candidate = candidateRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));
            candidateOccupation.setAuditFields(user);
        } else {
            candidate = authService.getLoggedInCandidate();
            if (candidate == null) {
                throw new InvalidSessionException("Not logged in");
            }
        }

        candidateOccupation.setCandidate(candidate);

        // Check candidate doesn't already have this occupation and isn't the same candidateOccupation
        CandidateOccupation existing = candidateOccupationRepository.findByCandidateIdAAndOccupationId(candidateOccupation.getCandidate().getId(), request.getOccupationId());
        if (existing != null && !existing.getId().equals(candidateOccupation.getId())){
            throw new EntityExistsException("occupation");
        }
        candidateOccupation.setOccupation(occupation);
        candidateOccupation.setYearsExperience(request.getYearsExperience());

        // Save the candidateOccupation
        candidateOccupation = candidateOccupationRepository.save(candidateOccupation);

        candidate.setAuditFields(user);
        candidateService.save(candidate, true);

        return candidateOccupation;
    }

    @Override
    public void deleteCandidateOccupation(Long id) {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateOccupation candidateOccupation = candidateOccupationRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateOccupation.class, id));

        Candidate candidate;

        // If request is coming from admin portal
        if (authService.hasAdminPrivileges(user.getRole())) {
            candidate = candidateRepository.findById(candidateOccupation.getCandidate().getId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateOccupation.getCandidate().getId()));
        } else {
            candidate = authService.getLoggedInCandidate();
            if (candidate == null) {
                throw new InvalidSessionException("Not logged in");
            }
            // Check that the user is deleting their own attachment
            if (!candidate.getId().equals(candidateOccupation.getCandidate().getId())) {
                throw new InvalidCredentialsException("You do not have permission to perform that action");
            }
        }

        candidateOccupationRepository.delete(candidateOccupation);

        candidate.setAuditFields(user);
        candidateService.save(candidate, true);
    }

    @Override
    public List<CandidateOccupation> listMyOccupations() {
        Long candidateId = authService.getLoggedInCandidateId();
        return candidateOccupationRepository.findByCandidateIdLoadOccupation(candidateId);

    }

    @Override
    public List<CandidateOccupation> listCandidateOccupations(Long candidateId) {
        return candidateOccupationRepository.findByCandidateId(candidateId);
    }

    @Override
    public List<Occupation> listOccupations() {
        List<Occupation> occupations = candidateOccupationRepository.findAllOccupations();
        return occupations;
    }

    @Override
    public List<CandidateOccupation> updateCandidateOccupations(UpdateCandidateOccupationsRequest request) {
        // Fetch updated candidate object from the DB to collect all data updates that may have been made since logging in.
        Candidate candidate = candidateService.getLoggedInCandidate()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        List<CandidateOccupation> updatedOccupations = new ArrayList<>();
        List<Long> updatedOccupationIds = new ArrayList<>();

        List<CandidateOccupation> candidateOccupations = candidateOccupationRepository.findByCandidateId(candidate.getId());
        Map<Long, CandidateOccupation> map = candidateOccupations.stream().collect( Collectors.toMap(CandidateOccupation::getId,
                Function.identity()) );

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateCandidateOccupations")
            .message("Update candidate occupations request" + request.getUpdates())
            .logInfo();

        for (UpdateCandidateOccupationRequest update : request.getUpdates()) {
            /* Check if candidate occupation has been previously saved */
            CandidateOccupation candidateOccupation = update.getId() != null ? map.get(update.getId()) : null;
            if (candidateOccupation != null){
                if(update.getOccupationId() == null) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("UpdateCandidateOccupations")
                        .message("NULL-AVOID: update.getOccupationId. Updating " + update.getId() + ", for candidate id: " + candidate.getId())
                        .logWarn();

                    emailHelper.sendAlert("Avoided Null Pointer exception, check logs for warning. Search NULL-AVOID to find.");
                } else if (candidateOccupation.getOccupation() == null) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("UpdateCandidateOccupations")
                        .message("NULL-AVOID: candidateOccupation.getOccupation. Updating " + update.getId() + ", for candidate id: " + candidate.getId())
                        .logWarn();

                    emailHelper.sendAlert("Avoided Null Pointer exception, check logs for warning. Search NULL-AVOID to find.");
                } else {
                    /* Check if the occupation has changed on existing candidate occupation and update */
                    if (!update.getOccupationId().equals(candidateOccupation.getOccupation().getId())) {
                        Occupation occupation = occupationRepository.findById(update.getOccupationId())
                                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, update.getOccupationId()));
                        candidateOccupation.setOccupation(occupation);

                        LogBuilder.builder(log)
                            .user(authService.getLoggedInUser())
                            .action("UpdateCandidateOccupations")
                            .message("Set new Occupation to an existing candidate occupation " + occupation.getName())
                            .logInfo();
                    }
                    candidateOccupation.setYearsExperience(update.getYearsExperience());
                }
            } else {
                /* Check if candidate already has same occupation, if so update candidateOccupation, else create new. */
                candidateOccupation = candidateOccupationRepository.findByCandidateIdAAndOccupationId(candidate.getId(),
                        update.getOccupationId());

                Occupation occupation = occupationRepository.findById(update.getOccupationId())
                        .orElseThrow(() -> new NoSuchObjectException(Occupation.class, update.getOccupationId()));

                if (candidateOccupation != null) {
                    // If candidate has candidateOccupation with same occupation, just update that one.
                    candidateOccupation.setYearsExperience(update.getYearsExperience());

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("UpdateCandidateOccupations")
                        .message("Updated existing candidate occupation " + candidateOccupation.getOccupation().getName())
                        .logInfo();
                } else {
                    // If candidate has not got that occupation, create new occupation.
                    candidateOccupation = new CandidateOccupation(candidate, occupation, update.getYearsExperience());

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("UpdateCandidateOccupations")
                        .message("Created new candidate occupation " + candidateOccupation.getOccupation().getName())
                        .logInfo();
                }

            }
            updatedOccupations.add(candidateOccupationRepository.save(candidateOccupation));

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("UpdateCandidateOccupations")
                .message("Saved candidate " + candidate.getId() + " occupation " + candidateOccupation.getOccupation().getName())
                .logInfo();

            updatedOccupationIds.add(candidateOccupation.getId());
        }

        for (Long existingCandidateOccupationId : map.keySet()) {
            /* Check if the candidate occupation has been removed */
            if (!updatedOccupationIds.contains(existingCandidateOccupationId)){
                //The user will have confirmed that they are OK to lose any
                // associated experiences
                candidateOccupationRepository.deleteById(existingCandidateOccupationId);
            }
        }

        candidate.setAuditFields(candidate.getUser());
        candidateService.save(candidate, true);

        return candidateOccupations;
    }

    @Override
    public CandidateOccupation updateCandidateOccupation(UpdateCandidateOccupationRequest request) {
        CandidateOccupation candidateOccupation = candidateOccupationRepository.findByIdLoadCandidate(request.getId())
                .orElseThrow(() -> new NoSuchObjectException(CandidateOccupation.class, request.getId()));

        // Load the occupation from the database - throw an exception if not found
        Occupation occupationToBeUpdated = occupationRepository.findById(request.getOccupationId())
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, request.getOccupationId()));

        // Check candidate doesn't already have this occupation and isn't the same candidateOccupation
        CandidateOccupation existing = candidateOccupationRepository.findByCandidateIdAAndOccupationId(candidateOccupation.getCandidate().getId(), request.getOccupationId());
        if (existing != null && !existing.getId().equals(candidateOccupation.getId())){
            throw new EntityExistsException("occupation");
        }

        candidateOccupation.setOccupation(occupationToBeUpdated);
        candidateOccupation.setYearsExperience(request.getYearsExperience());

        candidateOccupation.setAuditFields(authService.getLoggedInUser().orElse(null));

        candidateService.save(candidateOccupation.getCandidate(), true);

        return candidateOccupationRepository.save(candidateOccupation);

    }
}
