package org.tbbtalent.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.CandidateNote;

public interface CandidateNoteRepository extends JpaRepository<CandidateNote, Long> {


    Page<CandidateNote> findByCandidateId(Long candidateId, Pageable request);
}
