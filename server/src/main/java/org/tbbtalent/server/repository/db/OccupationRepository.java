/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.Occupation;
import org.tbbtalent.server.model.db.Status;

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
