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
import org.tbbtalent.server.model.db.CandidateVisaCheck;
import org.tbbtalent.server.model.db.CandidateVisaJobCheck;
import org.tbbtalent.server.model.db.Occupation;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CandidateVisaJobRepository;
import org.tbbtalent.server.repository.db.CandidateVisaRepository;
import org.tbbtalent.server.repository.db.OccupationRepository;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.visa.job.CreateCandidateVisaJobCheckRequest;
import org.tbbtalent.server.service.db.CandidateVisaJobCheckService;

//** Manage candidate visa checks
// * @author John Cameron
@Service
public class CandidateVisaJobCheckImpl implements CandidateVisaJobCheckService {
    private final CandidateVisaJobRepository candidateVisaJobRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateVisaRepository candidateVisaRepository;
    private final OccupationRepository occupationRepository;

    public CandidateVisaJobCheckImpl(
            CandidateVisaJobRepository candidateVisaJobRepository,
            CandidateRepository candidateRepository,
            CandidateVisaRepository candidateVisaRepository,
            OccupationRepository occupationRepository) {
        this.candidateVisaJobRepository = candidateVisaJobRepository;
        this.candidateRepository = candidateRepository;
        this.candidateVisaRepository = candidateVisaRepository;
        this.occupationRepository = occupationRepository;

    }

    @Override
    public CandidateVisaJobCheck createVisaJobCheck(
            long visaId, CreateCandidateVisaJobCheckRequest request)
            throws NoSuchObjectException {

        CandidateVisaCheck visaCheck = candidateVisaRepository.findById(visaId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateVisaCheck.class, visaId));

        CandidateVisaJobCheck jobCheck = new CandidateVisaJobCheck();
        jobCheck.setCandidateVisaCheck(visaCheck);
        jobCheck.setName(request.getName());
        jobCheck.setSfJobLink(request.getSfJobLink());

        return candidateVisaJobRepository.save(jobCheck);
    }

    @Override
    public boolean deleteVisaJobCheck(long visaId)
            throws EntityReferencedException, InvalidRequestException {
        candidateVisaJobRepository.deleteById(visaId);
        return true;
    }

    @Override
    public void updateIntakeData(
            Long visaJobId, @NonNull Candidate candidate,
            CandidateIntakeDataUpdate data) throws NoSuchObjectException {
        CandidateVisaJobCheck cv;
        cv = candidateVisaJobRepository.findById(visaJobId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateVisaJobCheck.class, visaJobId));

        Occupation occupation = null;
        if (data.getVisaJobOccupationId() != null) {
            occupation = occupationRepository.findById(data.getVisaJobOccupationId())
                    .orElseThrow(() -> new NoSuchObjectException(Occupation.class, data.getVisaJobOccupationId()));
        }
        cv.populateIntakeData(occupation, data);
        candidateVisaJobRepository.save(cv);

    }
}
