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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.ChatPost;
import org.tbbtalent.server.model.db.JobChat;
import org.tbbtalent.server.model.db.chat.Post;
import org.tbbtalent.server.repository.db.ChatPostRepository;
import org.tbbtalent.server.service.db.ChatPostService;
import org.tbbtalent.server.service.db.UserService;

@Service
@RequiredArgsConstructor
public class ChatPostServiceImpl implements ChatPostService {

    private final UserService userService;
    private final ChatPostRepository chatPostRepository;

    @Override
    public ChatPost createPost(@NonNull Post post, @NonNull JobChat jobChat) {
        ChatPost chatPost = new ChatPost();
        chatPost.setJobChat(jobChat);
        chatPost.setContent(post.getContent());
        chatPost.setCreatedDate(OffsetDateTime.now());
        chatPost.setCreatedBy(userService.getLoggedInUser());

        chatPost = chatPostRepository.save(chatPost);
        return chatPost;
    }

    @Override
    @NonNull
    public ChatPost getChatPost(long id) throws NoSuchObjectException {
       return chatPostRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(ChatPost.class, id));
    }

    @Override
    public List<ChatPost> listChatPosts(long chatId) {
        return chatPostRepository.findByJobChatId(chatId)
            .orElseThrow(() -> new NoSuchObjectException(JobChat.class, chatId));
    }
}
