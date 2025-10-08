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

import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.CandidatePropertyKey;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.task.TaskAssignment;
import org.tctalent.server.repository.db.CandidatePropertyRepository;
import org.tctalent.server.service.db.CandidatePropertyService;

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

    @Nullable
    public CandidateProperty findProperty(@NonNull Candidate candidate, @NonNull String name) {

        CandidatePropertyKey key = new CandidatePropertyKey(candidate.getId(), name);
        final Optional<CandidateProperty> oProp = candidatePropertyRepository.findById(key);

        return oProp.orElse(null);
    }

    @Override
    public CandidateProperty createOrUpdateProperty(@NonNull Candidate candidate,
        @NonNull String name, @Nullable String value, @Nullable TaskAssignment taskAssignment) {

        CandidateProperty property = findProperty(candidate, name);
        if (property == null) {
            property = new CandidateProperty();
            CandidatePropertyKey key = new CandidatePropertyKey(candidate.getId(), name);
            property.setId(key);
            property.setCandidate(candidate);
            property.setName(name);
        }
        property.setValue(value);
        property.setRelatedTaskAssignment((TaskAssignmentImpl) taskAssignment);
        return candidatePropertyRepository.save(property);
    }

    @Override
    public void deleteProperty(@NonNull Candidate candidate, @NonNull String name) {
        candidatePropertyRepository.deleteById(new CandidatePropertyKey(candidate.getId(), name));
    }
}
