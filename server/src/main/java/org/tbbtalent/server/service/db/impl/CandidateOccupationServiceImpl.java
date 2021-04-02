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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.repository.db.CandidateJobExperienceRepository;
import org.tbbtalent.server.repository.db.CandidateOccupationRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.OccupationRepository;
import org.tbbtalent.server.request.candidate.occupation.CreateCandidateOccupationRequest;
import org.tbbtalent.server.request.candidate.occupation.UpdateCandidateOccupationRequest;
import org.tbbtalent.server.request.candidate.occupation.UpdateCandidateOccupationsRequest;
import org.tbbtalent.server.request.candidate.occupation.VerifyCandidateOccupationRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CandidateNoteService;
import org.tbbtalent.server.service.db.CandidateOccupationService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.email.EmailHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CandidateOccupationServiceImpl implements CandidateOccupationService {

    private static final Logger log = LoggerFactory.getLogger(CandidateServiceImpl.class);

    private final CandidateOccupationRepository candidateOccupationRepository;
    private final CandidateJobExperienceRepository candidateJobExperienceRepository;
    private final OccupationRepository occupationRepository;
    private final CandidateNoteService candidateNoteService;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final UserContext userContext;
    private final EmailHelper emailHelper;

    @Autowired
    public CandidateOccupationServiceImpl(CandidateOccupationRepository candidateOccupationRepository,
                                          CandidateJobExperienceRepository candidateJobExperienceRepository,
                                          OccupationRepository occupationRepository,
                                          CandidateRepository candidateRepository,
                                          CandidateService candidateService,
                                          CandidateNoteService candidateNoteService,
                                          UserContext userContext,
                                          EmailHelper emailHelper) {
        this.candidateOccupationRepository = candidateOccupationRepository;
        this.candidateJobExperienceRepository = candidateJobExperienceRepository;
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.occupationRepository = occupationRepository;
        this.candidateNoteService = candidateNoteService;
        this.userContext = userContext;
        this.emailHelper = emailHelper;
    }


    @Override
    public CandidateOccupation createCandidateOccupation(CreateCandidateOccupationRequest request) {
        User user = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        
        // Load the industry from the database - throw an exception if not found
        Occupation occupation = occupationRepository.findById(request.getOccupationId())
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, request.getOccupationId()));

        // Create a new candidateOccupation object to insert into the database
        CandidateOccupation candidateOccupation = new CandidateOccupation();

        Candidate candidate;
        /* Check if request is coming from admin */
        if (user.getRole().equals(Role.admin)) {
            candidate = candidateRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));
            // Set verified if request coming from admin
            candidateOccupation.setVerified(request.isVerified());

            candidateOccupation.setAuditFields(user);
            // removed verification note as verified no longer used
