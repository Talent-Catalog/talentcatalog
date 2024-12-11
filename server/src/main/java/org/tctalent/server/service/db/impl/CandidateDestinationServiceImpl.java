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
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDestination;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.repository.db.CandidateDestinationRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tctalent.server.request.candidate.destination.CreateCandidateDestinationRequest;
import org.tctalent.server.request.candidate.destination.UpdateCandidateDestinationRequest;
import org.tctalent.server.service.db.CandidateDestinationService;

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
        CandidateDestination cd = new CandidateDestination();

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));
        cd.setCandidate(candidate);

        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));
        cd.setCountry(country);

        cd.setInterest(request.getInterest());
        cd.setNotes(request.getNotes());

        return candidateDestinationRepository.save(cd);
    }

    @Override
    public CandidateDestination updateDestination(
            long id, UpdateCandidateDestinationRequest request)
            throws NoSuchObjectException {

        CandidateDestination cd = candidateDestinationRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateDestination.class, id));

        cd.setInterest(request.getInterest());
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

    @Override
    public List<CandidateDestination> list(long candidateId) {
        return candidateDestinationRepository.findByCandidateId(candidateId);
    }
}
