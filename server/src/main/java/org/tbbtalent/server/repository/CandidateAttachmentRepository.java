package org.tbbtalent.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.tbbtalent.server.model.CandidateAttachment;

import java.util.List;
import java.util.Optional;

public interface CandidateAttachmentRepository extends JpaRepository<CandidateAttachment, Long> {

    @Query(" select distinct a from CandidateAttachment a "
            + " left join a.createdBy u "
            + " left join a.candidate c "
            + " where c.id = ?1 ")
    List<CandidateAttachment> findByCandidateIdLoadAudit(Long candidateId);

    Page<CandidateAttachment> findByCandidateId(Long candidateId, Pageable request);

    @Query(" select distinct a from CandidateAttachment a "
            + " left join a.candidate c "
            + " where a.id = ?1 ")
    Optional<CandidateAttachment> findByIdLoadCandidate(Long id);
}
