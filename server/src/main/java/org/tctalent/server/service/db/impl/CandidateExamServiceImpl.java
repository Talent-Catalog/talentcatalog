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


import java.util.List;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateExamRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tctalent.server.request.candidate.exam.UpdateCandidateExamRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateExamService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.email.EmailHelper;
import org.tctalent.server.util.audit.AuditHelper;

/**
 * Manage candidate exams
 *
 * @author John Cameron
 */
@Service
@Slf4j
public class CandidateExamServiceImpl implements CandidateExamService {
    private final CandidateExamRepository candidateExamRepository;
    private final CandidateRepository candidateRepository;
    private final AuthService authService;
    private final EmailHelper emailHelper;
    private final CandidateService candidateService;
    public CandidateExamServiceImpl(
        CandidateExamRepository candidateExamRepository,
        CandidateRepository candidateRepository,
        AuthService authService,
        EmailHelper emailHelper,
        CandidateService candidateService
        ) {
        this.candidateExamRepository = candidateExamRepository;
        this.candidateRepository = candidateRepository;
        this.authService = authService;
        this.emailHelper = emailHelper;
        this.candidateService = candidateService;
    }

    @Override
    public CandidateExam createExam(
            long candidateId, CreateCandidateExamRequest request)
            throws NoSuchObjectException {

        Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));

        CandidateExam ce = new CandidateExam();
        ce.setCandidate(candidate);
        ce.setExam(request.getExam());
        ce.setOtherExam(request.getOtherExam());
        ce.setScore(request.getScore());
        ce.setYear(request.getYear());
        ce.setNotes(request.getNotes());

        return candidateExamRepository.save(ce);
    }

    @Override
    public @NotNull CandidateExam updateCandidateExam(UpdateCandidateExamRequest request) {
        User loggedInUser = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateExam candidateExam = this.candidateExamRepository.findByIdLoadCandidate(request.getId())
            .orElseThrow(() -> new NoSuchObjectException(CandidateExam.class, request.getId()));

        // Update exam object to insert into the database
        candidateExam.setExam(request.getExam());
        candidateExam.setOtherExam(request.getOtherExam());
        candidateExam.setScore(request.getScore());
        candidateExam.setYear(request.getYear());
        candidateExam.setNotes(request.getNotes());

        Candidate candidate = candidateExam.getCandidate();

        // Save the candidate exam
        candidateExam = candidateExamRepository.save(candidateExam);

        AuditHelper.setAuditFieldsFromUser(candidate, loggedInUser);
        candidateService.save(candidate, true);

        return candidateExam;
    }

    @Override
    public @NotNull List<CandidateExam> list(long id) {
        return candidateExamRepository.findByCandidateId(id);
    }

}
