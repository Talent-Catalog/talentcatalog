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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author sadatmalik
 */
class BrandingInfoTest {

    private static final String LANDING_PAGE = "https://www.talentbeyondboundaries.org/talentcatalog";
    private static final String LOGO = "https://images.squarespace-cdn.com/content/v1/my-logo";
    private static final String PARTNER_NAME = "Talent Beyond Boundaries";
    private static final String WEBSITE_URL = "https://www.talentbeyondboundaries.org";

    private BrandingInfo brandingInfo;

    @BeforeEach
    void setUp() {
        brandingInfo = new BrandingInfo();
        brandingInfo.setLandingPage(LANDING_PAGE);
        brandingInfo.setLogo(LOGO);
        brandingInfo.setPartnerName(PARTNER_NAME);
        brandingInfo.setWebsiteUrl(WEBSITE_URL);
    }

    @Test
    @DisplayName("branding info builder sets all required fields")
    void testBuilderWithGetters() {
        assertEquals(LANDING_PAGE, brandingInfo.getLandingPage());
        assertEquals(LOGO, brandingInfo.getLogo());
        assertEquals(PARTNER_NAME, brandingInfo.getPartnerName());
        assertEquals(WEBSITE_URL, brandingInfo.getWebsiteUrl());
    }
}
