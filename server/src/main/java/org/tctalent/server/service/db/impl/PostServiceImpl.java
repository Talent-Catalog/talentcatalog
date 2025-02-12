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

package org.tctalent.server.service.db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.LinkPreview;
import org.tctalent.server.model.db.chat.Post;
import org.tctalent.server.service.db.LinkPreviewService;
import org.tctalent.server.service.db.PostService;

/**
 * Post service which will try to create {@link LinkPreview}s from any urls found in the post content
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {

    private final LinkPreviewService linkPreviewService;

    @NonNull
    @Override
    public Post createPost(String content) {
        Post post = new Post();
        post.setContent(content);
        extractLinkPreviews(post, content);
        return post;
    }

    private void extractLinkPreviews(@NonNull Post post, @Nullable String content) {
        if (content != null) {
            //Replace any existing link previews
            post.getLinkPreviews().clear();

            //TODO JC Scan content looking for links and urls.
            //TODO JC Loop through all urls found in content. For each...
//            String url = "???"; //TODO JC Debug
//            LinkPreview linkPreview = linkPreviewService.buildLinkPreview(url);
//            if (linkPreview != null) {
//                post.getLinkPreviews().add(linkPreview);
//            }
        }
    }
}
