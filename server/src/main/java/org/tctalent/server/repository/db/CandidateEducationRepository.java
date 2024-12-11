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
import org.tctalent.server.model.db.CandidateEducation;

public interface CandidateEducationRepository extends JpaRepository<CandidateEducation, Long> {

    // TO DO ADD EDUCATION LEVEL TO CANDIDATE EDUCATION TABLE
//    @Query(" select e from CandidateEducation e "
//            + " where e.educationLevel.id = :id ")
//    List<CandidateEducation> findByEducationLevelId(@Param("id") Long id);

    @Query(" select e from CandidateEducation e "
            + " left join e.candidate c "
            + " where e.id = :id")
    Optional<CandidateEducation> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select e from CandidateEducation e "
            + " left join e.country "
            + " left join e.educationMajor "
            + " where e.candidate.id = :candidateId ")
    List<CandidateEducation> findByCandidateId(@Param("candidateId") Long candidateId);

    @Query(" select e from CandidateEducation e "
            + " left join e.candidate c "
            + " where e.id = :id "
            + " and c.id = :candidateId ")
    CandidateEducation findByIdAndCandidateId(@Param("id") Long id,
                                              @Param("candidateId") Long candidateId);
}
