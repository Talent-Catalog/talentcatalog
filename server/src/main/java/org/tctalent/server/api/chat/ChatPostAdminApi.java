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

package org.tctalent.server.api.chat;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.api.admin.IJoinedTableApi;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.request.chat.SearchChatRequest;
import org.tctalent.server.request.chat.UpdateChatRequest;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@RestController()
@RequestMapping("/api/admin/chat-post")
@Slf4j
@RequiredArgsConstructor
public class ChatPostAdminApi implements
    IJoinedTableApi<SearchChatRequest, UpdateChatRequest, UpdateChatRequest> {

    private final ChatPostService chatPostService;

    @Override
    @GetMapping("{id}/list")
    public List<Map<String, Object>> list(long chatId) {
        List<ChatPost> posts = chatPostService.listChatPosts(chatId);
        return postDto().buildList(posts);
    }

    private DtoBuilder postDto() {
        return new DtoBuilder()
            .add("id")
            .add("content")
            .add("createdDate")
            .add("createdBy", userDto())
            ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
            .add("id")
            .add("firstName")
            .add("lastName")
            ;
    }

}
