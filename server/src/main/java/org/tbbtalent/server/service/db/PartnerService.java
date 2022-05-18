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

package org.tbbtalent.server.service.db;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.partner.Partner;

/**
 * Service for managing {@link Partner}
 *
 * @author John Cameron
 */
public interface PartnerService {

    /**
     * Get the Partner with the given id.
     * @param partnerId ID of partner to get
     * @return partner
     * @throws NoSuchObjectException if there is no Partner with this id.
     */
    @NonNull
    Partner get(long partnerId) throws NoSuchObjectException;

    /**
     * Get the default source partner (eg TBB).
     * @return Default source partner (as defined in database - defaultSourcePartner field)
     * @throws NoSuchObjectException if no default source partner is configured
     */
    @NonNull
    Partner getDefaultSourcePartner() throws NoSuchObjectException;

    /**
     * Get the partner associated with the given domain name.
     * @param hostDomain Domain name - eg tbbtalent.org
     * @return Partner associated with domain or null if no partner is found associated with that domain
     */
    @Nullable
    Partner getPartnerFromHost(String hostDomain);

}
