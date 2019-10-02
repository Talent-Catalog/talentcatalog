package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.model.Status;

import java.util.List;

public interface LanguageRepository extends JpaRepository<Language, Long>, JpaSpecificationExecutor<Language> {

    @Query(" select l from Language l "
            + " where l.status = :status")
    List<Language> findByStatus(@Param("status") Status status);

    @Query(" select distinct l from Language l "
            + " where lower(l.name) = lower(:name)"
            + " and l.status != 'deleted'" )
    Language findByNameIgnoreCase(@Param("name") String name);
}
