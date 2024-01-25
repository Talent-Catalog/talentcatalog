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

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.ChatPost;

public interface ChatPostRepository extends JpaRepository<ChatPost, Long>,
    JpaSpecificationExecutor<ChatPost> {

    Optional<List<ChatPost>> findByJobChatId(Long chatId);

    @Query(
        value="SELECT id FROM chat_post p WHERE p.job_chat_id = :chatId ORDER BY p.id DESC LIMIT 1",
        nativeQuery = true
        //Need native query because JPQL does not support LIMIT with the version we are running
        //This post implies it does https://www.baeldung.com/spring-data-jpa-last-record in
        // Spring Data JPA version 3.2.
    )
    Long findLastChatPost(@Param("chatId") Long chatId);
}
