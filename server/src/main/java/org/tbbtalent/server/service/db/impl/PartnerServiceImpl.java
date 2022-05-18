/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.PartnerImpl;
import org.tbbtalent.server.model.db.partner.Partner;
import org.tbbtalent.server.repository.db.PartnerRepository;
import org.tbbtalent.server.service.db.PartnerService;

@Service
public class PartnerServiceImpl implements PartnerService {
    private final PartnerRepository partnerRepository;

    public PartnerServiceImpl(
        PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    @NonNull
    @Override
    public Partner get(long partnerId) throws NoSuchObjectException {
        final PartnerImpl partner = partnerRepository.findById(partnerId)
            .orElseThrow(() -> new NoSuchObjectException(Partner.class, partnerId));

        return partner;
    }

    @NonNull
    @Override
    public Partner getDefaultSourcePartner() throws NoSuchObjectException {
        final PartnerImpl partner = partnerRepository.findByDefaultSourcePartner(true)
            .orElseThrow(() -> new NoSuchObjectException(Partner.class, "default"));

        return partner;
    }

    @Nullable
    @Override
    public Partner getPartnerFromHost(String hostDomain) {
        final PartnerImpl partner = partnerRepository.findByRegistrationUrl(hostDomain)
            .orElse(null);

        return partner;
    }


}
