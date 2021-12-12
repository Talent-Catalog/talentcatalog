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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.BrandingInfo;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.BrandingService;

/**
 * Implements BrandingService
 *
 * @author John Cameron
 */
@Service
public class BrandingServiceImpl implements BrandingService {
    private final AuthService authService;

    private final static String DEFAULT_BRANDING_DOMAIN = "tbbtalent.org";
    private final Map<String, BrandingInfo> domainToBrandingMap = new HashMap<>();

    public BrandingServiceImpl(AuthService authService) {
        this.authService = authService;

        initializeBrandingInfos();
    }

    private void initializeBrandingInfos() {
        BrandingInfo info;

        info = new BrandingInfo();
        info.setHostDomain(DEFAULT_BRANDING_DOMAIN);
        info.setLogo("assets/images/tbbLogo.png");
        info.setLandingPage("https://www.talentbeyondboundaries.org/talentcatalog/");
        domainToBrandingMap.put(info.getHostDomain(), info);

        info = new BrandingInfo();
        info.setHostDomain("unhcrtalent.org");
        info.setLogo("assets/images/unhcrLogo.png");
        domainToBrandingMap.put(info.getHostDomain(), info);

        info = new BrandingInfo();
        info.setHostDomain("iomtalent.org");
        info.setLogo("assets/images/iomLogo.png");
        domainToBrandingMap.put(info.getHostDomain(), info);
    }

    @Override
    @NonNull
    public BrandingInfo getBrandingInfo(String hostDomain) {

        String activeDomain;
        Optional<User> user = authService.getLoggedInUser();
        if (user.isPresent()) {
            //Logged in - use domain they logged in on
            User loggedInUser = user.get();
            activeDomain = loggedInUser.getHostDomain();
        } else {
            //Not logged in - use domain passed in.
            activeDomain = hostDomain;
        }

        return selectBrandingInfoByDomain(activeDomain);
    }

    private @NotNull BrandingInfo selectBrandingInfoByDomain(@Nullable String hostDomain) {
        BrandingInfo info = null;
        if (hostDomain != null) {
            info = domainToBrandingMap.get(hostDomain);
        }
        if (info == null) {
            info = domainToBrandingMap.get(DEFAULT_BRANDING_DOMAIN);
        }
        return info;
    }
}
