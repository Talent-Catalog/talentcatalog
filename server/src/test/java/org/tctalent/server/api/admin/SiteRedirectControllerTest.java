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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author John Cameron
 */
@WebMvcTest(SiteRedirectController.class)
@AutoConfigureMockMvc
class SiteRedirectControllerTest extends ApiTestBase {

    private static final String BASE_PATH = "/backend/jobseeker";
    private static final String VIEW_RESUME_PATH = "/view-resume";


    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    SiteRedirectController siteRedirectController;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(siteRedirectController).isNotNull();
    }

    @Test
    void redirectOldResumeUrl() throws Exception {
        String testCandidateNumber = "12345";

        mockMvc.perform(get(BASE_PATH + VIEW_RESUME_PATH)
                .param("id", testCandidateNumber)
                .header("Authorization", "Bearer " + "jwt-token")
                .accept(MediaType.APPLICATION_OCTET_STREAM))

            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(
                "https://tctalent.org/admin-portal/candidate/" + testCandidateNumber));
    }
}
