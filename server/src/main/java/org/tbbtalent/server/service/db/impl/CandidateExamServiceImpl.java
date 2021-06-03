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

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateExam;
import org.tbbtalent.server.model.db.Exam;
import org.tbbtalent.server.repository.db.CandidateExamRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tbbtalent.server.service.db.CandidateExamService;

import java.math.BigDecimal;

/**
 * Manage candidate exams
 *
 * @author John Cameron
 */
@Service
public class CandidateExamServiceImpl implements CandidateExamService {
    private final CandidateExamRepository candidateExamRepository;
    private final CandidateRepository candidateRepository;

    public CandidateExamServiceImpl(
            CandidateExamRepository candidateExamRepository,
            CandidateRepository candidateRepository) {
        this.candidateExamRepository = candidateExamRepository;
        this.candidateRepository = candidateRepository;
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
    public void updateIntakeData( @NonNull Candidate candidate, CandidateIntakeDataUpdate data)
        throws NoSuchObjectException, EntityExistsException {
        CandidateExam ce;
        Long id = data.getExamId();
        ce = candidateExamRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateExam.class, id));

        boolean existingIelts = ce.getExam() == (Exam.IELTSGen);

        ce.populateIntakeData(candidate, data);

        // Check that the requested exam type doesnt already exist to avoid duplicates of ielts exams
        CandidateExam existingExam = candidateExamRepository.findDuplicateByExamType(ce.getExam(), candidate.getId(), ce.getId()).orElse(null);
        if (existingExam != null) {
            if (existingExam.getExam().equals(Exam.IELTSGen) || existingExam.getExam().equals(Exam.IELTSAca)) {
                throw new EntityExistsException("exam type");
            }
        }

        candidateExamRepository.save(ce);

        // If exam score is not null and either existing Ielts exam is being updated, or data updating an Ielts exam.
        if (data.getExamType().equals(Exam.IELTSGen)) {
            if (!data.getExamScore().isEmpty()) {
                BigDecimal score = new BigDecimal(data.getExamScore());
                candidate.setIeltsScore(score);
            }
        } else {
            if (existingIelts) {
                candidate.setIeltsScore(null);
            }
        }

    }
}
