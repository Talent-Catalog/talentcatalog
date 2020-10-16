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
import org.tbbtalent.server.model.db.CandidateDestination;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.repository.db.CandidateDestinationRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.destination.CreateCandidateDestinationRequest;
import org.tbbtalent.server.service.db.CandidateDestinationService;

/**
 * Manage candidate destinations
 *
 * @author John Cameron
 */
@Service
public class CandidateDestinationServiceImpl implements CandidateDestinationService {
    private final CandidateDestinationRepository candidateDestinationRepository;
    private final CandidateRepository candidateRepository;
    private final CountryRepository countryRepository;

    public CandidateDestinationServiceImpl(
            CandidateDestinationRepository candidateDestinationRepository,
            CandidateRepository candidateRepository,
            CountryRepository countryRepository) {
        this.candidateDestinationRepository = candidateDestinationRepository;
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    public CandidateDestination createDestination(
            long candidateId, CreateCandidateDestinationRequest request)
            throws NoSuchObjectException {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));

        CandidateDestination cd = new CandidateDestination();
        cd.setCandidate(candidate);

        cd.setCountry(request.getCountry());
        cd.setInterest(request.getInterest());
        cd.setFamily(request.getFamily());
        cd.setLocation(request.getLocation());
        cd.setNotes(request.getNotes());

        return candidateDestinationRepository.save(cd);
    }

    @Override
    public boolean deleteDestination(long destinationId)
            throws EntityReferencedException, InvalidRequestException {
        candidateDestinationRepository.deleteById(destinationId);
        return true;
    }

    @Override
    public void updateIntakeData(
            Long countryId, @NonNull Candidate candidate, CandidateIntakeDataUpdate data)
            throws NoSuchObjectException {
        if (countryId != null) {
            Country country = countryRepository.findById(countryId)
                    .orElseThrow(() -> new NoSuchObjectException(Country.class, countryId));

            CandidateDestination cd;
            Long id = data.getDestinationId();
            cd = candidateDestinationRepository.findById(id)
                        .orElseThrow(() -> new NoSuchObjectException(CandidateDestination.class, id));

            cd.populateIntakeData(candidate, country, data);
            candidateDestinationRepository.save(cd);
        }
    }
}
