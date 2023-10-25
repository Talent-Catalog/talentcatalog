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

package org.tctalent.server.service.db.impl;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.repository.db.JobChatRepository;
import org.tctalent.server.request.chat.UpdateChatRequest;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.UserService;

@Service
@RequiredArgsConstructor
public class JobChatServiceImpl implements JobChatService {

    private final UserService userService;
    private final JobChatRepository jobChatRepository;

    @Override
    @NonNull
    public JobChat createJobChat(UpdateChatRequest request) throws EntityExistsException {
        JobChat chat = new JobChat();
        chat.setCreatedBy(userService.getLoggedInUser());
        chat.setCreatedDate(OffsetDateTime.now());

        return jobChatRepository.save(chat);
    }

    @Override
    @NonNull
    public JobChat getJobChat(long id) throws NoSuchObjectException {
        return jobChatRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(JobChat.class, id));
    }

    @Override
    @NonNull
    public List<JobChat> listJobChats() {
        final List<JobChat> chats = jobChatRepository.findAll(Sort.by(Direction.ASC, "id"));
        return chats;
    }
}
