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

package org.tctalent.server.service.db.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.LinkPreview;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.chat.Post;
import org.tctalent.server.repository.db.ChatPostRepository;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.util.dto.DtoBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatPostServiceImpl implements ChatPostService {

    private final ChatPostRepository chatPostRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public ChatPost createPost(@NonNull Post post, @NonNull JobChat jobChat, User user) {
        ChatPost chatPost = new ChatPost();
        chatPost.setJobChat(jobChat);

        //Check for nulls in content
        String content = post.getContent();
        if (content.indexOf('\u0000') >= 0) {
            //Replace nulls with '?'.
            content = content.replaceAll("\u0000", "?");
            LogBuilder.builder(log)
                .action("createPost")
                .message("Post for chat " + jobChat.getId() + " had nulls: '" + content + "'")
                .logError();
        }
        chatPost.setContent(content);

        chatPost.setCreatedDate(OffsetDateTime.now());
        chatPost.setCreatedBy(user);
        if (post.getLinkPreviews() != null) {
            for (LinkPreview linkPreview : post.getLinkPreviews()) {
                chatPost.addLinkPreview(linkPreview);
            }
        }

        //Log chatPost - for debugging purposes
        LogBuilder.builder(log)
            .action("createPost")
            .message("Chat " + jobChat.getId() + ": " + chatPost.getContent())
            .logInfo();

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
    public DtoBuilder getChatPostDtoBuilder() {
        return new DtoBuilder()
            .add("id")
            .add("content")
            .add("createdDate")
            .add("createdBy", userDto())
            .add("jobChat", jobChatDto())
            .add("updatedDate")
            .add("updatedBy", userDto())
            .add("reactions", reactionDto())
            .add("linkPreviews", linkPreviewDto())
            ;
    }

    private DtoBuilder linkPreviewDto() {
        return new DtoBuilder()
            .add("id")
            .add("url")
            .add("title")
            .add("description")
            .add("imageUrl")
            .add("domain")
            .add("faviconUrl")
            ;
    }

    private DtoBuilder jobChatDto() {
        return new DtoBuilder()
            .add("id")
            ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
            .add("id")
            .add("firstName")
            .add("lastName")
            .add("partner", partnerDto())
            .add("role")
            ;
    }

    private DtoBuilder partnerDto() {
        return new DtoBuilder()
            .add("id")
            .add("abbreviation")
            ;
    }

    private DtoBuilder reactionDto() {
        return new DtoBuilder()
            .add("id")
            .add("emoji")
            .add("users", reactionUserDto())
            ;
    }

    private DtoBuilder reactionUserDto() {
        return new DtoBuilder()
            .add("id")
            .add("displayName")
            ;
    }

    @Nullable
    @Override
    public ChatPost getLastChatPost(long chatId) {
        Long postId = chatPostRepository.findLastChatPost(chatId);
        ChatPost post = postId == null ? null : getChatPost(postId);
        return post;
    }

    @NonNull
    public List<ChatPost> listChatPosts(long chatId) {
        return chatPostRepository.findByJobChatIdOrderByIdAsc(chatId)
            .orElseThrow(() -> new NoSuchObjectException(JobChat.class, chatId));
    }

    @Override
    public void publishChatPost(ChatPost post) {
        final Map<String, Object> postDto = getChatPostDtoBuilder().build(post);
        messagingTemplate.convertAndSend(
            CHAT_PUBLISH_ROOT + "/" + post.getJobChat().getId(), postDto);
    }
}
