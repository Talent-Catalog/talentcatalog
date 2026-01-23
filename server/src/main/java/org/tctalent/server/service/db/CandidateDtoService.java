/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.service.db;

import org.springframework.lang.Nullable;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;

/**
 * Service interface for processing Candidate DTOs.
 * <p>
 *     CandidateDTOs are different to Candidate entities in that they are intended to be
 *     used as read-only objects. They can be changed but there will be no side effects,
 *     unlike Candidate entities where changes typically generate SQL requests to update the
 *     associated database.
 * </p>
 *
 * @author John Cameron
 */
public interface CandidateDtoService {


    /**
     * Populates computed fields on the given candidates.
     * @param candidates Candidates to be updated
     * @param savedListId Optional id of a saved list serving as the "context" for
     *                    candidates in that list.
     *                    Some populated fields - such as "context notes" are populated
     *                    in the context of a saved list. Candidates who appear in this savedList
     *                    will have their "context" fields populated.
     */
    void populateComputedFields(Iterable<CandidateReadDto> candidates, @Nullable Long savedListId);
}
