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
        cd.setNotes(request.getNotes());

        return candidateDependantRepository.save(cd);
    }

    @Override
    public boolean deleteDependant(long dependantId)
            throws EntityReferencedException, InvalidRequestException {
        candidateDependantRepository.deleteById(dependantId);
        return true;
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
