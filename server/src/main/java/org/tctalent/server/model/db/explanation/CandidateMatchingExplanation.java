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

package org.tctalent.server.model.db.explanation;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.tctalent.server.model.db.Matching;
import org.tctalent.server.model.db.User;

/**
 * A cached LLM explanation of how a candidate's job experience relates to the description of a
 * {@link Matching}.
 * <p/>
 * This is disposable cache data, not history: at most one explanation is kept per (candidate,
 * Matching) pair, and generating a new one replaces it. It doesn't depend on how the Matching is
 * used - by a job, a search or a list.
 * <p/>
 * {@link #matchingUpdatedDate} records the Matching's {@link Matching#getUpdatedDate() updatedDate}
 * when generation started. The explanation is current while that still equals the Matching's
 * updatedDate - see {@link #isCurrent}. Changes to the candidate's own experience are not
 * tracked, so they don't make an explanation stale.
 * <p/>
 * Read only here. Explanations are only written by
 * {@link org.tctalent.server.repository.db.CandidateMatchingExplanationRepository#saveIfMatchingUnchanged},
 * a conditional upsert which also sets the audit columns. That is why this entity is not audited
 * by Spring Data, unlike similar entities.
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Immutable
@Table(name = "candidate_matching_explanation")
public class CandidateMatchingExplanation {

    @EqualsAndHashCode.Include
    @EmbeddedId
    private CandidateMatchingExplanationKey id;

    /**
     * The Matching's updatedDate when this explanation's generation started.
     */
    @Column(name = "matching_updated_date", nullable = false)
    private OffsetDateTime matchingUpdatedDate;

    /**
     * The complete generated explanation, stored as a single {@code jsonb} column rather than
     * normalized into separate rows/tables.
     */
    @Type(JsonBinaryType.class)
    @Column(name = "explanation", nullable = false, columnDefinition = "jsonb")
    private CandidateMatchingExplanationData explanation;

    @Column(name = "created_date")
    private OffsetDateTime createdDate;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "updated_date")
    private OffsetDateTime updatedDate;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    protected CandidateMatchingExplanation() {
    }

    /**
     * Whether this explanation was generated from the Matching description that was current at
     * the given Matching updatedDate.
     * <p/>
     * Compared as instants: OffsetDateTime.equals would also compare offsets, which can differ
     * between a value read from the database and one created in Java.
     *
     * @param matchingUpdatedDate The Matching's current updatedDate
     * @return True if this explanation is current
     */
    public boolean isCurrent(OffsetDateTime matchingUpdatedDate) {
        return this.matchingUpdatedDate.isEqual(matchingUpdatedDate);
    }
}
