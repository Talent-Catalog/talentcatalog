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

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.BrandingInfo;
import org.tctalent.server.service.db.BrandingService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Access to branding information for the admin portal.
 */
@RestController()
@RequestMapping("/api/admin/branding")
@Slf4j
@RequiredArgsConstructor
public class BrandingAdminApi {

    private final BrandingService brandingService;

    @GetMapping()
    public Map<String, Object> getBrandingInfo() {

        BrandingInfo info = brandingService.getBrandingInfo(null);
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
