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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.BrandingInfo;
import org.tctalent.server.service.db.BrandingService;

/**
 * @author sadatmalik
 */
@WebMvcTest(BrandingAdminApi.class)
@AutoConfigureMockMvc
class BrandingAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/branding";
    private static final String LOGO = "https://images.squarespace-cdn.com/content/v1/my-logo";
    private static final String PARTNER_NAME = "Talent Beyond Boundaries";
    private static final String WEBSITE_URL = "https://www.talentbeyondboundaries.org";

    private static final BrandingInfo brandingInfo = new BrandingInfo();

    static {
        brandingInfo.setLogo(LOGO);
        brandingInfo.setPartnerName(PARTNER_NAME);
        brandingInfo.setWebsiteUrl(WEBSITE_URL);
    }

    @Autowired BrandingAdminApi brandingAdminApi;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean BrandingService brandingService;

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(brandingAdminApi).isNotNull();
    }

    @Test
    @DisplayName("get branding info succeeds")
    void getBrandingInfoSucceeds() throws Exception {
        given(brandingService.getBrandingInfo(any())).willReturn(brandingInfo);

        mockMvc.perform(get(BASE_PATH)
                .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.logo", containsString(LOGO)))
                .andExpect(jsonPath("$.partnerName", containsString(PARTNER_NAME)))
                .andExpect(jsonPath("$.websiteUrl", containsString(WEBSITE_URL)));
    }
}
