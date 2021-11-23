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

package org.tbbtalent.server.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.CandidateTodo;

import java.util.List;
import java.util.Optional;

public interface CandidateTodoRepository extends JpaRepository<CandidateTodo, Long> {
    @Query(" select t from CandidateTodo t "
            + " where t.candidate.id = :candidateId")
    List<CandidateTodo> findByCandidateId(@Param("candidateId") Long candidateId);

    @Query(" select t from CandidateTodo t "
            + " where t.candidate.id = :candidateId"
            + " and t.name = :name")
    Optional<CandidateTodo> findByCandidateAndName(@Param("candidateId") Long candidateId, @Param("name") String name);
}
