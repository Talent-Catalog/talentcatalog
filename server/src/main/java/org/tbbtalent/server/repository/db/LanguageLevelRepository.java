/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.LanguageLevel;
import org.tbbtalent.server.model.db.Status;

public interface LanguageLevelRepository extends JpaRepository<LanguageLevel, Long>, JpaSpecificationExecutor<LanguageLevel> {

    @Query(" select l from LanguageLevel l "
            + " where l.status = :status")
    List<LanguageLevel> findByStatus(@Param("status") Status status);

    @Query(" select distinct l from LanguageLevel l "
            + " where lower(l.name) = lower(:name)"
            + " and l.status != 'deleted'" )
    LanguageLevel findByNameIgnoreCase(@Param("name") String name);


    @Query(" select distinct l from LanguageLevel l "
            + " where l.level = :level"
            + " and l.status != 'deleted'" )
    LanguageLevel findByLevelIgnoreCase(@Param("level") int level);

    @Query(" select distinct l from LanguageLevel l "
            + " where l.status = 'active'" )
    List<LanguageLevel> findAllActive();
}
