/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.task.CreateTaskAssignmentRequest;
import org.tctalent.server.request.task.TaskListRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskService;

/**
 * Unit tests for Task Assignment Admin Api endpoints.
 *
 * @author Caroline Cameorn
 */
@WebMvcTest(TaskAssignmentAdminApi.class)
@AutoConfigureMockMvc
class TaskAssignmentAdminApiTest extends ApiTestBase {

  private static final long TASK_ASSIGNMENT_ID = 465L;

  private static final String BASE_PATH = "/api/admin/task-assignment";
  private static final String SEARCH_PATH = "/search";
  private static final String DESTINATIONS_LIST_PATH = "/destinations";
  private static final String SEARCH_PAGED_PATH = "/search-paged";

  private static final TaskImpl task = AdminApiTestUtil.getTask();
  private static final TaskAssignmentImpl taskAssignment = AdminApiTestUtil.getTaskAssignment();
  private static final List<TaskAssignmentImpl> taskAssignments = AdminApiTestUtil.getTaskAssignments();

  private final Page<TaskAssignmentImpl> taskAssignmentPage =
      new PageImpl<>(
          taskAssignments,
          PageRequest.of(0,10, Sort.unsorted()),
          1
      );

  @MockBean AuthService authService;
  @MockBean CandidateService candidateService;
  @MockBean SavedListService savedListService;
  @MockBean TaskAssignmentService taskAssignmentService;
  @MockBean TaskService taskService;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired TaskAssignmentAdminApi taskAssignmentAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(taskAssignmentAdminApi).isNotNull();
  }

  @Test
  @DisplayName("search task assignments succeeds")
  void searchTaskAssignmentsSucceeds() throws Exception {
    TaskListRequest request = new TaskListRequest();

    given(taskAssignmentService
        .listTaskAssignments(any(TaskListRequest.class)))
        .willReturn(taskAssignments);

    mockMvc.perform(post(BASE_PATH + SEARCH_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id", is(99)))
        .andExpect(jsonPath("$.[0].task.id", is(148)))
        .andExpect(jsonPath("$.[0].task.helpLink", is("http://help.link")))
        .andExpect(jsonPath("$.[0].task.taskType", is("Simple")))
        .andExpect(jsonPath("$.[0].task.displayName", is("task display name")))
        .andExpect(jsonPath("$.[0].task.name", is("a test task")))
        .andExpect(jsonPath("$.[0].task.description", is("a test task description")))
        .andExpect(jsonPath("$.[0].task.optional", is(false)))
        .andExpect(jsonPath("$.[0].task.daysToComplete", is(7)))
        .andExpect(jsonPath("$[0].status", is("active")))
        .andExpect(jsonPath("$[0].dueDate", is("2025-01-01")))
        .andExpect(jsonPath("$[0].completedDate", is("active")))
        .andExpect(jsonPath("$[0].abandonedDate", is("Palestine")))
        .andExpect(jsonPath("$[0].candidateNotes", is("These are candidate notes.")));

    verify(taskAssignmentService).listTaskAssignments(any(TaskListRequest.class));
  }

//  @Test
//  @DisplayName("get restricted countries succeeds")
//  void getRestrictedCountriesSucceeds() throws Exception {
//    given(taskAssignmentService
//        .listCountries(true))
//        .willReturn(countries);
//
//    mockMvc.perform(get(BASE_PATH + "/" + RESTRICTED_LIST_PATH)
//            .header("Authorization", "Bearer " + "jwt-token")
//            .contentType(MediaType.APPLICATION_JSON))
//
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$", notNullValue()))
//        .andExpect(jsonPath("$").isArray())
//        .andExpect(jsonPath("$", hasSize(3)))
//        .andExpect(jsonPath("$[0].name", is("Jordan")))
//        .andExpect(jsonPath("$[0].status", is("active")))
//        .andExpect(jsonPath("$[1].name", is("Pakistan")))
//        .andExpect(jsonPath("$[1].status", is("active")))
//        .andExpect(jsonPath("$[2].name", is("Palestine")))
//        .andExpect(jsonPath("$[2].status", is("active")));
//
//    verify(taskAssignmentService).listCountries(true);
//  }
//
//  @Test
//  @DisplayName("get destination countries succeeds")
//  void getDestinationCountriesSucceeds() throws Exception {
//    given(taskAssignmentService
//        .getTBBDestinations())
//        .willReturn(countries);
//
//    mockMvc.perform(get(BASE_PATH + "/" + DESTINATIONS_LIST_PATH)
//            .header("Authorization", "Bearer " + "jwt-token")
//            .contentType(MediaType.APPLICATION_JSON))
//
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$", notNullValue()))
//        .andExpect(jsonPath("$").isArray())
//        .andExpect(jsonPath("$", hasSize(3)))
//        .andExpect(jsonPath("$[0].name", is("Jordan")))
//        .andExpect(jsonPath("$[0].status", is("active")))
//        .andExpect(jsonPath("$[1].name", is("Pakistan")))
//        .andExpect(jsonPath("$[1].status", is("active")))
//        .andExpect(jsonPath("$[2].name", is("Palestine")))
//        .andExpect(jsonPath("$[2].status", is("active")));
//
//
//    verify(taskAssignmentService).getTBBDestinations();
//  }
//
//  @Test
//  @DisplayName("search paged countries succeeds")
//  void searchPagedCountriesSucceeds() throws Exception {
//    SearchTaskAssignmentRequest request = new SearchTaskAssignmentRequest();
//
//    given(taskAssignmentService
//        .searchCountries(any(SearchTaskAssignmentRequest.class)))
//        .willReturn(taskAssignmentPage);
//
//    mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH)
//            .header("Authorization", "Bearer " + "jwt-token")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request))
//            .accept(MediaType.APPLICATION_JSON))
//
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$.totalElements", is(3)))
//        .andExpect(jsonPath("$.totalPages", is(1)))
//        .andExpect(jsonPath("$.number", is(0)))
//        .andExpect(jsonPath("$.hasNext", is(false)))
//        .andExpect(jsonPath("$.hasPrevious", is(false)))
//        .andExpect(jsonPath("$.content", notNullValue()))
//        .andExpect(jsonPath("$.content.[0].name", is("Jordan")))
//        .andExpect(jsonPath("$.content.[0].status", is("active")))
//        .andExpect(jsonPath("$.content.[1].name", is("Pakistan")))
//        .andExpect(jsonPath("$.content.[1].status", is("active")))
//        .andExpect(jsonPath("$.content.[2].name", is("Palestine")))
//        .andExpect(jsonPath("$.content.[2].status", is("active")));
//
//    verify(taskAssignmentService).searchCountries(any(SearchTaskAssignmentRequest.class));
//  }
//
//  @Test
//  @DisplayName("get taskAssignmentImpl by id succeeds")
//  void getTaskAssignmentByIdSucceeds() throws Exception {
//
//    given(taskAssignmentService
//        .getTaskAssignment(TASKASSIGNMENT_ID))
//        .willReturn(new TaskAssignmentImpl("Ukraine", Status.active));
//
//    mockMvc.perform(get(BASE_PATH + "/" + TASKASSIGNMENT_ID)
//            .header("Authorization", "Bearer " + "jwt-token")
//            .accept(MediaType.APPLICATION_JSON))
//
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$", notNullValue()))
//        .andExpect(jsonPath("$.name", is("Ukraine")))
//        .andExpect(jsonPath("$.status", is("active")));
//
//    verify(taskAssignmentService).getTaskAssignment(TASKASSIGNMENT_ID);
//  }
//
  @Test
  @DisplayName("create task assignment succeeds")
  void createTaskAssignmentSucceeds() throws Exception {
    CreateTaskAssignmentRequest request = new CreateTaskAssignmentRequest();

    given(taskAssignmentService
        .assignTaskToCandidate(
            any(User.class),
            any(TaskImpl.class),
            any(Candidate.class),
            any(SavedList.class),
            any(LocalDate.class)))
        .willReturn(taskAssignment);

    mockMvc.perform(post(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.id", is(99)))
        .andExpect(jsonPath("$.task.id", is(148)))
        .andExpect(jsonPath("$.task.helpLink", is("http://help.link")))
        .andExpect(jsonPath("$.task.taskType", is("Simple")))
        .andExpect(jsonPath("$.task.displayName", is("task display name")))
        .andExpect(jsonPath("$.task.name", is("a test task")))
        .andExpect(jsonPath("$.task.description", is("a test task description")))
        .andExpect(jsonPath("$.task.optional", is(false)))
        .andExpect(jsonPath("$.task.daysToComplete", is(7)))
        .andExpect(jsonPath("$.status", is("active")))
        .andExpect(jsonPath("$.dueDate", is("2025-01-01")))
        .andExpect(jsonPath("$.completedDate", is("active")))
        .andExpect(jsonPath("$.abandonedDate", is("Palestine")))
        .andExpect(jsonPath("$.candidateNotes", is("These are candidate notes.")));

    verify(taskAssignmentService).assignTaskToCandidate(
        any(User.class),
        any(TaskImpl.class),
        any(Candidate.class),
        any(SavedList.class),
        any(LocalDate.class));
  }
//
//  @Test
//  @DisplayName("update taskAssignmentImpl succeeds")
//  void updateTaskAssignmentSucceeds() throws Exception {
//    UpdateTaskAssignmentRequest request = new UpdateTaskAssignmentRequest();
//    request.setName("Ukraine");
//    request.setStatus(Status.active);
//
//    given(taskAssignmentService
//        .updateTaskAssignment(anyLong(), any(UpdateTaskAssignmentRequest.class)))
//        .willReturn(new TaskAssignmentImpl("Ukraine", Status.active));
//
//    mockMvc.perform(put(BASE_PATH + "/" + TASKASSIGNMENT_ID)
//            .header("Authorization", "Bearer " + "jwt-token")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request))
//            .accept(MediaType.APPLICATION_JSON))
//
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$", notNullValue()))
//        .andExpect(jsonPath("$.name", is("Ukraine")))
//        .andExpect(jsonPath("$.status", is("active")));
//
//    verify(taskAssignmentService).updateTaskAssignment(anyLong(), any(UpdateTaskAssignmentRequest.class));
//  }
//
//  @Test
//  @DisplayName("delete taskAssignment by id succeeds")
//  void deleteTaskAssignmentByIdSucceeds() throws Exception {
//
//    given(taskAssignmentService
//        .deleteTaskAssignment(TASKASSIGNMENT_ID))
//        .willReturn(true);
//
//    mockMvc.perform(delete(BASE_PATH + "/" + TASKASSIGNMENT_ID)
//            .header("Authorization", "Bearer " + "jwt-token")
//            .accept(MediaType.APPLICATION_JSON))
//
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$", notNullValue()))
//        .andExpect(jsonPath("$", is(true)));
//
//    verify(taskAssignmentService).deleteTaskAssignment(TASKASSIGNMENT_ID);
//  }

}
