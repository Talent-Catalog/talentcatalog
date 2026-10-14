/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.springframework.lang.Nullable;

/**
 * A matching context: the natural language description that candidates are matched against
 * (for example, refined job requirements).
 * <p>
 * A Matching is independent of any Job. Candidate sources deliberately participating in the same
 * matching activity share a Matching. Its description is mutable and may be blank - for example,
 * when the activity has been started but no requirements have been entered yet.
 * <p>
 * {@link #updatedDate} records when the description last actually changed, so that anything
 * derived from the description (eg a match explanation) can snapshot it and later determine
 * whether it was derived from the current description. For that reason, it is not a general
 * audit field: it is only changed by {@link #updateMatchingDescription}, and only when the
 * description actually changes.
 */
@Getter
@Entity
@Table(name = "matching")
@SequenceGenerator(name = "seq_gen", sequenceName = "matching_id_seq", allocationSize = 1)
public class Matching extends AbstractDomainObject<Long> {

    /**
     * Natural language matching description - may be HTML from a rich text editor. Null if there
     * is no description: a description without any text content (see {@link #hasTextContent})
     * is always stored as null.
     */
    @Nullable
    @Column(name = "matching_description")
    private String matchingDescription;

    /**
     * When {@link #matchingDescription} last changed.
     * <p>
     * Always held in UTC truncated to microseconds - the precision of a Postgres
     * {@code timestamptz} - so that a value read before persisting is equal to the value
     * reloaded from the database.
     */
    @Column(name = "updated_date", nullable = false)
    private OffsetDateTime updatedDate;

    protected Matching() {
    }

    /**
     * Creates a new matching context with the given initial description.
     *
     * @param matchingDescription Initial description - null or blank if none
     */
    public Matching(@Nullable String matchingDescription) {
        this.matchingDescription = normalize(matchingDescription);
        this.updatedDate = now();
    }

    /**
     * Changes the matching description.
     * <p>
     * A description without any text content means no description (stored as null). The
     * {@link #updatedDate} is only changed if the description actually changes - supplying
     * the same description again (or another blank one when there is no description) changes
     * nothing.
     *
     * @param matchingDescription New description - null or blank if none
     * @return True if the description changed
     */
    public boolean updateMatchingDescription(@Nullable String matchingDescription) {
        final String normalized = normalize(matchingDescription);
        if (Objects.equals(this.matchingDescription, normalized)) {
            return false;
        }
        this.matchingDescription = normalized;
        this.updatedDate = now();
        return true;
    }

    /**
     * Whether the given text - which may be HTML, eg from a rich text editor - has any visible
     * text content, ie anything other than HTML tags, non-breaking spaces and whitespace.
     * <p>
     * For example "&lt;p&gt;&lt;/p&gt;", "&lt;p&gt;&amp;nbsp;&lt;/p&gt;" and "   " all have no
     * text content. This matches {@code hasTextContent} in the admin portal's
     * {@code util/string.ts}.
     */
    public static boolean hasTextContent(@Nullable String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        //Jsoup converts &nbsp; to U+00A0, which String.isBlank does not treat as whitespace.
        return !Jsoup.parse(text).text().replace('\u00a0', ' ').isBlank();
    }

    @Nullable
    private static String normalize(@Nullable String matchingDescription) {
        return hasTextContent(matchingDescription) ? matchingDescription : null;
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
    }
}
