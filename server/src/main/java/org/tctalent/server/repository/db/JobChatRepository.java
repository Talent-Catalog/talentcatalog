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

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;

public interface JobChatRepository extends JpaRepository<JobChat, Long>,
    JpaSpecificationExecutor<JobChat> {
    @Query("SELECT c FROM JobChat c WHERE c.jobOpp.id = :jobId")
    List<JobChat> findByJobOppId(@Param("jobId") Long jobId);

    @Query(" select c from JobChat c "
        + " where c.id in (:ids) ")
    List<JobChat> findByIds(@Param("ids") Iterable<Long> ids);

    @Query("select c from JobChat c where c.type = :type "
        + "and c.jobOpp is not null and c.jobOpp.id = :jobId")
    JobChat findByTypeAndJob(@Param("type") JobChatType type, @Param("jobId") Long jobId);

    @Query("select c from JobChat c where c.type = :type "
        + "and c.candidate is not null and c.candidate.id = :candidateId")
    JobChat findByTypeAndCandidate(
        @Param("type") JobChatType type,
        @Param("candidateId") Long candidateId);

    @Query("select c from JobChat c where c.type = :type "
        + "and c.candidate is not null and c.candidate.id = :candidateId "
        + "and c.jobOpp is not null and c.jobOpp.id = :jobId")
    JobChat findByTypeAndCandidateAndJob(
        @Param("type") JobChatType type,
        @Param("candidateId") Long candidateId,
        @Param("jobId") Long jobId);

    @Query("select c from JobChat c where c.type = :type "
        + "and c.jobOpp is not null and c.jobOpp.id = :jobId "
        + "and c.sourcePartner is not null and c.sourcePartner.id = :partnerId")
    JobChat findByTypeAndJobAndPartner(
        @Param("type") JobChatType type, @Param("jobId") Long jobId,
        @Param("partnerId") Long partnerId);

    /**
     * Find chats which have posts where the date of the last post is greater than a given date.
     * <p/>
     * Note: I had to use a native query returning chat ids instead of a non-native query returning
     * JobChats because the Spring Framework JPA Data code crashed with a Null pointer exception
     * trying to validate the query - even though the query was valid.
     * As a workaround, this query returns ids which can be passed to {@link #findByIds(Iterable)}
     * to retrieve the JobChat objects - JC
     *
     * @param dateTime We want chats with posts after this date
     * @return Chats since the given date
     */
    @Query(value = """
        select id from job_chat c where
        (select created_date from chat_post
        where chat_post.id = (select max(chat_post.id) from chat_post where chat_post.job_chat_id = c.id)) > :date
        """, nativeQuery = true)
    List<Long> findChatsWithPostsSinceDate(@Param("date") OffsetDateTime dateTime);
}
