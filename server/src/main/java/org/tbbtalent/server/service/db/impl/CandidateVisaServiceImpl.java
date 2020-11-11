/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
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
        cv.setEligibility(request.getEligibility());
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
            Long countryId, @NonNull Candidate candidate, 
            CandidateIntakeDataUpdate data) throws NoSuchObjectException {
        if (countryId != null) {
            Country country = countryRepository.findById(countryId)
                    .orElseThrow(() -> new NoSuchObjectException(Country.class, countryId));

            User checkedBy = null;
            final Long checkedById = data.getVisaCheckedById();
            if (checkedById != null) {
                checkedBy = userRepository.findById(checkedById)
                    .orElseThrow(() -> new NoSuchObjectException(User.class, checkedById));
            }
            
            CandidateVisaCheck cv;
            Long id = data.getVisaId();
            cv = candidateVisaRepository.findById(id)
                    .orElseThrow(() -> new NoSuchObjectException(CandidateVisaCheck.class, id));
            cv.populateIntakeData(candidate, country, data, checkedBy);
            candidateVisaRepository.save(cv);
        }
    }
}
