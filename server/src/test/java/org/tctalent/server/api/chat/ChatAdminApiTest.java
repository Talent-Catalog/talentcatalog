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

package org.tctalent.server.api.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.tctalent.server.api.admin.ApiTestBase;
import org.tctalent.server.data.JobChatTestData;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.JobChatUserInfo;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.chat.CreateChatRequest;
import org.tctalent.server.service.db.CandidateService;
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
    private static final String GET_CHAT_USER_INFO = "/get-chat-user-info";
    private static final String POST = "/post";
    private static final String READ = "/read";
    private static final String USER = "/user";

    private static final JobChat chat = JobChatTestData.getChat();
    private static final List<JobChat> chatList = JobChatTestData.getListOfChats();
    private static final ChatPost post = JobChatTestData.getChatPost();
    private static final JobChatUserInfo info = JobChatTestData.getJobChatUserInfo();


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
    CandidateService candidateService;
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

        given(chatService.getOrCreateJobChat(
            nullable(JobChatType.class), nullable(SalesforceJobOpp.class), nullable(PartnerImpl.class),
            nullable(Candidate.class)))
            .willReturn(chat);

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
            .andExpect(jsonPath("$.id", is(chat.getId().intValue())));

        verify(chatService).getOrCreateJobChat(
            nullable(JobChatType.class), nullable(SalesforceJobOpp.class), nullable(PartnerImpl.class),
            nullable(Candidate.class)
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
            nullable(Candidate.class)))
            .willReturn(chat);

        mockMvc.perform(post(BASE_PATH + GET_OR_CREATE_PATH)
                .header("Authorization", "Bearer " + "jwt-token")
                .with(csrf())
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
            nullable(Candidate.class)
        );
    }

    @Test
    void markAsReadUpto() throws Exception {

        given(chatService.getJobChat(anyLong()))
            .willReturn(chat);

        given(chatPostService.getLastChatPost(anyLong()))
            .willReturn(post);

        given(chatPostService.getChatPost(anyLong()))
            .willReturn(post);

        given(userService.getLoggedInUser())
            .willReturn(user);


        //For moment just testing that post id is passed as zero - meaning read whole post
        mockMvc.perform(put(BASE_PATH + "/" + chat.getId() + POST + "/0" + READ)
                .header("Authorization", "Bearer " + "jwt-token")
                .with(csrf())
                )

            .andDo(print())
            .andExpect(status().isOk());


        verify(jobChatUserService).markChatAsRead(chat, user, post);
    }

    @Test
    void getJobChatUserInfo() throws Exception {

        given(chatService.getJobChat(anyLong()))
            .willReturn(chat);

        given(jobChatUserService.getJobChatUserInfo(any(JobChat.class), any(User.class)))
            .willReturn(info);

        given(userService.getUser(anyLong()))
            .willReturn(user);


        mockMvc.perform(get(
            BASE_PATH + "/" + chat.getId() + USER + "/123" + GET_CHAT_USER_INFO)
                .header("Authorization", "Bearer " + "jwt-token")
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.lastReadPostId", is(100)))
            .andExpect(jsonPath("$.lastPostId", is(123)));


        verify(jobChatUserService).getJobChatUserInfo(chat, user);
    }
}
