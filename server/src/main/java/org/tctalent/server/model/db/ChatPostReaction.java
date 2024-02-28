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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_post_reaction")
@SequenceGenerator(name = "seq_gen", sequenceName = "chat_post_reaction_id_seq", allocationSize = 1)
public class ChatPostReaction extends AbstractDomainObject<Long> {

    /**
     * Associated chat post
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_post_id")
    private ChatPost chatPost;

    private String emoji;

    /**
     * String of the ids of users who submitted this chat post reaction.
     * This is a simple string of ids to avoid a lengthy and unnecessary many-to-many relationship,
     * as we don't need to track the inverse relationship of users and their post reactions.
     * It is a string as opposed to a List of ids due to the error:
     * ''Basic' attribute type should not be a container'
     */
    private String userIds;

    /**
     * Get the string of user ids and convert to a comma separated list of ids(long).
     * @return List of user ids
     */
    public List<Long> getUserIdsList() {
        List<Long> userIdsList;
        userIdsList = Stream.of(userIds.split(","))
                                    .map(Long::parseLong)
                                    .collect(Collectors.toList());
        return userIdsList;
    }

    /**
     * Set list of user ids (long) to a string of ids comma separated to save to database.
     */
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));
    }
}
