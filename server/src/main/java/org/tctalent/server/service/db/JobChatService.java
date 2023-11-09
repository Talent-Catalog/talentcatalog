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

package org.tctalent.server.service.db;

import java.util.List;
import org.springframework.lang.NonNull;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.request.chat.UpdateChatRequest;

public interface JobChatService {

    /**
     * Creates a new job chat
     * @param request Request defining the chat to be created
     * @return Created job chat
     * @throws EntityExistsException if there is already a job chat matching the given request.
     */
    @NonNull JobChat createJobChat(UpdateChatRequest request) throws EntityExistsException;

    /**
     * Get the JobChat with the given id.
     * @param id Id of job chat to get
     * @return JobChat
     * @throws NoSuchObjectException if there is no JobChat with this id.
     */
    @NonNull
    JobChat getJobChat(long id) throws NoSuchObjectException;

    /**
     * Return all JobChats.
     * @return JobChats
     */
    @NonNull
    List<JobChat> listJobChats();
}
