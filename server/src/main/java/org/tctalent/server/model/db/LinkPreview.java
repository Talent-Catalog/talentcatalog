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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
@Entity
@Table(name = "link_preview")
@SequenceGenerator(name = "seq_gen", sequenceName = "link_preview_id_seq", allocationSize = 1)
public class LinkPreview extends AbstractDomainObject<Long> {

    /**
     * Associated chat post
     */
    @ManyToOne
    @JoinColumn(name = "chat_post_id")
    private ChatPost chatPost;

    //TODO consider notblank / nullable here
    private String url;
    private String title;
    private String description;
    private String imageUrl;
    private String domain;
    private String faviconUrl;
}
