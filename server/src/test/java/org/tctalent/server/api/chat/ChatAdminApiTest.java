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

package org.tctalent.server.api.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.api.admin.AdminApiTestUtil;
import org.tctalent.server.api.admin.ApiTestBase;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.request.chat.CreateChatRequest;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.JobChatUserService;
import org.tctalent.server.service.db.JobService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.impl.JobChatServiceImpl;

/**
 * @author John Cameron
 */
@WebMvcTest(ChatAdminApi.class)
@AutoConfigureMockMvc
class ChatAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/chat";
    private static final String GET_OR_CREATE_PATH = "/get-or-create";

    private static final JobChat chat = AdminApiTestUtil.getChat();
    private static final List<JobChat> chatList = AdminApiTestUtil.getListOfChats();


    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ChatAdminApi chatAdminApi;

    @MockBean
    JobChatServiceImpl chatService;
    @MockBean
    JobChatUserService jobChatUserService;
    @MockBean
    ChatPostService chatPostService;
    @MockBean
    CandidateOpportunityService candidateOpportunityService;
    @MockBean
    JobService jobService;
    @MockBean
    PartnerService partnerService;
    @MockBean
    UserService userService;

    @BeforeEach
    void setUp() {
        configureAuthentication();

        //todo Maybe this is an indication that getJobChatDtoBuilder in a utility class rather than the service
        when(chatService.getJobChatDtoBuilder()).thenCallRealMethod();
    }

    @Test
    void testWebOnlyContextLoads() {
        assertThat(chatAdminApi).isNotNull();
    }

    @Test
    void create() throws Exception {
        CreateChatRequest request = new CreateChatRequest();

        given(chatService.createJobChat(
            nullable(JobChatType.class), nullable(SalesforceJobOpp.class), nullable(PartnerImpl.class),
            nullable(CandidateOpportunity.class)))
            .willReturn(chat);

        mockMvc.perform(post(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(chat.getId().intValue())));

        verify(chatService).createJobChat(
            nullable(JobChatType.class), nullable(SalesforceJobOpp.class), nullable(PartnerImpl.class),
            nullable(CandidateOpportunity.class)
        );
    }

    @Test
    void list() throws Exception {
        given(chatService.listJobChats())
            .willReturn(chatList);

        mockMvc.perform(get(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$.[0].id", is(chat.getId().intValue())));


        verify(chatService).listJobChats();

    }

    @Test
    void getOrCreate() throws Exception {
        CreateChatRequest request = new CreateChatRequest();

        given(chatService.getOrCreateJobChat(
            nullable(JobChatType.class), nullable(SalesforceJobOpp.class), nullable(PartnerImpl.class),
            nullable(CandidateOpportunity.class)))
            .willReturn(chat);

        mockMvc.perform(post(BASE_PATH + GET_OR_CREATE_PATH)
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(chat.getId().intValue())));

        verify(chatService).getOrCreateJobChat(
            nullable(JobChatType.class), nullable(SalesforceJobOpp.class), nullable(PartnerImpl.class),
            nullable(CandidateOpportunity.class)
        );
    }

    @Test
    void markAsReadUpto() {
    }

    @Test
    void getJobChatUserInfo() {
    }
}
