package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.EducationLevel;
import org.tbbtalent.server.model.LanguageLevel;
import org.tbbtalent.server.model.Status;

import java.util.List;

public interface LanguageLevelRepository extends JpaRepository<LanguageLevel, Long> {

    @Query(" select l from LanguageLevel l "
            + " where l.status = :status")
    List<LanguageLevel> findByStatus(@Param("status") Status status);
}
