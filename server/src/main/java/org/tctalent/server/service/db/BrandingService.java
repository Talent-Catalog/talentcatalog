/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.BrandingInfo;

/**
 * Manages branding based on host domain.
 *
 * @author John Cameron
 */
public interface BrandingService {

    /**
     * Returns the branding info to be displayed to a user.
     * <p/>
     * If the user is logged in, the branding of the user's associated partner is used.
     * If the user is not logged in, the branding associated with the given partner abbreviation
     * is used.
     * <p/>
     * If no branding is found, the default branding is returned (TBB's branding).
     * @param partnerAbbreviation Optional partner abbreviation
     * @return Branding information.
     */
    @NonNull
    BrandingInfo getBrandingInfo(@Nullable String partnerAbbreviation);

}
