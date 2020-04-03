package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.model.SurveyType;

import java.util.List;

public interface SurveyTypeRepository extends JpaRepository<SurveyType, Long>, JpaSpecificationExecutor<SurveyType> {

    @Query(" select s from SurveyType s "
            + " where s.status = :status order by s.name asc")
    List<SurveyType> findByStatus(@Param("status") Status status);

}
