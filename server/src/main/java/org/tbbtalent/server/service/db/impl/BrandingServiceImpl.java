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

package org.tbbtalent.server.service.db.impl;

import javax.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.BrandingInfo;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.db.partner.Partner;
import org.tbbtalent.server.model.db.partner.SourcePartner;
import org.tbbtalent.server.service.db.BrandingService;
import org.tbbtalent.server.service.db.PartnerService;
import org.tbbtalent.server.service.db.UserService;

/**
 * Implements BrandingService
 *
 * @author John Cameron
 */
@Service
@RequiredArgsConstructor
public class BrandingServiceImpl implements BrandingService {
    private final PartnerService partnerService;
    private final UserService userService;

    /**
     * Returns the branding information for a partner.
     * </p>
     * If a user is logged-in then get the partner associated with that user otherwise get the partner associated with
     * the specified partner abbreviation.
     * </p>
     * If no partner is found gets the default source partner.
     *
     * @param partnerAbbreviation Optional partner abbreviation
     * @return branding info
     */
    @Override
    @NonNull
    public BrandingInfo getBrandingInfo(@Nullable String partnerAbbreviation) {

        User user = userService.getLoggedInUser();

        Partner partner = user != null
                ? user.getPartner()
                : partnerService.getPartnerFromAbbreviation(partnerAbbreviation);

        if (partner == null) {
            partner = partnerService.getDefaultSourcePartner();
        }

        return extractBrandingInfoFromPartner(partner);
    }

    private @NotNull BrandingInfo extractBrandingInfoFromPartner(@NonNull Partner partner) {
        String landingPage = null;

        if (partner instanceof SourcePartner sourcePartner) {
            landingPage = sourcePartner.getRegistrationLandingPage();
        }

        return BrandingInfo.builder()
                .landingPage(landingPage)
                .logo(partner.getLogo())
                .partnerName(partner.getName())
                .websiteUrl(partner.getWebsiteUrl())
                .build();
    }
}
