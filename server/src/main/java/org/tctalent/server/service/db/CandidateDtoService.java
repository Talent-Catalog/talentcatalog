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
import java.util.List;
import org.tctalent.server.api.dto.CandidateDto;

/**
 * Service interface for managing Candidate DTOs.
 * <p>
 *     CandidateDTOs are different to Candidate entities in that are read-only.
 * </p>
 *
 * @author John Cameron
 */
public interface CandidateDtoService {

    /**
     * Returns a list of CandidateDTOs for the given candidate IDs.
     * @param ids Ids of candidates to be fetched
     * @return List of CandidateDTOs
     */
    List<CandidateDto> findByIds(Collection<Long> ids) throws JsonProcessingException;

}
