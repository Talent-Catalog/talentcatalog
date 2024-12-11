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

package org.tctalent.server.api.portal;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.BrandingInfo;
import org.tctalent.server.service.db.BrandingService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Access to branding information for the candidate portal.
 */
@RestController()
@RequestMapping("/api/portal/branding")
@Slf4j
public class BrandingPortalApi {

    private final BrandingService brandingService;

    @Autowired
    public BrandingPortalApi(BrandingService brandingService) {
        this.brandingService = brandingService;
    }

    /**
     * Retrieve the branding information for the candidate portal.
     * @return branding information
     */
    @GetMapping()
    public Map<String, Object> getBrandingInfo(
        @RequestParam(value = "p", required = false) final String partnerAbbreviation) {

        if (partnerAbbreviation != null) {
            LogBuilder.builder(log)
                .action("Get branding info")
                .message("Branding Info fetched with partner query param: 'p=" + partnerAbbreviation + "'")
                .logInfo();
        }
        BrandingInfo info = brandingService.getBrandingInfo(partnerAbbreviation);
        return brandingInfoDto().build(info);
    }

    DtoBuilder brandingInfoDto() {
        return new DtoBuilder()
                .add("logo")
                .add("partnerName")
                .add("websiteUrl")
                ;
    }
}
