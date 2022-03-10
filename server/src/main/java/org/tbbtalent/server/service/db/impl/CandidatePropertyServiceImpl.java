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

import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateProperty;
import org.tbbtalent.server.model.db.CandidatePropertyKey;
import org.tbbtalent.server.model.db.TaskAssignmentImpl;
import org.tbbtalent.server.repository.db.CandidatePropertyRepository;
import org.tbbtalent.server.service.db.CandidatePropertyService;

/**
 * Manage CandidateProperty's
 *
 * @author John Cameron
 */
@Service
public class CandidatePropertyServiceImpl implements CandidatePropertyService {
    private final CandidatePropertyRepository candidatePropertyRepository;

    public CandidatePropertyServiceImpl(CandidatePropertyRepository candidatePropertyRepository) {
        this.candidatePropertyRepository = candidatePropertyRepository;
    }

    @Override
    public CandidateProperty createOrUpdateProperty(@NonNull Candidate candidate,
        @NonNull String name, @Nullable String value, @Nullable TaskAssignmentImpl taskAssignment) {

        CandidatePropertyKey key = new CandidatePropertyKey(candidate.getId(), name);

        CandidateProperty property;

        final Optional<CandidateProperty> byId = candidatePropertyRepository.findById(key);
        if (byId.isPresent()) {
            property = byId.get();
            property.setValue(value);
            property.setRelatedTaskAssignment(taskAssignment);
        } else {
            property = new CandidateProperty();
            property.setCandidateId(candidate.getId());
            property.setName(name);
            property.setValue(value);
            property.setRelatedTaskAssignment(taskAssignment);

        }
        return candidatePropertyRepository.save(property);
    }

    @Override
    public void deleteProperty(@NonNull Candidate candidate, @NonNull String name) {
        candidatePropertyRepository.deleteById(new CandidatePropertyKey(candidate.getId(), name));
    }
}
