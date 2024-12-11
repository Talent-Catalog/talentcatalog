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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.model.db.CandidateVisaJobCheck;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateVisaRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.candidate.visa.CandidateVisaCheckData;
import org.tctalent.server.request.candidate.visa.CreateCandidateVisaCheckRequest;
import org.tctalent.server.service.db.CandidateVisaJobCheckService;
import org.tctalent.server.service.db.CandidateVisaService;

/**
 * Manage candidate visa checks
 *
 * @author John Cameron
 */
@Service
public class CandidateVisaServiceImpl implements CandidateVisaService {
    private final CandidateVisaRepository candidateVisaRepository;
    private final CandidateRepository candidateRepository;
    private final CountryRepository countryRepository;

    private final CandidateVisaJobCheckService candidateVisaJobCheckService;

    public CandidateVisaServiceImpl(
            CandidateVisaRepository candidateVisaRepository,
            CandidateRepository candidateRepository,
            CountryRepository countryRepository,
            CandidateVisaJobCheckService candidateVisaJobCheckService) {
        this.candidateVisaRepository = candidateVisaRepository;
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
        this.candidateVisaJobCheckService = candidateVisaJobCheckService;
    }

    @Override
    public CandidateVisaCheck getVisaCheck(long visaId)
            throws NoSuchObjectException {

        return candidateVisaRepository.findById(visaId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateVisaJobCheck.class, visaId));
    }

    @Override
    public List<CandidateVisaCheck> listCandidateVisaChecks(long candidateId) {
        return candidateVisaRepository.findByCandidateId(candidateId);
    }

    @Override
    public CandidateVisaCheck createVisaCheck(
            long candidateId, CreateCandidateVisaCheckRequest request)
            throws NoSuchObjectException, EntityExistsException {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));

        final Long countryId = request.getCountryId();
        CandidateVisaCheck existing = candidateVisaRepository.findByCandidateIdCountryId(candidateId, countryId).orElse(null);

        if (existing != null) {
            throw new EntityExistsException("Visa Check", "There is already a visa check associated with this country.");
        }

        CandidateVisaCheck cv = new CandidateVisaCheck();
        cv.setCandidate(candidate);
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new NoSuchObjectException(Country.class, countryId));
        cv.setCountry(country);
        return candidateVisaRepository.save(cv);
    }

    @Override
    public boolean deleteVisaCheck(long visaId)
            throws EntityReferencedException, InvalidRequestException {
        try {
            candidateVisaRepository.deleteById(visaId);
        } catch (Exception e) {
            throw new InvalidRequestException("There are job specific visa checks associated with this country's visa check, " +
                    "cannot delete unless this country's job specific visa checks are deleted first.");
        }
        return true;
    }

    @Override
    public void updateIntakeData(
        Long visaId, @NonNull CandidateVisaCheckData data) throws NoSuchObjectException {
        CandidateVisaCheck cv;
        cv = candidateVisaRepository.findById(visaId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateVisaCheck.class, visaId));

        // If there is a non null visa job id, this is a visa job update.
        final Long visaJobId = data.getVisaJobId();
        if (visaJobId != null) {
            candidateVisaJobCheckService.updateIntakeData(visaJobId, data);
        }

        cv.populateIntakeData(data);
        candidateVisaRepository.save(cv);

    }
}
