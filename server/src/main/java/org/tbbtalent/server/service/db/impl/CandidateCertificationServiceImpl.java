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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateCertification;
import org.tbbtalent.server.repository.db.CandidateCertificationRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tbbtalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CandidateCertificationService;
import org.tbbtalent.server.service.db.CandidateService;

@Service
public class CandidateCertificationServiceImpl implements CandidateCertificationService {

    private final CandidateCertificationRepository candidateCertificationRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final UserContext userContext;

    @Autowired
    public CandidateCertificationServiceImpl(CandidateCertificationRepository candidateCertificationRepository,
                                             CandidateRepository candidateRepository,
                                             CandidateService candidateService,
                                             UserContext userContext) {
        this.candidateCertificationRepository = candidateCertificationRepository;
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.userContext = userContext;
    }


    @Override
    public CandidateCertification createCandidateCertification(CreateCandidateCertificationRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Create a new candidateOccupation object to insert into the database
        CandidateCertification candidateCertification = new CandidateCertification();
        candidateCertification.setCandidate(candidate);
        candidateCertification.setName(request.getName());
        candidateCertification.setInstitution(request.getInstitution());
        candidateCertification.setDateCompleted(request.getDateCompleted());


        // Save the candidateOccupation
        candidateCertification = candidateCertificationRepository.save(candidateCertification);

        candidate.setAuditFields(candidate.getUser());
        candidateService.save(candidate, true);

        return candidateCertification;
    }

    @Override
    public void deleteCandidateCertification(Long id) {
        Candidate candidate = userContext.getLoggedInCandidate();
        CandidateCertification candidateCertification = candidateCertificationRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateCertification.class, id));

        // Check that the user is deleting their own candidateOccupation
        if (!candidate.getId().equals(candidateCertification.getCandidate().getId())) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }

        candidateCertificationRepository.delete(candidateCertification);

        candidate.setAuditFields(candidate.getUser());
        candidateService.save(candidate, true);

    }

    @Override
    public List<CandidateCertification> list(long id) {
        return candidateCertificationRepository.findByCandidateId(id);
    }

    @Override
    public CandidateCertification createCandidateCertificationAdmin(long candidateId, CreateCandidateCertificationRequest request) {
        Candidate candidate = this.candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));

        // Create a new candidateOccupation object to insert into the database
        CandidateCertification candidateCertification = new CandidateCertification();
        candidateCertification.setCandidate(candidate);
        candidateCertification.setName(request.getName());
        candidateCertification.setInstitution(request.getInstitution());
        candidateCertification.setDateCompleted(request.getDateCompleted());

        // Save the candidateOccupation
        candidateCertification =  candidateCertificationRepository.save(candidateCertification);

        candidate.setAuditFields(candidate.getUser());
        candidateService.save(candidate, true);

        return candidateCertification;
    }

    @Override
    public CandidateCertification updateCandidateCertificationAdmin(long id, UpdateCandidateCertificationRequest request) {

        CandidateCertification candidateCertification = this.candidateCertificationRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateCertification.class, id));

        // Update education object to insert into the database
        candidateCertification.setInstitution(request.getInstitution());
        candidateCertification.setName(request.getName());
        candidateCertification.setDateCompleted(request.getDateCompleted());

        candidateService.save(candidateCertification.getCandidate(), true);

        // Save the candidateOccupation
        return candidateCertificationRepository.save(candidateCertification);

    }
}
