/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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


    @Query("select chats.id from\n"
        + "\n"
        + "(select job_chat.id,job_chat.type from candidate_opportunity\n"
        + "    join candidate on candidate_opportunity.candidate_id = candidate.id\n"
        + "    join job_chat on candidate.id = job_chat.candidate_id and type = 'CandidateProspect'\n"
        + "where candidate_opportunity.id in (:oppIds)\n"
        + "union "
        + "select job_chat.id,job_chat.type from candidate_opportunity\n"
        + "    join candidate on candidate_opportunity.candidate_id = candidate.id\n"
        + "    join job_chat on candidate.id = job_chat.candidate_id\n"
        + "                         and job_id = candidate_opportunity.job_opp_id\n"
        + "                         and type = 'CandidateRecruiting'\n"
        + "where candidate_opportunity.id in (:oppIds)) as chats\n"
        + "where\n"
        + "        (select last_read_post_id from job_chat_user where job_chat_id = chats.id and user_id = :userId)\n"
        + "            < (select max(id) from chat_post where job_chat_id = chats.id)\n"
        + "\n"
        + "or  (\n"
        + "        (select last_read_post_id from job_chat_user where job_chat_id = chats.id and user_id = :userId) is null\n"
        + "        and\n"
        + "        (select count(*) from chat_post where job_chat_id = chats.id) > 0\n"
        + "    )\n")
    List<Long> findUnreadChatsInOpps(@Param("userId") long userId, @Param("oppIds") Iterable<Long> oppIds);

    @Query(" select op from CandidateOpportunity op "
        + " where op.sfId = :sfId ")
    Optional<CandidateOpportunity> findBySfId(@Param("sfId") String sfId);

    @Query(" select op from CandidateOpportunity op "
        + " where op.candidate.id = :candidateId and op.jobOpp.id = :jobOppId")
    CandidateOpportunity findByCandidateIdAndJobId(
        @Param("candidateId") Long candidateId, @Param("jobOppId") Long jobOppId);
}
