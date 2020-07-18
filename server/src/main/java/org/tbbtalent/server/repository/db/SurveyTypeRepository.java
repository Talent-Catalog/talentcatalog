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
import org.tbbtalent.server.model.db.SurveyType;

public interface SurveyTypeRepository extends JpaRepository<SurveyType, Long>, JpaSpecificationExecutor<SurveyType> {

    @Query(" select s from SurveyType s "
            + " where s.status = :status order by s.name asc")
    List<SurveyType> findByStatus(@Param("status") Status status);

}
