/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.BrandingInfo;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.BrandingService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.UserService;

import javax.validation.constraints.NotNull;

/**
 * Implements BrandingService
 *
 * @author John Cameron
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BrandingServiceImpl implements BrandingService {
    private final PartnerService partnerService;
    private final UserService userService;
    private final AuthService authService;

    /**
     * Returns the branding information for a partner.
     *
     * @param partnerAbbreviation Optional partner abbreviation
     * @return branding info
     */
    @Override
    @NonNull
    public BrandingInfo getBrandingInfo(@Nullable String partnerAbbreviation) {

        User user = userService.getLoggedInUser();

        Partner partner;
        if (user != null) {
            //Logged in - set partner associated with user
            partner = user.getPartner();
        } else {
            //Not logged in - try and determine partner
            //Look up any partnerAbbreviation
            partner = partnerService.getPartnerFromAbbreviation(partnerAbbreviation);
        }

        if (partner == null) {
            //Used default partner if none found so far
            partner = partnerService.getDefaultSourcePartner();
        }

        // When logged-in user is a candidate, check for and replace partner if it has a
        // redirectPartner assigned — typically because it is no longer active and another org has
        // assumed responsibility for candidates in its jurisdiction.
        // Set by SystemAdminApi.redirectInactivePartnerUrl.
        if (user != null && user.getRole() == Role.user) { // User is a candidate
            while (partner.getRedirectPartner() != null) {
                LogBuilder.builder(log) // Log the reassignment
                    .user(authService.getLoggedInUser())
                    .action("Get Branding Info")
                    .message(partner.getName() + " has a redirectPartner assigned - request "
                        + "redirected to " + partner.getRedirectPartner().getName() + ".")
                    .logInfo();

                partner = partner.getRedirectPartner();
            }
        }

        return extractBrandingInfoFromPartner(partner);
    }

    private @NotNull BrandingInfo extractBrandingInfoFromPartner(@NonNull Partner partner) {
        BrandingInfo info = new BrandingInfo();
        info.setLogo(partner.getLogo());
        if (partner.isSourcePartner()) {
            info.setLandingPage(partner.getRegistrationLandingPage());
        }
        info.setPartnerName(partner.getName());
        info.setWebsiteUrl(partner.getWebsiteUrl());
        return info;
    }
}
