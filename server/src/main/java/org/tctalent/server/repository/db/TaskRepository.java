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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.TaskImpl;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskImpl, Long>, JpaSpecificationExecutor<TaskImpl> {
    List<TaskImpl> findByName(String name);

    @Query("select distinct t from Task t "
            + " where lower(t.name) = lower(:name) ")
    Optional<TaskImpl> findByLowerName(@Param("name") String name);

    @Query(" select distinct t from Task t "
            + " where lower(t.displayName) = lower(:displayName)")
    TaskImpl findByLowerDisplayName(@Param("displayName") String displayName);
}
