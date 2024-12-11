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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.Status;

public interface EducationMajorRepository extends JpaRepository<EducationMajor, Long>, JpaSpecificationExecutor<EducationMajor> {

    @Query(" select m from EducationMajor m "
            + " where m.status = :status")
    List<EducationMajor> findByStatus(@Param("status") Status status);

    @Query(" select distinct m from EducationMajor m "
            + " where lower(m.name) = lower(:name)"
            + " and m.status != 'deleted'" )
    EducationMajor findByNameIgnoreCase(@Param("name") String name);

    @Query(" select m.name from EducationMajor m "
            + " where m.id in (:ids) order by m.name asc" )
    List<String> getNamesForIds(@Param("ids") List<Long> ids);
}
