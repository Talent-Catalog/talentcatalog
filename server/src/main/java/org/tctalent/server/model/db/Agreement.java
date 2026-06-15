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

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * Candidate agreement instance with explicit lifecycle dates.
 *
 * @author sadatmalik
 */
@Getter
@Setter
@Entity
@Table(name = "agreement")
@SequenceGenerator(name = "seq_gen", sequenceName = "agreement_id_seq", allocationSize = 1)
public class Agreement extends AbstractAuditableDomainObject<Long> {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "counterparty_id", nullable = false)
    private Counterparty counterparty;

    /**
     * Accepted TermsInfo id (version-specific).
     * <p>
     * Interim design note: currently references the in-memory TermsInfo registry. When TermsInfo is
     * promoted to a table in a later phase, this field is expected to become a foreign key.
     * </p>
     */
    @Column(name = "terms_info_id", nullable = false)
    private String termsInfoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false)
    private TermsType termsType;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime start;

    /**
     * Agreement end timestamp. Null means the agreement is currently active.
     */
    @Nullable
    @Column(name = "end_date")
    private OffsetDateTime end;
}
