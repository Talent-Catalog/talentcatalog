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

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.model.db.CandidateVisaJobCheck;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.repository.db.*;
import org.tctalent.server.request.candidate.visa.CandidateVisaCheckData;
import org.tctalent.server.request.candidate.visa.job.CreateCandidateVisaJobCheckRequest;
import org.tctalent.server.service.db.CandidateVisaJobCheckService;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateVisaJobRepository;
import org.tctalent.server.repository.db.CandidateVisaRepository;
import org.tctalent.server.repository.db.OccupationRepository;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;

//** Manage candidate visa checks
// * @author John Cameron
@Service
public class CandidateVisaJobCheckImpl implements CandidateVisaJobCheckService {
    private final CandidateVisaJobRepository candidateVisaJobRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateVisaRepository candidateVisaRepository;
    private final OccupationRepository occupationRepository;
    private final SalesforceJobOppRepository salesforceJobOppRepository;

    public CandidateVisaJobCheckImpl(
            CandidateVisaJobRepository candidateVisaJobRepository,
            CandidateRepository candidateRepository,
            CandidateVisaRepository candidateVisaRepository,
            OccupationRepository occupationRepository,
            SalesforceJobOppRepository salesforceJobOppRepository) {
        this.candidateVisaJobRepository = candidateVisaJobRepository;
        this.candidateRepository = candidateRepository;
        this.candidateVisaRepository = candidateVisaRepository;
        this.occupationRepository = occupationRepository;
        this.salesforceJobOppRepository = salesforceJobOppRepository;
    }

    @Override
    public CandidateVisaJobCheck getVisaJobCheck(long visaJobId)
            throws NoSuchObjectException {

        return candidateVisaJobRepository.findById(visaJobId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateVisaJobCheck.class, visaJobId));
    }

    @Override
    public CandidateVisaJobCheck createVisaJobCheck(
            long visaId, CreateCandidateVisaJobCheckRequest request)
            throws NoSuchObjectException {

        CandidateVisaCheck visaCheck = candidateVisaRepository.findById(visaId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateVisaCheck.class, visaId));

        CandidateVisaJobCheck jobCheck = new CandidateVisaJobCheck();
        jobCheck.setCandidateVisaCheck(visaCheck);

        SalesforceJobOpp jobOpp = salesforceJobOppRepository.findById(request.getJobOppId())
                .orElseThrow(() -> new NoSuchObjectException(SalesforceJobOpp.class, request.getJobOppId()));
        jobCheck.setJobOpp(jobOpp);

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
            Long visaJobId, @NonNull CandidateVisaCheckData data) throws NoSuchObjectException {
        CandidateVisaJobCheck cvj;
        cvj = candidateVisaJobRepository.findById(visaJobId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateVisaJobCheck.class, visaJobId));

        Occupation occupation = null;
        if (data.getVisaJobOccupationId() != null) {
            occupation = occupationRepository.findById(data.getVisaJobOccupationId())
                    .orElseThrow(() -> new NoSuchObjectException(Occupation.class, data.getVisaJobOccupationId()));
        }
        cvj.populateIntakeData(occupation, data);
        candidateVisaJobRepository.save(cvj);

    }
}
