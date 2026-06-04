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
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.tctalent.server.casi.domain.model.ServiceProvider;

/**
 * Agreement counterparty definition.
 * <p>
 * Interim design note: this entity is a stand-in for a future Org abstraction (Phase 2). The
 * provider identity fields below ({@link #partner}, {@link #employer}, {@link #serviceProvider},
 * {@link #name}) are expected to be normalised into a single Org foreign key when that model is
 * introduced.
 * </p>
 * @author sadatmalik
 */
@Getter
@Setter
@Entity
@Table(name = "counterparty")
@SequenceGenerator(name = "seq_gen", sequenceName = "counterparty_id_seq", allocationSize = 1)
public class Counterparty extends AbstractAuditableDomainObject<Long> {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CounterpartyType type;

    /**
     * Counterparty backed by an existing partner record (for example, source partner).
     * <p>
     * Interim field until Counterparty is normalised to a single Org foreign key.
     * </p>
     */
    @Nullable
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "partner_id")
    private PartnerImpl partner;

    /**
     * Counterparty backed by an existing employer record.
     * <p>
     * Interim field until Counterparty is normalised to a single Org foreign key.
     * </p>
     */
    @Nullable
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "employer_id")
    private Employer employer;

    /**
     * Counterparty represented by CASI service provider enum.
     * <p>
     * Interim field until CASI providers are normalised to Org records.
     * </p>
     */
    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "service_provider")
    private ServiceProvider serviceProvider;

    /**
     * Human-readable counterparty label, including counterparts without a backing entity
     * (for example OPC).
     * <p>
     * Interim field until Counterparty is normalised to a single Org foreign key.
     * </p>
     */
    @Nullable
    private String name;
}
