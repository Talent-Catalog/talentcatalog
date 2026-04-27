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

package org.tctalent.server.api.admin;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.BrandingInfo;
import org.tctalent.server.service.db.BrandingService;
import org.tctalent.server.service.db.RootRequestService;

/**
 * Handle rerouting when user just types in a domain - eg just tctalent.org
 */
@RestController
@RequestMapping("/")
@Slf4j
public class RootRouteAdminApi {

    private final BrandingService brandingService;
    private final RootRequestService rootRequestService;

    @Autowired
    public RootRouteAdminApi(BrandingService brandingService, RootRequestService rootRequestService) {
        this.brandingService = brandingService;
        this.rootRequestService = rootRequestService;
    }

    /**
     * Logic is to go to landing page associated with branding if one has been configured,
     * otherwise just go to candidate-portal/login.
     * @return Rerouted url or redirect for partner subdomains
     */
    @GetMapping
    public Object route(
        HttpServletRequest request,
        @RequestHeader MultiValueMap<String, String> headers,
        @RequestHeader(name="Host", required=false) final String host,
        @RequestParam(value = "utm_source", required = false) final String utmSource,
        @RequestParam(value = "utm_medium", required = false) final String utmMedium,
        @RequestParam(value = "utm_campaign", required = false) final String utmCampaign,
        @RequestParam(value = "utm_term", required = false) final String utmTerm,
        @RequestParam(value = "utm_content", required = false) final String utmContent,
        @RequestParam(value = "p", required = false) final String partnerParam,
        @RequestParam(value = "r", required = false) final String referrerParam,
        @RequestParam(value = "h", required = false) final String showHeaders) {


        if (showHeaders != null) {
            headers.forEach((key, value) ->
                LogBuilder.builder(log)
                    .action("Route")
                    .message(String.format("Header '%s' = %s", key, String.join("|", value)))
                    .logInfo());

            String ipAddress = request.getHeader("X-Forward-For");
            if(ipAddress == null) {
                LogBuilder.builder(log)
                    .action("Route")
                    .message("Ip address: " + request.getRemoteAddr())
                    .logInfo();
            }
        }

        String queryString = request.getQueryString();
        //Store query information
        if (queryString != null) {
            storeQueryInfo(request, partnerParam, referrerParam, utmSource, utmMedium, utmCampaign, utmTerm, utmContent);
        }

        BrandingInfo info = brandingService.getBrandingInfo(partnerParam);

        String landingPage = info.getLandingPage();

        //If we have a landing page, go there, otherwise go straight to candidate-portal/login.
        String routingUrl = Objects.requireNonNullElse(landingPage, "/candidate-portal/login");

        if (queryString != null) {
            routingUrl += "?" + queryString;
        }

        if (partnerParam != null) {
            String infoMess = "RootRouting";
            if (host != null) {
                infoMess += ": Host " + host;
            }
            infoMess += ", Partner specified 'p=" + partnerParam + "'";

            LogBuilder.builder(log)
                .action("Route")
                .message(infoMess)
                .logInfo();

            LogBuilder.builder(log)
                .action("Route")
                .message("Routing to landing page: " + routingUrl)
                .logInfo();
        }

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(routingUrl)).build();
    }

    private void storeQueryInfo(HttpServletRequest request,
        String partnerParam, String referrerParam,
        String utmSource, String utmMedium, String utmCampaign, String utmTerm, String utmContent) {

        rootRequestService.createRootRequest(request, partnerParam, referrerParam,
            utmSource, utmMedium, utmCampaign, utmTerm, utmContent);
    }
}
