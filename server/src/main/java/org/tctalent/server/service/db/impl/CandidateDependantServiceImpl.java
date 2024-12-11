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
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDependant;
import org.tctalent.server.repository.db.CandidateDependantRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tctalent.server.request.candidate.dependant.CreateCandidateDependantRequest;
import org.tctalent.server.service.db.CandidateDependantService;

/**
 * Manage candidate destinations
 *
 * @author John Cameron
 */
@Service
@AllArgsConstructor
public class CandidateDependantServiceImpl implements CandidateDependantService {
    private final CandidateDependantRepository candidateDependantRepository;
    private final CandidateRepository candidateRepository;

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
        return cd;
    }

    @Override
    public Candidate deleteDependant(long dependantId)
            throws EntityReferencedException, InvalidRequestException {
        CandidateDependant cd;
        cd = candidateDependantRepository.findById(dependantId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateDependant.class, dependantId));
        Long candidateId = cd.getCandidate().getId();
        candidateDependantRepository.deleteById(dependantId);

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));
        return candidate;
    }

    @Override
    public List<CandidateDependant> list(long candidateId) {
        return candidateDependantRepository.findByCandidateId(candidateId);
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

    @Override
    public CandidateDependant getDependant(long dependantId)
        throws NoSuchObjectException {
        CandidateDependant candidateDependant;
        candidateDependant = candidateDependantRepository.findById(dependantId)
            .orElseThrow(() -> new NoSuchObjectException(CandidateDependant.class, dependantId));

        return candidateDependant;
    }

}
