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

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.CandidateOpportunity;

public interface CandidateOpportunityRepository extends JpaRepository<CandidateOpportunity, Long>,
    JpaSpecificationExecutor<CandidateOpportunity> {


    @Query(value = """
        select chats.id from

        (select job_chat.id from candidate_opportunity
            join candidate on candidate_opportunity.candidate_id = candidate.id
            join job_chat on candidate.id = job_chat.candidate_id
                and type = 'CandidateProspect'
            where candidate_opportunity.id in (:oppIds)
        union
        select job_chat.id from candidate_opportunity
            join candidate on candidate_opportunity.candidate_id = candidate.id
            join job_chat on candidate.id = job_chat.candidate_id
                                 and job_id = candidate_opportunity.job_opp_id
                and type = 'CandidateRecruiting'
            where candidate_opportunity.id in (:oppIds)
        ) as chats
        
        where
                (select last_read_post_id from job_chat_user where job_chat_id = chats.id and user_id = :userId)
                    < (select max(id) from chat_post where job_chat_id = chats.id)

        or  (
                (select last_read_post_id from job_chat_user where job_chat_id = chats.id and user_id = :userId) is null
                and
                (select count(*) from chat_post where job_chat_id = chats.id) > 0
            )
        """, nativeQuery = true)
    List<Long> findUnreadChatsInOpps(@Param("userId") long userId, @Param("oppIds") Iterable<Long> oppIds);

    @Query(" select op from CandidateOpportunity op "
        + " where op.sfId = :sfId ")
    Optional<CandidateOpportunity> findBySfId(@Param("sfId") String sfId);

    List<CandidateOpportunity> findAllBySfIdIsNull();

    @Query(" select op from CandidateOpportunity op "
        + " where op.candidate.id = :candidateId and op.jobOpp.id = :jobOppId")
    CandidateOpportunity findByCandidateIdAndJobId(
        @Param("candidateId") Long candidateId, @Param("jobOppId") Long jobOppId);

    @Query(" select op from CandidateOpportunity op where op.jobOpp.createdBy.partner.id = :partnerId")
    List<CandidateOpportunity> findPartnerOpps(@Param("partnerId") Long partnerId);

    @Query(
        "SELECT co.sfId " +
        "FROM CandidateOpportunity co " +
        "WHERE co.closed = false " +
        "AND co.sfId IS NOT NULL"
    )
    List<String> findAllNonNullSfIdsByClosedFalse();
}
