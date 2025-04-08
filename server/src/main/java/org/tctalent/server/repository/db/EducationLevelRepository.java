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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.Status;

public interface EducationLevelRepository extends JpaRepository<EducationLevel, Long>, JpaSpecificationExecutor<EducationLevel> {

    @Query(" select l from EducationLevel l "
            + " where l.status = :status order by l.level")
    List<EducationLevel> findByStatus(@Param("status") Status status);

    @Query(" select distinct l from EducationLevel l "
            + " where lower(l.name) = lower(:name)"
            + " and l.status != 'deleted' order by l.level" )
    EducationLevel findByNameIgnoreCase(@Param("name") String name);


    @Query(" select distinct l from EducationLevel l "
            + " where l.level = :level"
            + " and l.status != 'deleted'" )
    EducationLevel findByLevelIgnoreCase(@Param("level") int level);

    @Query(" select distinct l from EducationLevel l "
            + " where l.status != 'active'" )
    List<EducationLevel> findAllActive();

    Optional<EducationLevel> findByLevel(int level);
}
