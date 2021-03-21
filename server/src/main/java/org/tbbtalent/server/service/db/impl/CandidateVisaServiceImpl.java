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
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CandidateVisaRepository;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.visa.CreateCandidateVisaCheckRequest;
import org.tbbtalent.server.service.db.CandidateVisaService;

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
    private final UserRepository userRepository;

    public CandidateVisaServiceImpl(
            CandidateVisaRepository candidateVisaRepository,
            CandidateRepository candidateRepository,
            CountryRepository countryRepository, UserRepository userRepository) {
        this.candidateVisaRepository = candidateVisaRepository;
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CandidateVisaCheck createVisaCheck(
            long candidateId, CreateCandidateVisaCheckRequest request) 
            throws NoSuchObjectException {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));

        CandidateVisaCheck cv = new CandidateVisaCheck();
        cv.setCandidate(candidate);

        final Long countryId = request.getCountryId();
        if (countryId != null) {
            Country country = countryRepository.findById(countryId)
                    .orElseThrow(() -> new NoSuchObjectException(Country.class, countryId));
            cv.setCountry(country);
        }
        //cv.setEligibility(request.getEligibility());
        cv.setAssessmentNotes(request.getAssessmentNotes());

        return candidateVisaRepository.save(cv);
    }

    @Override
    public boolean deleteVisaCheck(long visaId) 
            throws EntityReferencedException, InvalidRequestException {
        candidateVisaRepository.deleteById(visaId);
        return true;
    }

    @Override
    public void updateIntakeData(
            Long visaId, @NonNull Candidate candidate,
            CandidateIntakeDataUpdate data) throws NoSuchObjectException {
        if (data.getVisaCountryId() != null) {
            Country country = countryRepository.findById(data.getVisaCountryId())
                    .orElseThrow(() -> new NoSuchObjectException(Country.class, data.getVisaCountryId()));

            User createdBy = null;
            //final Long createdById = data.getVisaCreatedById();
//            if (createdById != null) {
//                createdBy = userRepository.findById(createdById)
//                    .orElseThrow(() -> new NoSuchObjectException(User.class, createdById));
//            }
            
            CandidateVisaCheck cv;
            cv = candidateVisaRepository.findById(visaId)
                    .orElseThrow(() -> new NoSuchObjectException(CandidateVisaCheck.class, visaId));
            cv.populateIntakeData(candidate, country, data, createdBy);
            candidateVisaRepository.save(cv);
        }
    }
}
