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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.tctalent.server.request.job.JobInfoForSlackPost;
import org.tctalent.server.request.opportunity.PostJobToSlackRequest;
import org.tctalent.server.service.db.JobService;
import org.tctalent.server.service.db.SlackService;

/**
 * @author John Cameron
 */
@WebMvcTest(SlackAdminApi.class)
@AutoConfigureMockMvc
class SlackAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/slack";
    private static final String POST_JOB_PATH = "/post-job";
    private static final String testSlackChannelUrl = "https://slack.channel";


    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    SlackAdminApi slackAdminApi;

    @MockBean
    SlackService slackService;
    @MockBean
    JobService jobService;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    void postJobFromRequest() throws Exception {
        PostJobToSlackRequest request = new PostJobToSlackRequest();

        given(slackService.postJob(any(PostJobToSlackRequest.class)))
            .willReturn(testSlackChannelUrl);

        mockMvc.perform(post(BASE_PATH + POST_JOB_PATH)
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.slackChannelUrl", is(testSlackChannelUrl)));

        verify(slackService).postJob(any(PostJobToSlackRequest.class));
    }

    @Test
    void postJobFromJobIdAndLink() throws Exception {

        given(slackService.postJob(any(JobInfoForSlackPost.class)))
            .willReturn(testSlackChannelUrl);

        JobInfoForSlackPost testInfo = new JobInfoForSlackPost();
        given(jobService.extractJobInfoForSlack(anyLong(), anyString()))
            .willReturn(testInfo);

        String tcLink = "https://tctalent.org/admin-portal/job/123";

        mockMvc.perform(post(BASE_PATH + "/123" + POST_JOB_PATH)
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(tcLink)
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.slackChannelUrl", is(testSlackChannelUrl)));

        verify(slackService).postJob(any(JobInfoForSlackPost.class));
    }
}
