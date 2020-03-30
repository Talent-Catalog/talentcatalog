package org.tbbtalent.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.CandidateSurvey;

public interface CandidateSurveyRepository extends JpaRepository<CandidateSurvey, Long> {

    Page<CandidateSurvey> findByCandidateId(Long candidateId, Pageable request);
}