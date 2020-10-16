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
import org.tbbtalent.server.model.db.CandidateVisa;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CandidateVisaRepository;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.visa.CreateCandidateVisaRequest;
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

    public CandidateVisaServiceImpl(
            CandidateVisaRepository candidateVisaRepository, 
            CandidateRepository candidateRepository, 
            CountryRepository countryRepository) {
        this.candidateVisaRepository = candidateVisaRepository;
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    public CandidateVisa createVisa(
            long candidateId, CreateCandidateVisaRequest request) 
            throws NoSuchObjectException {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));

        CandidateVisa cv = new CandidateVisa();
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
    public boolean deleteVisa(long visaId) 
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

            CandidateVisa cv;
            Long id = data.getVisaId();
            if (id == null) {
                //Create
                cv = new CandidateVisa();
            } else {
                //Update
                cv = candidateVisaRepository.findById(id)
                        .orElseThrow(() -> new NoSuchObjectException(CandidateVisa.class, id));
            }
            cv.populateIntakeData(candidate, country, data);
            candidateVisaRepository.save(cv);
        }
    }
}
