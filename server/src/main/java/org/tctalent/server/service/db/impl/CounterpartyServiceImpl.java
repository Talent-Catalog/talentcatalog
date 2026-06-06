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

package org.tctalent.server.service.db.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.model.db.Counterparty;
import org.tctalent.server.model.db.CounterpartyType;
import org.tctalent.server.repository.db.CounterpartyRepository;
import org.tctalent.server.service.db.CounterpartyService;

/**
 * Implementation of {@link CounterpartyService}
 *
 * @author sadatmalik
 */
@Service
@RequiredArgsConstructor
public class CounterpartyServiceImpl implements CounterpartyService {

    private final CounterpartyRepository counterpartyRepository;

    @Override
    @NonNull
    @Transactional
    public Counterparty findOrCreateByTypeAndName(@NonNull CounterpartyType type, @NonNull String name) {
        return counterpartyRepository.findByTypeAndNameIgnoreCase(type, name)
            .orElseGet(() -> {
                Counterparty counterparty = new Counterparty();
                counterparty.setType(type);
                counterparty.setName(name);
                return counterpartyRepository.save(counterparty);
            });
    }
}
