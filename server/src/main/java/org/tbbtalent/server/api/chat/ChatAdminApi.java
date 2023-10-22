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

package org.tbbtalent.server.api.chat;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.api.admin.ITableApi;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.model.db.JobChat;
import org.tbbtalent.server.request.chat.SearchChatRequest;
import org.tbbtalent.server.request.chat.UpdateChatRequest;
import org.tbbtalent.server.service.db.JobChatService;
import org.tbbtalent.server.util.dto.DtoBuilder;

/**
 * This is the api where new chats can be created and updated.
 *
 * @author John Cameron
 */
@RestController()
@RequestMapping("/api/admin/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatAdminApi implements
    ITableApi<SearchChatRequest, UpdateChatRequest, UpdateChatRequest> {

    private final JobChatService chatService;

    @Override
    @PostMapping
    public @NonNull Map<String, Object> create(UpdateChatRequest request) throws EntityExistsException {
        JobChat chat = chatService.createJobChat(request);
        return chatDto().build(chat);
    }

    @Override
    @NonNull
    public List<Map<String, Object>> list() {
        List<JobChat> chats = chatService.listJobChats();
        return chatDto().buildList(chats);
    }

    private DtoBuilder chatDto() {
        return new DtoBuilder()
            .add("id")
        ;
    }
}
