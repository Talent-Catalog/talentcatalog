package org.tbbtalent.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.CandidateShortlistItem;

public interface CandidateShortlistRepository extends JpaRepository<CandidateShortlistItem, Long> {

    Page<CandidateShortlistItem> findByCandidateId(Long candidateId, Pageable request);
}
