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
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.Occupation;

public interface CandidateOccupationRepository extends JpaRepository<CandidateOccupation, Long> {

    @Query(" select p from CandidateOccupation p "
            + " left join p.candidate c "
            + " where p.id = :id")
    Optional<CandidateOccupation> findByIdLoadCandidate(@Param("id") Long id);

    @Query(value = " select co from CandidateOccupation co "
            + " left join co.candidate c "
            + " left join co.occupation o "
            + " where c.id = :candidateId")
    List<CandidateOccupation> findByCandidateIdLoadOccupation(@Param("candidateId") Long candidateId);

    @Query(" select co from CandidateOccupation co "
            + " where co.occupation.id = :occupationId ")
    List<CandidateOccupation> findByOccupationId(@Param("occupationId") Long occupationId);

    @Query("select distinct o from CandidateOccupation co"
            + " join co.occupation o order by o.name asc")
    List<Occupation> findAllOccupations();

    @Query(" select o from CandidateOccupation o "
            + " where o.candidate.id = :candidateId")
    List<CandidateOccupation> findByCandidateId(@Param("candidateId") Long candidateId);

    @Query(" select co from CandidateOccupation co "
            + " where co.candidate.id = :candidateId"
            + " and co.occupation.id = :occupationId")
    CandidateOccupation findByCandidateIdAAndOccupationId(@Param("candidateId") Long candidateId,
                                                          @Param("occupationId") Long occupationId);
}
