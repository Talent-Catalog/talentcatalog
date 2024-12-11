/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.repository.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.task.UploadType;

import java.util.List;
import java.util.Optional;

public interface CandidateAttachmentRepository extends JpaRepository<CandidateAttachment, Long> {

    @Query(" select distinct a from CandidateAttachment a "
            + " left join a.createdBy u "
            + " left join a.candidate c "
            + " where c.id = :candidateId "
            + " order by a.createdDate desc")
    List<CandidateAttachment> findByCandidateIdLoadAudit(@Param("candidateId") Long candidateId);

    @Query(" select distinct a from CandidateAttachment a "
            + " left join a.candidate c "
            + " where c.id = :candidateId "
            + " and a.uploadType = :uploadType ")
    List<CandidateAttachment> findByCandidateIdAndType(@Param("candidateId") Long candidateId,
                                                         @Param("uploadType") UploadType uploadType);

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
