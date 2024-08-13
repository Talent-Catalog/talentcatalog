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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
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

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Associated chat post.
     * FetchType.LAZY specified because otherwise we fall back to EAGER fetching which is
     * <a href="https://vladmihalcea.com/eager-fetching-is-a-code-smell/">bad for performance.</a>.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_post_id", nullable=false)
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

    /**
     * Since we cannot rely on a natural identifier for equality checks, we need to use the entity
     * identifier instead for the equals method. See
     * <a href="https://vladmihalcea.com/the-best-way-to-map-a-onetomany-association-with-jpa-and-hibernate/#:~:text=OneToMany%20Set%20association.-,Bidirectional%20%40OneToMany,-The%20best%20way">
     *   this article</a> for best practices in defining bi-directional one-to-many relationships, and
     *   <a href="https://vladmihalcea.com/hibernate-facts-equals-and-hashcode/">this article</a> for
     *   implementing equals and hashCode specifically.
     * @param o Object to be checked for equality
     * @return boolean denoting entity equality
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkPreview )) return false;
        return id != null && id.equals(((LinkPreview) o).getId());
    }

    /**
     * We can’t use an auto-incrementing database id in the hashCode method since the transient and
     * the attached object versions will no longer be located in the same hashed bucket. The
     * possibility arises that we will instantiate two different objects that represent the same
     * row in the database. See
     * <a href="https://vladmihalcea.com/the-best-way-to-map-a-onetomany-association-with-jpa-and-hibernate/#:~:text=OneToMany%20Set%20association.-,Bidirectional%20%40OneToMany,-The%20best%20way">
     *   this article</a> for best practices in defining bidirectional one-to-many relationships,
     *   <a href="https://vladmihalcea.com/hibernate-facts-equals-and-hashcode/">this article</a> for
     *   implementing equals and hashCode specificall discussion of the reasons this is required
     *   <a href="http://www.onjava.com/pub/a/onjava/2006/09/13/dont-let-hibernate-steal-your-identity.html">
     *     here.</a>
     * @return integer representing the hash value of the LinkPreview class
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}