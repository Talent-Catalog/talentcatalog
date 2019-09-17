package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Language;

import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, Long> {

    @Query(" select l from Language l "
            + " left join l.candidate c "
            + " where l.id = :id")
    Optional<Language> findByIdLoadCandidate(@Param("id") Long id);
}
