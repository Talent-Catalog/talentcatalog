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

package org.tctalent.server.service.db;

import org.springframework.lang.NonNull;
import org.tctalent.server.model.db.Counterparty;
import org.tctalent.server.model.db.CounterpartyType;
import org.tctalent.server.model.db.PartnerImpl;

/**
 * Service responsible for creating/finding agreement counterparties.
 *
 * @author sadatmalik
 */
public interface CounterpartyService {

    /**
     * Finds an existing named counterparty (case-insensitive), or creates one if none exists.
     *
     * @param type Counterparty type
     * @param name Counterparty display name
     * @return Existing or newly created counterparty
     */
    @NonNull
    Counterparty findOrCreateByTypeAndName(@NonNull CounterpartyType type, @NonNull String name);

    /**
     * Finds an existing partner-backed counterparty, or creates one if none exists.
     * <p>
     * Prefer this over {@link #findOrCreateByTypeAndName} when the counterparty maps to a known
     * {@link PartnerImpl} record (for example, the OPC system partner). Using the partner FK
     * provides a proper relational link and exercises the {@code cp_type_partner_uq_idx} unique
     * index rather than relying on a name string.
     * </p>
     *
     * @param type    Counterparty type
     * @param partner Backing partner entity
     * @return Existing or newly created counterparty
     */
    @NonNull
    Counterparty findOrCreateByTypeAndPartner(@NonNull CounterpartyType type, @NonNull PartnerImpl partner);
}
