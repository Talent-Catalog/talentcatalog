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
import org.tbbtalent.server.model.db.CandidateDependant;
import org.tbbtalent.server.repository.db.CandidateDependantRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.dependant.CreateCandidateDependantRequest;
import org.tbbtalent.server.service.db.CandidateDependantService;

/**
 * Manage candidate destinations
 *
 * @author John Cameron
 */
@Service
public class CandidateDependantServiceImpl implements CandidateDependantService {
    private final CandidateDependantRepository candidateDependantRepository;
    private final CandidateRepository candidateRepository;
    private final CountryRepository countryRepository;

    public CandidateDependantServiceImpl(
            CandidateDependantRepository candidateDependantRepository,
            CandidateRepository candidateRepository,
            CountryRepository countryRepository) {
        this.candidateDependantRepository = candidateDependantRepository;
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    public CandidateDependant createDependant(
            long candidateId, CreateCandidateDependantRequest request)
            throws NoSuchObjectException {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));

        CandidateDependant cd = new CandidateDependant();
        cd.setCandidate(candidate);

        cd.setRelation(request.getRelation());
        cd.setDob(request.getDob());
        cd.setHealthConcern(request.getHealthConcern());
        cd.setHealthNotes(request.getHealthNotes());

        cd = candidateDependantRepository.save(cd);
        Long noOfObjects = candidateDependantRepository.countByCandidateId(candidateId);
        candidate.setNumberDependants(noOfObjects);
        return cd;
    }

    @Override
    public Candidate deleteDependant(long dependantId)
            throws EntityReferencedException, InvalidRequestException {
        CandidateDependant cd;
        cd = candidateDependantRepository.findById(dependantId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateDependant.class, dependantId));
        Candidate candidate = cd.getCandidate();
        candidateDependantRepository.deleteById(dependantId);

        Long noOfObjects = candidateDependantRepository.countByCandidateId(cd.getCandidate().getId());
        if (noOfObjects == 0) {
            candidate.setNumberDependants(null);
        } else {
            candidate.setNumberDependants(noOfObjects);
        }
        return candidate;
    }

    @Override
    public void updateIntakeData(@NonNull Candidate candidate, CandidateIntakeDataUpdate data)
            throws NoSuchObjectException {

        CandidateDependant cd;
        Long id = data.getDependantId();
        cd = candidateDependantRepository.findById(id)
                    .orElseThrow(() -> new NoSuchObjectException(CandidateDependant.class, id));

        cd.populateIntakeData(candidate, data);
        candidateDependantRepository.save(cd);
    }
}
