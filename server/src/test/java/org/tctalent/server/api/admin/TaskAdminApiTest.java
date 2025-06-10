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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.TaskTestData.getListOfTasks;
import static org.tctalent.server.data.TaskTestData.getTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.request.task.SearchTaskRequest;
import org.tctalent.server.request.task.UpdateTaskRequest;
import org.tctalent.server.service.db.TaskService;

/**
 * @author John Cameron
 */
@WebMvcTest(TaskAdminApi.class)
@AutoConfigureMockMvc
class TaskAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/task";
    private static final String SEARCH_PAGED_PATH = "/search-paged";

    private static final TaskImpl task = getTask();
    private final Page<TaskImpl> tasksPage =
        new PageImpl<>(
            List.of(task),
            PageRequest.of(0,10, Sort.unsorted()),
            1
        );

    private static final List<TaskImpl> taskList = getListOfTasks();

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TaskAdminApi taskAdminApi;

    @MockBean
    TaskService taskService;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    void list() throws Exception {
        given(taskService.listTasks())
            .willReturn(taskList);

        mockMvc.perform(
                get(BASE_PATH)
                    .header("Authorization", "Bearer " + "jwt-token")
                    .accept(MediaType.APPLICATION_JSON)
            )

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$.[1].name", is("test task 2")));

        verify(taskService).listTasks();
    }

    @Test
    void searchPaged() throws Exception {
        SearchTaskRequest request = new SearchTaskRequest();

        given(taskService.searchTasks(any(SearchTaskRequest.class)))
            .willReturn(tasksPage);

        mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH)
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements", is(1)))
            .andExpect(jsonPath("$.totalPages", is(1)))
            .andExpect(jsonPath("$.number", is(0)))
            .andExpect(jsonPath("$.hasNext", is(false)))
            .andExpect(jsonPath("$.hasPrevious", is(false)))
            .andExpect(jsonPath("$.content", notNullValue()))
            .andExpect(jsonPath("$.content.[0].name", is("a test task")));

        verify(taskService).searchTasks(any(SearchTaskRequest.class));

    }

    @Test
    void testGet() throws Exception {
        given(taskService.get(anyLong()))
            .willReturn(task);

        mockMvc.perform(get(BASE_PATH + "/123")
                .header("Authorization", "Bearer " + "jwt-token")
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.name", is("a test task")));

        verify(taskService).get(anyLong());
    }

    @Test
    void update() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setDisplayName("new display name");
        request.setDescription("new description");
        request.setDaysToComplete(7);
        request.setOptional(true);

        given(taskService.update(anyLong(), any(UpdateTaskRequest.class)))
            .willReturn(task);

        mockMvc.perform(put(BASE_PATH + "/123")
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.name", is("a test task")));

        verify(taskService).update(anyLong(), any(UpdateTaskRequest.class));

    }
}
