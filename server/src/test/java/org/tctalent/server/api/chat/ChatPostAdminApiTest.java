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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.api.admin.AdminApiTestUtil;
import org.tctalent.server.api.admin.ApiTestBase;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.service.db.impl.ChatPostServiceImpl;

/**
 * @author John Cameron
 */
@WebMvcTest(ChatPostAdminApi.class)
@AutoConfigureMockMvc
class ChatPostAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/chat-post";
    private static final String LIST = "/list";
    private static final String UPLOAD = "/upload";
    private static final List<ChatPost> postList = AdminApiTestUtil.getListOfPosts();

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ChatPostAdminApi chatPostAdminApi;

    @MockBean
    ChatPostServiceImpl chatPostService;

    @BeforeEach
    void setUp() {
        configureAuthentication();

        //todo Maybe this is an indication that getChatPostDtoBuilder in a utility class rather than the service
        when(chatPostService.getChatPostDtoBuilder()).thenCallRealMethod();

    }

    @Test
    void list() throws Exception {
        given(chatPostService.listChatPosts(anyLong()))
            .willReturn(postList);

        long chatId = 123;

        mockMvc.perform(get(BASE_PATH + "/" + chatId + LIST )
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$.[0].content", is("Post 1")));

        verify(chatPostService).listChatPosts(chatId);
    }

    @Test
    void upload() throws Exception {
        String testFileUrl = "https://file.link";

        //See https://www.baeldung.com/spring-multipart-post-request-test
        MockMultipartFile testFile = new MockMultipartFile(
            "file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
            "some content".getBytes()
        );

        given(chatPostService.uploadFile(anyLong(), any(MultipartFile.class)))
            .willReturn(testFileUrl);

        long postId = 123;

        mockMvc.perform(multipart(BASE_PATH + "/" + postId + UPLOAD )
                .file(testFile)
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.url", is(testFileUrl)));

        verify(chatPostService).uploadFile(anyLong(), any(MultipartFile.class));
    }
}
