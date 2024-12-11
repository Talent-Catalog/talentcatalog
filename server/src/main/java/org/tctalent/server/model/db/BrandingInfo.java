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

package org.tctalent.server.model.db;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * Partner branding information.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class BrandingInfo {

    /**
     * Optional landing page associated with branding - eg TBB redirects to
     * https://www.talentbeyondboundaries.org/talentcatalog/.
     * <p/>
     * If no landing page is configured, the default is to automatically go to candidate-portal
     */
    @Nullable
    private String landingPage;

    /**
     * Optional path to logo to display with branding.
     */
    @Nullable
    private String logo;

    /**
     * Partner name associated with branding, if known. Null if unknown.
     */
    @Nullable
    private String partnerName;

    /**
     * Website associated with logo
     */
    @Nullable
    private String websiteUrl;

}
