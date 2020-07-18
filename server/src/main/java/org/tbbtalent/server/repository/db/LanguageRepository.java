/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.Language;
import org.tbbtalent.server.model.db.Status;

public interface LanguageRepository extends JpaRepository<Language, Long>, JpaSpecificationExecutor<Language> {

    @Query(" select l from Language l "
            + " where l.status = :status order by l.name asc")
    List<Language> findByStatus(@Param("status") Status status);

    @Query(" select distinct l from Language l "
            + " where lower(l.name) = lower(:name)"
            + " and l.status != 'deleted' order by l.name asc" )
    Language findByNameIgnoreCase(@Param("name") String name);
}
