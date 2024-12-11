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
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.Status;

public interface OccupationRepository extends JpaRepository<Occupation, Long>, JpaSpecificationExecutor<Occupation> {

    @Query(" select o from Occupation o "
            + " where o.status = :status order by o.name asc")
    List<Occupation> findByStatus(@Param("status") Status status);

    @Query(" select distinct o from Occupation o "
            + " where lower(o.name) = lower(:name)"
            + " and o.status != 'deleted' order by o.name asc" )
    Occupation findByNameIgnoreCase(@Param("name") String name);

    @Query(" select o.name from Occupation o "
            + " where o.id in (:ids) order by o.name asc" )
    List<String> getNamesForIds(@Param("ids") List<Long> ids);
}
