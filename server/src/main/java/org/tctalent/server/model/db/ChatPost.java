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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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

    //Author of post is stored in inherited createdBy
    //Timestamp of post is stored in inherited createdDate
}
