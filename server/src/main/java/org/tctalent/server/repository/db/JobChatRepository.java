/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;

public interface JobChatRepository extends JpaRepository<JobChat, Long>,
    JpaSpecificationExecutor<JobChat> {

    @Query("select c from JobChat c where c.type = :type "
        + "and c.jobOpp is not null and c.jobOpp.id = :jobId")
    JobChat findByTypeAndJob(@Param("type") JobChatType type, @Param("jobId") Long jobId);

    @Query("select c from JobChat c where c.type = :type "
        + "and c.candidateOpp is not null and c.candidateOpp.id = :oppId")
    JobChat findByTypeAndCandidateOpp(@Param("type") JobChatType type, @Param("oppId") Long oppId);

    @Query("select c from JobChat c where c.type = :type "
        + "and c.jobOpp is not null and c.jobOpp.id = :jobId "
        + "and c.sourcePartner is not null and c.sourcePartner.id = :partnerId")
    JobChat findByTypeAndJobAndPartner(
        @Param("type") JobChatType type, @Param("jobId") Long jobId,
        @Param("partnerId") Long partnerId);
}
