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

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.EducationType;

public interface EducationRepository extends JpaRepository<CandidateEducation, Long> {

    @Query(" select e from CandidateEducation e "
            + " left join e.candidate c "
            + " where e.id = :id")
    Optional<CandidateEducation> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select e from CandidateEducation e "
            + " where e.educationType = :educationType")
    CandidateEducation findByIdLoadEducationType(@Param("educationType") EducationType educationType);
}
