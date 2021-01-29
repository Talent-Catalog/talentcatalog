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

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.Nationality;
import org.tbbtalent.server.model.db.Status;

public interface NationalityRepository extends JpaRepository<Nationality, Long>, JpaSpecificationExecutor<Nationality> {

    @Query(" select n from Nationality n "
            + " where n.status = :status order by n.name asc")
    List<Nationality> findByStatus(@Param("status") Status status);

    @Query(" select distinct n from Nationality n "
            + " where lower(n.name) = lower(:name)"
            + " and n.status != 'deleted' order by n.name asc" )
    Nationality findByNameIgnoreCase(@Param("name") String name);

    @Query(" select n.name from Nationality n "
            + " where id in (:ids) order by n.name asc" )
    List<String> getNamesForIds(@Param("ids") List<Long> ids);}
