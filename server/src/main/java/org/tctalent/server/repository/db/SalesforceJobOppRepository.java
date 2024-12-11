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
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;

public interface SalesforceJobOppRepository extends JpaRepository<SalesforceJobOpp, Long>,
    JpaSpecificationExecutor<SalesforceJobOpp> {

    @Query("select distinct j from SalesforceJobOpp j left join j.submissionList list "
        + " where list = :jobList")
    SalesforceJobOpp getJobBySubmissionList(@Param("jobList") SavedList jobList);

    @Query(" select j from SalesforceJobOpp j "
        + " where j.sfId = :sfId ")
    Optional<SalesforceJobOpp> findBySfId(@Param("sfId") String sfId);

    @Query(value = """
        select chats.id from

        (select job_chat.id from salesforce_job_opp
            join job_chat on salesforce_job_opp.id = job_id
                and type in ('JobCreatorAllSourcePartners','AllJobCandidates','JobCreatorSourcePartner')
            where salesforce_job_opp.id in (:oppIds)
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
}
