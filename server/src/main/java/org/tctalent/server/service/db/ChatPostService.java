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

import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.chat.Post;
import org.tctalent.server.util.dto.DtoBuilder;

public interface ChatPostService {

    ChatPost createPost(@NonNull Post post, @NonNull JobChat jobChat);

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

    List<ChatPost> listChatPosts(long chatId);

    /**
     * Upload a file to a chat post which is stored in the job's Google Drive folder, in a ChatUploads subfolder.
     * This file upload is to be viewable by anyone with the link so that it can be displayed on the TC as part of the chat post.
     * @param id Id of the chat post
     * @param file The file to upload to the Google Drive
     * @return String URL of the file location on the Google Drive.
     * The URL isn't stored on the ChatPost object in the database, but instead it will be stored as part of an <img> tag in the ChatPost content.
     * @throws InvalidRequestException
     * @throws NoSuchObjectException
     * @throws IOException
     */
    String uploadFile(long id, MultipartFile file)
        throws InvalidRequestException, NoSuchObjectException, IOException;
}
