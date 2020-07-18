/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.SystemLanguage;

public interface SystemLanguageRepository extends JpaRepository<SystemLanguage, Long>, JpaSpecificationExecutor<SystemLanguage> {

    @Query(" select l from SystemLanguage l "
            + " where l.status = :status")
    List<SystemLanguage> findByStatus(@Param("status") Status status);

}
