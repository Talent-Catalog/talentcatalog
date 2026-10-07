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
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;

/**
 * A previously generated, persisted LLM explanation of how a candidate relates to a specific
 * Talent Catalog job.
 * <p/>
 * At most one explanation is kept per (candidate, job) pair - generating a new explanation for
 * the same pair replaces the existing record rather than creating another one. This is expressed
 * as a composite-key entity in the same style as {@link org.tctalent.server.model.db.CandidateSavedList}.
 * <p/>
 * This entity is candidate/job *contextual* data (an LLM-generated artifact about a candidate/job
 * pairing), not candidate profile data, so it does not extend
 * {@link org.tctalent.server.model.db.AbstractCandidateDataDomainObject}. It also cannot extend
 * {@link org.tctalent.server.model.db.AbstractAuditableDomainObject} (nor, transitively,
 * {@code AbstractCandidateDataDomainObject}): that hierarchy declares a single {@code @Id
 * @GeneratedValue(strategy = SEQUENCE)} column, which is fundamentally incompatible with an
 * {@link EmbeddedId} composite key populated via {@link MapsId} - exactly why
 * {@code CandidateSavedList} itself does not extend those base classes either. Spring Data JPA
 * auditing is therefore wired up directly on this entity instead, mirroring the same
 * {@code @CreatedDate}/{@code @CreatedBy}/{@code @LastModifiedDate}/{@code @LastModifiedBy} +
 * {@link AuditingEntityListener} approach that
 * {@link org.tctalent.server.model.db.AbstractCandidateDataDomainObject} uses.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "candidate_job_match_explanation")
@EntityListeners(AuditingEntityListener.class)
public class CandidateJobMatchExplanation {

    @EqualsAndHashCode.Include
    @EmbeddedId
    CandidateJobMatchExplanationKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("candidateId")
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jobId")
    @JoinColumn(name = "job_id")
    private SalesforceJobOpp job;

    /**
     * The complete generated explanation, stored as a single {@code jsonb} column rather than
     * normalized into separate rows/tables.
     */
    @Type(JsonBinaryType.class)
    @Column(name = "explanation", nullable = false, columnDefinition = "jsonb")
    private CandidateJobMatchExplanationData explanation;

    @CreatedDate
    @Column(name = "created_date")
    private OffsetDateTime createdDate;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @LastModifiedDate
    @Column(name = "updated_date")
    private OffsetDateTime updatedDate;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    public CandidateJobMatchExplanation() {
    }

    public CandidateJobMatchExplanation(Candidate candidate, SalesforceJobOpp job) {
        this.candidate = candidate;
        this.job = job;
        this.id = new CandidateJobMatchExplanationKey(candidate.getId(), job.getId());
    }
}
