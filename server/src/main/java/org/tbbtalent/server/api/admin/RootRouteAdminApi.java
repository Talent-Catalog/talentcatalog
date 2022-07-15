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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.tbbtalent.server.model.db.BrandingInfo;
import org.tbbtalent.server.service.db.BrandingService;
import org.tbbtalent.server.util.SubdomainRedirectHelper;

/**
 * Handle rerouting when user just types in a domain - eg just tctalent.org
 */
@RestController()
@RequestMapping("/")
public class RootRouteAdminApi {
    private static final Logger log = LoggerFactory.getLogger(RootRouteAdminApi.class);

    private final BrandingService brandingService;

    @Autowired
    public RootRouteAdminApi(BrandingService brandingService) {
        this.brandingService = brandingService;
    }

    /**
     * Logic is to go to landing page associated with branding if one has been configured,
     * otherwise just go to candidate-portal.
     * @return Rerouted url
     */
    @GetMapping()
    public Object route(
        @RequestHeader MultiValueMap<String, String> headers,
        @RequestHeader(name="Host", required=false) final String host,
        @RequestHeader(name=":authority", required=false) final String authority,
        @RequestParam(value = "p", required = false) final String partnerAbbreviation,
        @RequestParam(value = "h", required = false) final String showHeaders) {

        //TODO JC This is where a tctalent.org subdomain url can redirect to a plain url with p= query
        //eg crs.tctalent.org --> tctalent.org?p=crs

        if (showHeaders != null) {
            headers.forEach((key, value) -> {
                log.info(String.format(
                    "Header '%s' = %s", key, value.stream().collect(Collectors.joining("|"))));
            });
        }

        //TODO JC Install filter - see https://tomgregory.com/spring-boot-behind-load-balancer-using-x-forwarded-headers/

        String redirectUrl = SubdomainRedirectHelper.computeRedirectUrl(host);
        if (redirectUrl != null) {
            log.info("Redirecting to: " + redirectUrl);
            return new ModelAndView("redirect:" + redirectUrl);
        }

        BrandingInfo info = brandingService.getBrandingInfo(partnerAbbreviation);

        String landingPage = info.getLandingPage();

        //If we have a landing page, go there, otherwise go straight to candidate-portal.
        String routingUrl;
        if (landingPage != null) {
            routingUrl = landingPage;
        } else {
            routingUrl = "/candidate-portal/";
            if (partnerAbbreviation != null) {
                routingUrl += "?p=" + partnerAbbreviation;
            }
        }

        String infoMess = "RootRouting: Host " + host + ", :authority " + authority;
        if (partnerAbbreviation != null) {
            infoMess += ", Partner specified 'p=" + partnerAbbreviation + "'";
        }
        log.info(infoMess);
        log.info("Routing to landing page: " + routingUrl);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(routingUrl)).build();
    }
}
