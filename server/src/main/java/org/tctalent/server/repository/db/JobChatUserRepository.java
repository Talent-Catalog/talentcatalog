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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.model.db.JobChatUser;
import org.tctalent.server.model.db.JobChatUserKey;

/**
 * See doc for {@link org.tctalent.server.model.db.JobChatUser}
 */
public interface JobChatUserRepository extends JpaRepository<JobChatUser, JobChatUserKey> {
  /**
   * Deletes {@link JobChatUser} entries associated with the specified job chat ID.
   * @param jobChatId The ID of the job chat for which associated {@link JobChatUser} entries will be deleted.
   */
  @Transactional
  @Modifying
  @Query("DELETE FROM JobChatUser cp WHERE cp.chat.id = :jobChatId")
  void deleteByJobChatId(@Param("jobChatId") Long jobChatId);
}
