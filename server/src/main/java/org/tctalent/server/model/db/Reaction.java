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

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * At the end of a displayed sent post is a button allowing users to 'react' to its content by
 * selecting an emoji which is then appended to the post with their name attributed. Each user
 * selection is a unique reaction, but reactions using the same emoji are collated into one emoji
 * badge display, with a hover-over list of associated users and a count thereof. Works in much the
 * same way as Slack, Facebook et al. at time of writing (Aug '24).
 */
@Getter
@Setter
@Entity
@Table(name = "reaction")
@SequenceGenerator(name = "seq_gen", sequenceName = "reaction_id_seq", allocationSize = 1)
public class Reaction extends AbstractDomainObject<Long> {

    /**
     * Associated user(s)
     * <p>
     * Eagerly fetched because, in a WebSocket context, the Hibernate session may be closed before
     * the users collection is accessed for dto construction.
     */
    @ManyToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
        name = "reaction_user",
        joinColumns = @JoinColumn(name = "reaction_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;

    /**
     * Associated chat post
     */
    @ManyToOne
    @JoinColumn(name = "chat_post_id")
    private ChatPost chatPost;

    /**
     * The user-selected emoji
     */
    private String emoji;
}
