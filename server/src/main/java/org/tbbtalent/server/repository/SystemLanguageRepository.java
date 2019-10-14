package org.tbbtalent.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.model.SystemLanguage;

public interface SystemLanguageRepository extends JpaRepository<SystemLanguage, Long>, JpaSpecificationExecutor<SystemLanguage> {

    @Query(" select l from SystemLanguage l "
            + " where l.status = :status")
    List<SystemLanguage> findByStatus(@Param("status") Status status);

}
