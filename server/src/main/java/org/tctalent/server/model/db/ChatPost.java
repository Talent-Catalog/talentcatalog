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

package org.tctalent.server.model.db;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a post in a JobChat.
 *
 * @author John Cameron
 */
@Getter
@Setter
@Entity
@Table(name = "chat_post")
@SequenceGenerator(name = "seq_gen", sequenceName = "chat_post_id_seq", allocationSize = 1)
public class ChatPost extends AbstractAuditableDomainObject<Long> {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_chat_id")
    private JobChat jobChat;
    private String content;

    @OneToMany(mappedBy = "chatPost", cascade = CascadeType.ALL)
    private List<Reaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "chatPost", cascade = CascadeType.ALL)
    private List<LinkPreview> linkPreviews = new ArrayList<>();

    public void setLinkPreviews(List<LinkPreview> linkPreviews) {
        //TODO JC This still has problems because it doesn't clear out existing linkPreviews
        if (linkPreviews != null) {
            linkPreviews.forEach(linkPreview -> {
                linkPreview.setChatPost(this);
                this.linkPreviews.add(linkPreview);
            });
        }
    }

    //Author of post is stored in inherited createdBy
    //Timestamp of post is stored in inherited createdDate
}
