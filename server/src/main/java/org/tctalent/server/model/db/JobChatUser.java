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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

/**
 * Used to keep track of which post in a chat each user has read up to.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "job_chat_user")
public class JobChatUser {

    @EqualsAndHashCode.Include
    @EmbeddedId
    JobChatUserKey id;

    /**
     * Chat whose user read info we are interested in
     */
    @NonNull
    @ManyToOne
    @MapsId("jobChatId")
    @JoinColumn(name = "job_chat_id")
    JobChat chat;

    /**
     * User who has read up to a certain point in the above chat
     */
    @NonNull
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    User user;

    /**
     * This is the last post in the above chat that the user has read.
     * <p/>
     * May be null in which case we don't know
     */
    @Nullable
    @ManyToOne
    @JoinColumn(name = "last_read_post_id")
    ChatPost lastReadPost;

}
