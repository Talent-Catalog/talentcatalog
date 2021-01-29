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
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateExam;
import org.tbbtalent.server.repository.db.CandidateExamRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.NationalityRepository;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tbbtalent.server.service.db.CandidateExamService;

/**
 * Manage candidate exams
 *
 * @author John Cameron
 */
@Service
public class CandidateExamServiceImpl implements CandidateExamService {
    private final CandidateExamRepository candidateExamRepository;
    private final CandidateRepository candidateRepository;
    private final NationalityRepository nationalityRepository;

    public CandidateExamServiceImpl(
            CandidateExamRepository candidateExamRepository,
            CandidateRepository candidateRepository, 
            NationalityRepository nationalityRepository) {
        this.candidateExamRepository = candidateExamRepository;
        this.candidateRepository = candidateRepository;
        this.nationalityRepository = nationalityRepository;
    }

    @Override
    public CandidateExam createExam(
            long candidateId, CreateCandidateExamRequest request)
            throws NoSuchObjectException {

        Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));
        
        CandidateExam cc = new CandidateExam();
        cc.setCandidate(candidate);
        cc.setExam(request.getExam());
        cc.setScore(request.getScore());
        
        return candidateExamRepository.save(cc);
    }

    @Override
    public boolean deleteExam(long examId)
            throws EntityReferencedException, InvalidRequestException {
        candidateExamRepository.deleteById(examId);
        return true; 
    }

    @Override
    public void updateIntakeData(
            @NonNull Candidate candidate, CandidateIntakeDataUpdate data)
            throws NoSuchObjectException {

            CandidateExam cc;
            Long id = data.getExamId();
            cc = candidateExamRepository.findById(id)
                    .orElseThrow(() -> new NoSuchObjectException(CandidateExam.class, id));
            cc.populateIntakeData(candidate, data);
            candidateExamRepository.save(cc);
    }
}
