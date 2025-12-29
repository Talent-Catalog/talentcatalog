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

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collection;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;

/**
 * Service interface for managing Candidate DTOs.
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
     * Loads Candidate DTOs into a Map of ids to CandidateReadDto objects 
     * for the given candidate IDs.
     * @param ids Ids of candidates to be fetched
     * @return Map of candidate ids to CandidateDTOs
     */
    @NonNull
    Map<Long, CandidateReadDto> loadByIds(Collection<Long> ids) throws JsonProcessingException;

}
