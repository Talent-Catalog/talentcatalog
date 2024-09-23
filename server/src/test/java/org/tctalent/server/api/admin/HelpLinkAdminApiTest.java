/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.request.helplink.UpdateHelpLinkRequest;
import org.tctalent.server.service.db.HelpLinkService;

/**
 * Test HelpLink
 *
 * @author John Cameron
 */
@WebMvcTest(HelpLinkAdminApi.class)
@AutoConfigureMockMvc
class HelpLinkAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/help-link";

    private static final HelpLink helpLink = AdminApiTestUtil.getHelpLink();


    @MockBean
    HelpLinkService helpLinkService;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired HelpLinkAdminApi helpLinkAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(helpLinkAdminApi).isNotNull();
    }

    @Test
    void create() throws Exception {
        UpdateHelpLinkRequest request = new UpdateHelpLinkRequest();

        given(helpLinkService
            .createHelpLink(any(UpdateHelpLinkRequest.class)))
            .willReturn(helpLink);

        mockMvc.perform(post(BASE_PATH)
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(99)))
            .andExpect(jsonPath("$.country.name", is("Jordan")))
            .andExpect(jsonPath("$.caseStage", is("cvReview")))
            .andExpect(jsonPath("$.jobStage", is("jobOffer")))
            .andExpect(jsonPath("$.label", is("Test label")))
            .andExpect(jsonPath("$.link", is("https://www.talentbeyondboundaries.org/")))
        ;

        verify(helpLinkService).createHelpLink(any(UpdateHelpLinkRequest.class));

    }
}
