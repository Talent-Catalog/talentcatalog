/*
 * Copyright (c) 2025 Talent Catalog.
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
import org.tctalent.server.model.db.chat.Post;

/**
 * Service for creating {@link Post}'s
 * <p/>
 * Posts should normally be created through this service which automatically scans for any urls
 * in the content and attempts to generate any LinkPreviews if possible.
 *
 * @author John Cameron
 */
public interface PostService {

    /**
     * Creates post with given content and optionally generated LinkPreviews
     * @param content Content of post
     * @param suppressLinkPreviews If true, LinkPreviews are not automatically generated
     * @return Post
     */
    @NonNull
    Post createPost(String content, boolean suppressLinkPreviews);

    /**
     * Creates post with given content
     * <p/>
     * Automatically generates LinkPreviews from content if any urls are found in the content.
     * <p/>
     * Equivalent to calling createPost(content, false).
     * @param content Content of post
     * @return Post
     */
    @NonNull
    Post createPost(String content);
}
