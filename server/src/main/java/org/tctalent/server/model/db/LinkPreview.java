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

package org.tctalent.server.model.db;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * Displays a preview of the content navigable to via link-formatted text in the Chat Post editor
 * or sent Chat Post. A good overview of what constitutes a typical link preview and the methodology
 * used to put ours together can be found in
 * <a href="https://andrejgajdos.com/how-to-create-a-link-preview/">this blog post</a> by Andrej Gaydos.
 * All the desired elements aren't necessarily unearthed by our scraping methods, or present in the
 * first place. The minimal acceptable elements for display on the TC are a valid URL, which we
 * parse to also provide a domain.
 */
@Getter
@Setter
@Entity
@Table(name = "link_preview")
@SequenceGenerator(name = "seq_gen", sequenceName = "link_preview_id_seq", allocationSize = 1)
public class LinkPreview extends AbstractDomainObject<Long> {

    /**
     * Associated chat post.
     * FetchType.LAZY specified because otherwise we fall back to EAGER fetching which is
     * <a href="https://vladmihalcea.com/eager-fetching-is-a-code-smell/">bad for performance.</a>.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_post_id")
    private ChatPost chatPost;

    /**
     * The linked-to URL
     */
    @NotBlank
    private String url;

    /**
     * The domain component of the URL
     */
    @NotBlank
    private String domain;

    /**
     * The linked-to content's best match for a title
     */
    @Nullable
    private String title;

    /**
     * The linked-to content's best match for a description
     */
    @Nullable
    private String description;

    /**
     * The best-suited image from the linked-to content
     */
    @Nullable
    private String imageUrl;

    /**
     * The small icon that often precedes the page title on a browser tab
     */
    @Nullable
    private String faviconUrl;

}
