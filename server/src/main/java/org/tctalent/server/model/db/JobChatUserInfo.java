/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.model.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * This is the data returned to the browser regard where a user has read up to on a chat and
 * where the chat ends.
 *
 * @author John Cameron
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobChatUserInfo {

    /**
     * Last id post read by user.
     * Null if chat has not been read, or we don't know.
     */
    @Nullable
    Long lastReadPostId;

    /**
     * ID of last post in chat.
     * Null if we don't know, or there are no posts in the chat.
     */
    @Nullable
    Long lastPostId;
}
