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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.BrandingInfo;
import org.tbbtalent.server.service.db.BrandingService;

/**
 * Handle redirections when user just types in a domain - eg just tbbtalent.org
 */
@RestController()
@RequestMapping("/")
public class RootRedirectAdminApi {
    private static final Logger log = LoggerFactory.getLogger(RootRedirectAdminApi.class);

    private final BrandingService brandingService;

    @Autowired
    public RootRedirectAdminApi(BrandingService brandingService) {
        this.brandingService = brandingService;
    }

    /**
     * Logic is to go to landing page associated with branding if one has been configured.
     * Otherwise just go to candidate-portal.
     * @param host Host domain of request.
     * @return Redirected url
     */
    @GetMapping()
    public ResponseEntity<Void> redirect(
        @RequestHeader(name="Host", required=false) final String host) {

        BrandingInfo info = brandingService.getBrandingInfo(host);

        String landingPage = info.getLandingPage();

        //If we have a landing page, go there, otherwise go straight to candidate-portal.
        String redirectUrl = landingPage != null ? landingPage : "/candidate-portal/";

        log.info("Redirect requested on host: " + host + ". Associated landing page = '" + (landingPage == null ? "null" : landingPage) + "'. Redirecting to " + redirectUrl);

        //TODO JC Don't actually do redirect
        //return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
        return new ResponseEntity<Void>(HttpStatus.OK);
    }
}
