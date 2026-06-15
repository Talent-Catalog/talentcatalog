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

package org.tctalent.server.repository.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.model.db.Counterparty;
import org.tctalent.server.model.db.CounterpartyType;

/**
 * Repository for {@link Counterparty} entities.
 * <p>
 * Interim design note: the multiple finder methods below
 * ({@link #findByTypeAndPartnerId(CounterpartyType, Long)},
 * {@link #findByTypeAndEmployerId(CounterpartyType, Long)},
 * {@link #findByTypeAndServiceProvider(CounterpartyType, ServiceProvider)},
 * {@link #findByTypeAndNameIgnoreCase(CounterpartyType, String)}) exist because counterparties
 * are currently represented via several provider identity forms rather than a unified Org entity.
 * </p>
 * <p>
 * When the Org abstraction is introduced in a later phase, these lookups are expected to collapse
 * to a single Org-based finder.
 * </p>
 * @author sadatmalik
 */
public interface CounterpartyRepository extends JpaRepository<Counterparty, Long> {

    Optional<Counterparty> findByTypeAndPartnerId(CounterpartyType type, Long partnerId);

    Optional<Counterparty> findByTypeAndEmployerId(CounterpartyType type, Long employerId);

    Optional<Counterparty> findByTypeAndServiceProvider(CounterpartyType type, ServiceProvider serviceProvider);

    Optional<Counterparty> findByTypeAndNameIgnoreCase(CounterpartyType type, String name);
}
