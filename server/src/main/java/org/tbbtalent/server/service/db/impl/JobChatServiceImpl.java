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

package org.tbbtalent.server.service.db.impl;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.ChatPost;
import org.tbbtalent.server.model.db.JobChat;
import org.tbbtalent.server.model.db.chat.Post;
import org.tbbtalent.server.service.db.JobChatService;
import org.tbbtalent.server.service.db.UserService;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Service
@RequiredArgsConstructor
public class JobChatServiceImpl implements JobChatService {

    private final UserService userService;

    @Override
    public ChatPost createPost(Post post, String chatId) {
        Long id = Long.parseLong(chatId);
        JobChat jobChat = getJobChat(id);
        ChatPost chatPost = new ChatPost();
        chatPost.setJobChat(jobChat);
        chatPost.setContent(post.getContent());
        chatPost.setCreatedDate(OffsetDateTime.now());
        chatPost.setCreatedBy(userService.getLoggedInUser());

        //TODO JC This should be stored on the DB
        chatPost.setId(1L);
        return chatPost;
    }

    private JobChat getJobChat(Long chatId) {
        JobChat jobChat = new JobChat();
        jobChat.setId(chatId);
        //TODO JC Implement getJobChat by looking up DB
        return jobChat;
    }
}
