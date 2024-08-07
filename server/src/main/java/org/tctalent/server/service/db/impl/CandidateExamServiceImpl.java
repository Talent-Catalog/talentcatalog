/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateExamRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tctalent.server.request.candidate.exam.UpdateCandidateExamRequest;
import org.tctalent.server.request.candidate.exam.UpdateCandidateExamsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateExamService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.email.EmailHelper;

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

        return candidateExamRepository.save(ce);
    }

    @Override
    public List<CandidateExam> updateCandidateExam(UpdateCandidateExamsRequest request) {

        // Obtain logged-in user for audit fields
        User user = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        Candidate candidate = authService.getLoggedInCandidate();
        if (candidate == null) {
            throw new InvalidSessionException("Not logged in");
        }
        List<CandidateExam> updatedExams = new ArrayList<>();
        List<Long> updatedExamIds = new ArrayList<>();

        List<CandidateExam> candidateExams = candidateExamRepository.findByCandidateId(candidate.getId());
        Map<Long, CandidateExam> map = candidateExams.stream().collect(Collectors.toMap(CandidateExam::getId,
            Function.identity()));


        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateCandidateExams")
            .message("Update candidate exams request" + request.getUpdates())
            .logInfo();

        for (UpdateCandidateExamRequest update : request.getUpdates()) {
            // Check if candidate exam has been previously saved
            CandidateExam candidateExam = update.getId() != null ? map.get(update.getId()) : null;
            if (candidateExam != null){
                if(update.getExam() == null) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("UpdateCandidateExams")
                        .message("NULL-AVOID: update.getExam. Updating " + update.getId() + ", for candidate id: " + candidate.getId())
                        .logWarn();

                    emailHelper.sendAlert("Avoided Null Pointer exception, check logs for warning. Search NULL-AVOID to find.");
                } else if (candidateExam.getExam() == null) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("UpdateCandidateExams")
                        .message("NULL-AVOID: candidateExam.getExam. Updating " + update.getId() + ", for candidate id: " + candidate.getId())
                        .logWarn();

                    emailHelper.sendAlert("Avoided Null Pointer exception, check logs for warning. Search NULL-AVOID to find.");
                } else {
                    // Check if the exam has changed on existing candidate exam and update
                    if (!update.getExam().equals(candidateExam.getExam())) {
                        candidateExam.setExam(update.getExam());
                        LogBuilder.builder(log)
                            .user(authService.getLoggedInUser())
                            .action("UpdateCandidateExams")
                            .message("Set new Exam to an existing candidate exam " + update.getExam().name())
                            .logInfo();
                    }
                    candidateExam.setScore(update.getScore());
                    candidateExam.setYear(update.getYear());
                    candidateExam.setNotes(update.getNotes());
                    candidateExam.setOtherExam(update.getOtherExam());
                }
            } else {
                // Check if candidate already has the same exam, if so update candidateExam, else create new
                candidateExam = candidateExamRepository.findByCandidateIdAndExam(candidate.getId(), update.getExam());

                if (candidateExam != null) {
                    // If candidate has candidateExam with the same exam, just update that one
                    candidateExam.setScore(update.getScore());
                    candidateExam.setYear(update.getYear());
                    candidateExam.setNotes(update.getNotes());
                    candidateExam.setOtherExam(update.getOtherExam());

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("UpdateCandidateExams")
                        .message("Updated existing candidate exam " + candidateExam.getExam().name())
                        .logInfo();
                } else {
                    // If candidate does not have that exam, create new exam
                    candidateExam = new CandidateExam();
                    candidateExam.setCandidate(candidate);
                    candidateExam.setExam(update.getExam());
                    candidateExam.setScore(update.getScore());
                    candidateExam.setYear(update.getYear());
                    candidateExam.setNotes(update.getNotes());
                    candidateExam.setOtherExam(update.getOtherExam());

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("UpdateCandidateExams")
                        .message("Created new candidate exam " + candidateExam.getExam().name())
                        .logInfo();
                }
            }
            updatedExams.add(candidateExamRepository.save(candidateExam));

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("UpdateCandidateExams")
                .message("Saved candidate " + candidate.getId() + " exam " + candidateExam.getExam().name())
                .logInfo();

            updatedExamIds.add(candidateExam.getId());
        }
        for (Long existingCandidateExamId : map.keySet()) {
            // Check if the candidate exam has been removed
            if (!updatedExamIds.contains(existingCandidateExamId)){
                candidateExamRepository.deleteById(existingCandidateExamId);
            }
        }

        candidate.setAuditFields(user);
        candidateService.save(candidate, true);

        return updatedExams;
    }


    public boolean deleteCandidateExam(Long id) {
        if (candidateExamRepository.existsById(id)) {
            candidateExamRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
