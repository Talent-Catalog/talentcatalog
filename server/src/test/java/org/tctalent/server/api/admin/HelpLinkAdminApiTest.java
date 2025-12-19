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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.HelpLinkTestData.getHelpLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;
import org.tctalent.server.request.helplink.UpdateHelpLinkRequest;
import org.tctalent.server.service.db.CountryService;
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

    private static final HelpLink helpLink = getHelpLink();

    @MockBean
    CountryService countryService;

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

    @Test
    void fetch() throws Exception {
        SearchHelpLinkRequest request = new SearchHelpLinkRequest();
        given(helpLinkService.fetchHelp(any())).willReturn(List.of(helpLink));

        mockMvc.perform(post(BASE_PATH + "/fetch")
                .with(csrf())
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id", is(99)))
            .andExpect(jsonPath("$[0].country.name", is("Jordan")));

        verify(helpLinkService).fetchHelp(any());
    }
    @Test
    void search() throws Exception {
        SearchHelpLinkRequest request = new SearchHelpLinkRequest();
        given(helpLinkService.search(any())).willReturn(List.of(helpLink));

        mockMvc.perform(post(BASE_PATH + "/search")
                .with(csrf())
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id", is(99)))
            .andExpect(jsonPath("$[0].country.name", is("Jordan")));

        verify(helpLinkService).search(any());
    }

    @Test
    void update() throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        UpdateHelpLinkRequest req = new UpdateHelpLinkRequest();
        HelpLink mockHelpLink = new HelpLink();
        mockHelpLink.setId(456L);
        given(helpLinkService.updateHelpLink(eq(456L), any())).willReturn(mockHelpLink);

        Map<String, Object> dto = helpLinkAdminApi.update(456L, req);

        assertNotNull(dto);
        assertEquals(456L, dto.get("id"));
        verify(helpLinkService).updateHelpLink(456L, req);
    }


    @Test
    void delete_shouldReturnTrue() throws EntityReferencedException, InvalidRequestException {
        given(helpLinkService.deleteHelpLink(1L)).willReturn(true);

        boolean result = helpLinkAdminApi.delete(1L);

        assertTrue(result);
        verify(helpLinkService).deleteHelpLink(1L);
    }

    @Test
    void searchPaged_returnsDtoWithContent() {
        // Arrange
        SearchHelpLinkRequest request = new SearchHelpLinkRequest();

        HelpLink helpLink = new HelpLink();
        helpLink.setId(1L);

        Page<HelpLink> page = new PageImpl<>(List.of(helpLink));
        given(helpLinkService.searchPaged(any(SearchHelpLinkRequest.class))).willReturn(page);

        // Act
        var result = helpLinkAdminApi.searchPaged(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).containsKey("content");
        var content = (List<?>) result.get("content");
        assertThat(content).isNotEmpty();

        Mockito.verify(helpLinkService).searchPaged(request);
    }
}
