/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.Industry;
import org.tbbtalent.server.model.db.Status;

public interface IndustryRepository extends JpaRepository<Industry, Long>, JpaSpecificationExecutor<Industry> {

    @Query(" select i from Industry i "
            + " where i.status = :status")
    List<Industry> findByStatus(@Param("status") Status status);

    @Query(" select distinct i from Industry i "
            + " where lower(i.name) = lower(:name)"
            + " and i.status != 'deleted'" )
    Industry findByNameIgnoreCase(@Param("name") String name);
}
