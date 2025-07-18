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

package org.tctalent.server.service.db;

import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.chat.Post;
import org.tctalent.server.util.dto.DtoBuilder;

public interface ChatPostService {

    /**
     * This is the destination prefix for all our websocket topics
     */
    String TOPIC_PREFIX = "/topic";

    /**
     * This is root of all chat websocket destinations
     */
    String CHAT_PUBLISH_ROOT = TOPIC_PREFIX + "/chat";

    /**
     * Creates a new post on the given chat
     * @param post Content of post
     * @param jobChat Chat where posted
     * @param user the user associated with the post - normally the person who made the post
     * @return The chat post.
     */
    ChatPost createPost(@NonNull Post post, @NonNull JobChat jobChat, User user);

    /**
     * Get the ChatPost with the given id.
     * @param id Id of post to get
     * @return ChatPost
     * @throws NoSuchObjectException if there is no post with this id.
     */
    @NonNull
    ChatPost getChatPost(long id) throws NoSuchObjectException;

    /**
     * Return a DtoBuilder for ChatPosts
     * @return DtoBuilder that builds DTO objects
     */
    DtoBuilder getChatPostDtoBuilder();

    /**
     * Returns the last post in the given chat.
     * @param chatId ID of chat
     * @return Last post, null if there are no posts.
     */
    @Nullable
    ChatPost getLastChatPost(long chatId);

    /**
     * Returns all posts associated with the given chat.
     * @param chatId Chat whose posts we want
     * @return posts (can be empty)
     * @throws NoSuchObjectException if there is no chat with that id.
     */
    @NonNull
    List<ChatPost> listChatPosts(long chatId) throws NoSuchObjectException;

    /**
     * Sends the given post out on a websocket with the chat's topic as a destination.
     * So all subscribers to that chat will receive the post.
     * <p/>
     * Note that this has the same effect as @SendTo in
     * {@link org.tctalent.server.api.chat.ChatPublishApi}.
     * <p/>
     * This method is used for auto generating and publishing posts from the server.
     * By contrast ChatPublishApi publishes posts that have been manually entered on the chat
     * by a user on their browser.
     *
     * @param post Post to be published
     */
    void publishChatPost(ChatPost post);
}
