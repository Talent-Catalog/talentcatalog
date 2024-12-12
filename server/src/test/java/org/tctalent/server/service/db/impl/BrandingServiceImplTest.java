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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.BrandingInfo;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.UserService;

/**
 * @author sadatmalik
 */
@ExtendWith(MockitoExtension.class)
class BrandingServiceImplTest {

    private static final String LANDING_PAGE = "https://www.talentbeyondboundaries.org/talentcatalog";
    private static final String LOGO = "https://images.squarespace-cdn.com/content/v1/my-logo";
    private static final String PARTNER_NAME = "Talent Beyond Boundaries";
    private static final String WEBSITE_URL = "https://www.talentbeyondboundaries.org";

    private static final PartnerImpl partner = new PartnerImpl();

    @Mock PartnerService partnerService;
    @Mock UserService userService;
    @Mock User user;

    @InjectMocks
    BrandingServiceImpl brandingService;

    @BeforeEach
    void setUp() {
        partner.setSourcePartner(true);
        partner.setRegistrationLandingPage(LANDING_PAGE);
        partner.setLogo(LOGO);
        partner.setName(PARTNER_NAME);
        partner.setWebsiteUrl(WEBSITE_URL);
    }

    @Test
    @DisplayName("get branding info succeeds when logged in user")
    void canGetBrandingInfoWithLoggedInUser() {
        given(userService.getLoggedInUser()).willReturn(user);
        given(user.getPartner()).willReturn(partner);

        BrandingInfo info = brandingService.getBrandingInfo(null);

        assertNotNull(info);
        verifyBrandingInfo(info);
    }

    @Test
    @DisplayName("get branding info succeeds - no logged in user - partner abbreviation provided")
    void canGetBrandingInfoWithPartnerAbbreviation() {
        given(userService.getLoggedInUser()).willReturn(null);
        given(partnerService.getPartnerFromAbbreviation(anyString())).willReturn(partner);

        BrandingInfo info = brandingService.getBrandingInfo("source-partner");

        assertNotNull(info);
        verifyBrandingInfo(info);
    }

    @Test
    @DisplayName("get branding info succeeds - default source partner")
    void canGetBrandingInfoWithDefaultSourcePartner() {
        given(userService.getLoggedInUser()).willReturn(null);
        given(partnerService.getPartnerFromAbbreviation(anyString())).willReturn(null);
        given(partnerService.getDefaultSourcePartner()).willReturn(partner);

        BrandingInfo info = brandingService.getBrandingInfo("source-partner");

        assertNotNull(info);
        verifyBrandingInfo(info);
    }

    private void verifyBrandingInfo(BrandingInfo info) {
        assertEquals(LANDING_PAGE, info.getLandingPage());
        assertEquals(LOGO, info.getLogo());
        assertEquals(PARTNER_NAME, info.getPartnerName());
        assertEquals(WEBSITE_URL, info.getWebsiteUrl());
    }

    private void verifyNonSourcePartnerBrandingInfo(BrandingInfo info) {
        assertNull(info.getLandingPage());
        assertEquals(LOGO, info.getLogo());
        assertEquals(PARTNER_NAME, info.getPartnerName());
        assertEquals(WEBSITE_URL, info.getWebsiteUrl());
    }

}
