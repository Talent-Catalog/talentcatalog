package org.tbbtalent.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.CandidateAttachment;

public interface CandidateAttachmentRepository extends JpaRepository<CandidateAttachment, Long> {


    Page<CandidateAttachment> findByCandidateId(Long candidateId, Pageable request);
}
