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

package org.tbbtalent.server.api.admin;

import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.BrandingInfo;
import org.tbbtalent.server.service.db.BrandingService;

@RestController()
@RequestMapping("/")
public class RootRedirectAdminApi {

    private final BrandingService brandingService;

    @Autowired
    public RootRedirectAdminApi(BrandingService brandingService) {
        this.brandingService = brandingService;
    }

    @GetMapping()
    public ResponseEntity<Void> redirect(
        @RequestHeader(name="Host", required=false) final String host) {

        BrandingInfo info = brandingService.getBrandingInfo(host);

        String redirectUrl;
        if (info.getLogo().equals("tbbtalent.org")) {
            //Hard code existing TBB redirect
            // TODO: 12/12/21 This should eventually come from BrandingInfo
            redirectUrl = "https://www.talentbeyondboundaries.org/talentcatalog/";
        } else {
            redirectUrl = "/candidate-portal/";
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }
}
