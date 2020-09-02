/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.CandidateAttachment;

import java.util.List;
import java.util.Optional;

public interface CandidateAttachmentRepository extends JpaRepository<CandidateAttachment, Long> {

    @Query(" select distinct a from CandidateAttachment a "
            + " left join a.createdBy u "
            + " left join a.candidate c "
            + " where c.id = :candidateId "
            + " and a.adminOnly = false ")
    List<CandidateAttachment> findByCandidateIdLoadAudit(@Param("candidateId") Long candidateId);

    Page<CandidateAttachment> findByCandidateId(Long candidateId, Pageable request);

    List<CandidateAttachment> findByCandidateId(Long candidateId);

    List<CandidateAttachment> findByCandidateIdAndCv(Long candidateId, boolean cv);

    @Query(" select distinct a from CandidateAttachment a "
            + " left join a.candidate c "
            + " where a.id = :id ")
    Optional<CandidateAttachment> findByIdLoadCandidate(@Param("id") Long id);

    List<CandidateAttachment> findByFileType(String fileType);

    @Query(" select distinct a from CandidateAttachment a "
            + "where a.fileType in (:files) "
            + "and a.migrated = :migrated")
    List<CandidateAttachment> findByFileTypesAndMigrated(@Param("files") List<String> files, @Param("migrated") Boolean migrated);

}
