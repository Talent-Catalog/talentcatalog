/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import org.tbbtalent.server.model.db.BrandingInfo;

/**
 * Manages branding based on host domain.
 *
 * @author John Cameron
 */
public interface BrandingService {

    /**
     * Returns the branding info to be displayed to a user.
     * <p/>
     * If the user is logged in, the branding associated with the host domain that the user
     * last logged in with is used.
     * If the user is not logged in, the branding associated with the given host domain is used.
     * <p/>
     * If no branding is found associated with a particular host domain, the default branding is
     * returned (TBB's branding).
     * @param hostDomain Host domain with which the current HTTP request is associated.
     * @return Branding information.
     */
    @NonNull
    BrandingInfo getBrandingInfo(String hostDomain);

}
