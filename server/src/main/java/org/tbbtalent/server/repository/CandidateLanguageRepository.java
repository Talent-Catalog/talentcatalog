package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.CandidateLanguage;

import java.util.List;
import java.util.Optional;

public interface CandidateLanguageRepository extends JpaRepository<CandidateLanguage, Long> {

    @Query(" select l from CandidateLanguage l "
            + " left join l.candidate c "
            + " where l.id = :id")
    Optional<CandidateLanguage> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select l from CandidateLanguage l "
            + " where l.language.id = :languageId ")
    List<CandidateLanguage> findByLanguageId(@Param("languageId") Long languageId);

    @Query(" select l from CandidateLanguage l "
            + " where l.writtenLevel.id = :languageLevelId "
            + " or l.spokenLevel.id = :languageLevelId ")
    List<CandidateLanguage> findByLanguageLevelId(@Param("languageLevelId") Long languageId);
}
