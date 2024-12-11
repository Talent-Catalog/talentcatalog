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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 * Primary key for {@link JobChatUser}.
 * See doc for that class.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Embeddable
public class JobChatUserKey implements Serializable {

    @Column(name = "job_chat_id")
    private Long jobChatId;

    @Column(name = "user_id")
    private Long userId;

    public JobChatUserKey() {
    }
}
