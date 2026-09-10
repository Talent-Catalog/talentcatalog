/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.util.textExtract.IdAndScore;

/**
 * Service interface for fetching Candidate DTOs from the database.
 * <p>
 *     Supports caching of CandidateDTOs.
 * </p>
 * <p>
 *     CandidateDTOs are different to Candidate entities in that they are intended to be
 *     used as read-only objects. They can be changed but there will be no side effects,
 *     unlike Candidate entities where changes typically generate SQL requests to update the
 *     associated database.
 * </p>
 *
 * @author John Cameron
 */
public interface CandidateDtoFetchService {

    /**
     * Fetches a page of Candidate DTOs from the database by executing the given SQL queries.
     * @param fetchIdsSql Sql which just returns the ids (and possible ranks) of the candidates
     *                    to be fetched
     * @param countSql Sql which returns the total number of candidates matching the query
     * @param pageRequest Page request specifying the page number and page size and sort (if any).
     * @return Page of Candidate DTOs
     */
    Page<CandidateReadDto> fetchPage(
        String fetchIdsSql, String countSql, @NonNull PageRequest pageRequest);

    /**
     * Fetches Candidate DTOs into a Map of ids to CandidateReadDto objects
     * for the given candidate IDs.
     *
     * @param ids Ids of candidates to be fetched
     * @return Map of candidate ids to CandidateDTOs
     * @throws NoSuchObjectException if any of the ids are bad - ie do not correspond a candidate.
     */
    @NonNull
    Map<Long, CandidateReadDto> fetchByIds(Collection<Long> ids) throws NoSuchObjectException;

    /**
     * Fetches the candidates in the order specified by the idAndScores and sets their scores.
     * @param idAndScores Ordered list of candidate ids to be fetched - plus their scores.
     * @return Ordered list of candidates, enriched with their scores.
     */
    @NonNull
    List<CandidateReadDto> fetchAndSetScores(List<IdAndScore> idAndScores);

}
