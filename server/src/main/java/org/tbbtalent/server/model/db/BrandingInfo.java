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

package org.tbbtalent.server.model.db;

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
     * Host domain associated with this branding - eg tbbtalent.org or unhcrtalent.org
     */
    @Nullable
    private String hostDomain;

    /**
     * Optional landing page associated with branding - eg for tbbtalent.org we redirect to
     * https://www.talentbeyondboundaries.org/talentcatalog/.
     * <p/>
     * If no landing page is configured, the default is to automatically go to candidate-portal
     */
    @Nullable
    private String landingPage;

    /**
     * Optional path to logo to display with branding.
     * <p/>
     * If no logo is supplied, a default is displayed (eg the TBB logo)
     */
    @Nullable
    private String logo;

}