//            candidateNoteService.createCandidateNote(new CreateCandidateNoteRequest(request.getCandidateId(),
//                    occupation.getName() +" verification status set to "+request.isVerified(), request.getComment()));
        } else {
            candidate = userContext.getLoggedInCandidate();
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

        candidate.setAuditFields(candidate.getUser());
        candidateService.save(candidate, true);

        return candidateOccupation;
    }

    @Override
    public void deleteCandidateOccupation(Long id) {
        User user = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateOccupation candidateOccupation = candidateOccupationRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateOccupation.class, id));

        Candidate candidate;

        // If request is coming from admin portal
        if (user.getRole().equals(Role.admin)) {
            candidate = candidateRepository.findById(candidateOccupation.getCandidate().getId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateOccupation.getCandidate().getId()));
        } else {
            candidate = userContext.getLoggedInCandidate();
            if (candidate == null) {
                throw new InvalidSessionException("Not logged in");
            }
            // Check that the user is deleting their own attachment
            if (!candidate.getId().equals(candidateOccupation.getCandidate().getId())) {
                throw new InvalidCredentialsException("You do not have permission to perform that action");
            }
        }

        candidateOccupationRepository.delete(candidateOccupation);

        candidate.setAuditFields(candidate.getUser());
        candidateService.save(candidate, true);
    }

    @Override
    public List<CandidateOccupation> listMyOccupations() {
        Long candidateId = userContext.getLoggedInCandidateId();
        return candidateOccupationRepository.findByCandidateIdLoadOccupation(candidateId);

    }

    @Override
    public List<CandidateOccupation> listCandidateOccupations(Long candidateId) {
        return candidateOccupationRepository.findByCandidateId(candidateId);
    }

    @Override
    public List<Occupation> listVerifiedOccupations() {
        List<Occupation> verifiedOccupations = candidateOccupationRepository.findAllVerifiedOccupations();
        return verifiedOccupations;
    }

    @Override
    public List<Occupation> listOccupations() {
        List<Occupation> occupations = candidateOccupationRepository.findAllOccupations();
        return occupations;
    }

    @Override
    public List<CandidateOccupation> updateCandidateOccupations(UpdateCandidateOccupationsRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        if (candidate == null) {
            throw new InvalidSessionException("Not logged in");
        }
        List<CandidateOccupation> updatedOccupations = new ArrayList<>();
        List<Long> updatedOccupationIds = new ArrayList<>();

        List<CandidateOccupation> candidateOccupations = candidateOccupationRepository.findByCandidateId(candidate.getId());
        Map<Long, CandidateOccupation> map = candidateOccupations.stream().collect( Collectors.toMap(CandidateOccupation::getId,
                Function.identity()) );

        log.info("Update candidate occupations request" + request.getUpdates());

        for (UpdateCandidateOccupationRequest update : request.getUpdates()) {
            /* Check if candidate occupation has been previously saved */
            CandidateOccupation candidateOccupation = update.getId() != null ? map.get(update.getId()) : null;
            if (candidateOccupation != null){
                if(update.getOccupationId() == null) {
                    log.warn("NULL-AVOID: update.getOccupationId. Updating " + update.getId() + ", for candidate id: " + candidate.getId());
                    emailHelper.sendAlert("Avoided Null Pointer exception, check logs for warning. Search NULL-AVOID to find.");
                } else if (candidateOccupation.getOccupation() == null) {
                    log.warn("NULL-AVOID: candidateOccupation.getOccupation. Updating " + update.getId() + ", for candidate id: " + candidate.getId());
                    emailHelper.sendAlert("Avoided Null Pointer exception, check logs for warning. Search NULL-AVOID to find.");
                } else {
                    /* Check if the occupation has changed on existing candidate occupation and update */
                    if (!update.getOccupationId().equals(candidateOccupation.getOccupation().getId())) {
                        Occupation occupation = occupationRepository.findById(update.getOccupationId())
                                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, update.getOccupationId()));
                        candidateOccupation.setOccupation(occupation);
                        log.info("Set new Occupation to on existing candidate occupation " + occupation.getName());
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
                    log.info("Updated existing candidate occupation " + candidateOccupation.getOccupation().getName());
                } else {
                    // If candidate has not got that occupation, create new occupation.
                    candidateOccupation = new CandidateOccupation(candidate, occupation, update.getYearsExperience());
                    log.info("Created new candidate occupation " + candidateOccupation.getOccupation().getName());
                }

            }
            updatedOccupations.add(candidateOccupationRepository.save(candidateOccupation));
            log.info("Saved candidate " + candidate.getId() + " occupation " + candidateOccupation.getOccupation().getName());
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
    //@Audit(type = AuditType.CANDIDATE_OCCUPATION, action = AuditAction.VERIFY, extraInfo = "Set verified to {input.verified} with comment: {input.comment}")
    public CandidateOccupation verifyCandidateOccupation(VerifyCandidateOccupationRequest request) {
        CandidateOccupation candidateOccupation = candidateOccupationRepository.findByIdLoadCandidate(request.getId())
                .orElseThrow(() -> new NoSuchObjectException(CandidateOccupation.class, request.getId()));

        // Load the verified occupation from the database - throw an exception if not found
        Occupation verifiedOccupation = occupationRepository.findById(request.getOccupationId())
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, request.getOccupationId()));

        // Check candidate doesn't already have this occupation and isn't the same candidateOccupation
        CandidateOccupation existing = candidateOccupationRepository.findByCandidateIdAAndOccupationId(candidateOccupation.getCandidate().getId(), request.getOccupationId());
        if (existing != null && !existing.getId().equals(candidateOccupation.getId())){
            throw new EntityExistsException("occupation");
        }

        candidateOccupation.setOccupation(verifiedOccupation);
        candidateOccupation.setYearsExperience(request.getYearsExperience());
        candidateOccupation.setVerified(request.isVerified());

        candidateOccupation.setAuditFields(userContext.getLoggedInUser().orElse(null));
        // removed as no longer use verification
//        candidateNoteService.createCandidateNote(new CreateCandidateNoteRequest(candidateOccupation.getCandidate().getId(),
//                candidateOccupation.getOccupation().getName() +" verification status set to "+request.isVerified(), request.getComment()));

        candidateService.save(candidateOccupation.getCandidate(), true);
        
        return candidateOccupationRepository.save(candidateOccupation);

    }
//
//    @Override
//    public CandidateOccupation getCandidateOccupation(ListJobExperienceRequest request) {
//        return candidateOccupationRepository.findByCandidateIdAAndOccupationId(request.getCandidateId(), request.getOccupationId());
//    }
}
