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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatUserInfo;
import org.tctalent.server.model.db.User;

/**
 * Service for keeping track of where each user has read up to in each post.
 */
public interface JobChatUserService {

    /**
     * Returns information about where the given user has read up to in the given chat.
     * @param chat Chat in question
     * @param user User in question
     * @return Information relating to where the user has read up to in the chat
     */
    @NonNull
    JobChatUserInfo getJobChatUserInfo(@NonNull JobChat chat, @NonNull User user);

    /**
     * Determines whether the given chat has been marked as read by the given user.
     * @param chat Chat in question
     * @param user User in question
     * @return True if chat is marker as read by the user, otherwise false.
     */
    boolean isChatReadByUser(@NonNull JobChat chat, @NonNull User user);

    /**
     * Records which post the given user has read up to in the given chat
     * @param chat Chat in question
     * @param user User in question
     * @param post This is the post that the user has read up to in the chat. Can be null if there
     *             are no posts in the chat.
     */
    void markChatAsRead(@NonNull JobChat chat, @NonNull User user, @Nullable ChatPost post);
}
