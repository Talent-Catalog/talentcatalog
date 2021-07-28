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
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateCertification;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateCertificationRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tbbtalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.CandidateCertificationService;
import org.tbbtalent.server.service.db.CandidateService;

import java.util.List;

import static org.tbbtalent.server.util.audit.AuditHelper.setAuditFieldsFromUser;

@Service
public class CandidateCertificationServiceImpl implements CandidateCertificationService {

    private final CandidateCertificationRepository candidateCertificationRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final AuthService authService;

    @Autowired
    public CandidateCertificationServiceImpl(CandidateCertificationRepository candidateCertificationRepository,
                                             CandidateRepository candidateRepository,
                                             CandidateService candidateService,
                                             AuthService authService) {
        this.candidateCertificationRepository = candidateCertificationRepository;
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.authService = authService;
    }

    @Override
    public List<CandidateCertification> list(long id) {
        return candidateCertificationRepository.findByCandidateId(id);
    }

    @Override
    public CandidateCertification createCandidateCertification(CreateCandidateCertificationRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Candidate candidate = candidateService.getCandidateFromRequest(request.getCandidateId());

        // Create a new candidateOccupation object to insert into the database
        CandidateCertification candidateCertification = new CandidateCertification();
        candidateCertification.setCandidate(candidate);
        candidateCertification.setName(request.getName());
        candidateCertification.setInstitution(request.getInstitution());
        candidateCertification.setDateCompleted(request.getDateCompleted());


        // Save the candidateOccupation
        candidateCertification = candidateCertificationRepository.save(candidateCertification);

        setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);

        return candidateCertification;
    }

    @Override
    public CandidateCertification updateCandidateCertification(UpdateCandidateCertificationRequest request) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateCertification candidateCertification = this.candidateCertificationRepository.findByIdLoadCandidate(request.getId())
                .orElseThrow(() -> new NoSuchObjectException(CandidateCertification.class, request.getId()));

        // Update certification object to insert into the database
        candidateCertification.setName(request.getName());
        candidateCertification.setInstitution(request.getInstitution());
        candidateCertification.setDateCompleted(request.getDateCompleted());

        Candidate candidate = candidateCertification.getCandidate();

        // Save the candidate certification
        candidateCertification = candidateCertificationRepository.save(candidateCertification);

        setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);

        return candidateCertification;
    }

    @Override
    public void deleteCandidateCertification(Long id) {
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateCertification candidateCertification = candidateCertificationRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateCertification.class, id));

        Candidate candidate = candidateCertification.getCandidate();

        // Check that the user is deleting their own candidate certification
        if (!candidate.getId().equals(candidateCertification.getCandidate().getId())) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }

        candidateCertificationRepository.delete(candidateCertification);

        setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);

    }
}
