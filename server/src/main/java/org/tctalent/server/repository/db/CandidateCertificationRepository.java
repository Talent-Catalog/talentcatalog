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

package org.tctalent.server.repository.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.CandidateCertification;

public interface CandidateCertificationRepository extends JpaRepository<CandidateCertification, Long> {

    @Query(" select f from CandidateCertification f "
            + " left join f.candidate c "
            + " where f.id = :id")
    Optional<CandidateCertification> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select f from CandidateCertification f "
            + " where f.candidate.id = :candidateId ")
    List<CandidateCertification> findByCandidateId(@Param("candidateId") Long candidateId);
}
