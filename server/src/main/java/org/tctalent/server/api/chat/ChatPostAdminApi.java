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

package org.tctalent.server.api.chat;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.api.admin.IJoinedTableApi;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.UrlDto;
import org.tctalent.server.request.chat.SearchChatPostRequest;
import org.tctalent.server.request.chat.UpdateChatPostRequest;
import org.tctalent.server.service.db.ChatPostService;

@RestController()
@RequestMapping("/api/admin/chat-post")
@Slf4j
@RequiredArgsConstructor
public class ChatPostAdminApi implements
    IJoinedTableApi<SearchChatPostRequest, UpdateChatPostRequest, UpdateChatPostRequest> {

    private final ChatPostService chatPostService;

    @Override
    @GetMapping("{id}/list")
    public List<Map<String, Object>> list(long chatId) {
        List<ChatPost> posts = chatPostService.listChatPosts(chatId);
        return chatPostService.getChatPostDtoBuilder().buildList(posts);
    }

    /**
     * Note that id is id of post, not chat
     */
    @PostMapping("{id}/upload")
    public UrlDto upload(
        @PathVariable("id") long id, @RequestParam("file") MultipartFile file)
        throws InvalidRequestException, IOException, NoSuchObjectException {
        String fileUrl = chatPostService.uploadFile(id, file);
        UrlDto urlDto = new UrlDto(fileUrl);
        return urlDto;
    }
}
