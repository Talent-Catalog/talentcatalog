package org.tbbtalent.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.CandidateReviewStatusItem;

public interface CandidateReviewStatusRepository extends JpaRepository<CandidateReviewStatusItem, Long> {

    Page<CandidateReviewStatusItem> findByCandidateId(Long candidateId, Pageable request);
}
