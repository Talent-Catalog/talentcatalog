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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.task.CreateTaskAssignmentRequest;
import org.tctalent.server.request.task.TaskListRequest;
import org.tctalent.server.request.task.UpdateTaskAssignmentRequestAdmin;
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

  private static final long TASK_ASSIGNMENT_ID = 99L;
  private static final String BASE_PATH = "/api/admin/task-assignment";
  private static final String SEARCH_PATH = "/search";
  private static final String ASSIGN_TO_LIST_PATH = "/assign-to-list";
  private static final String REMOVE_FROM_LIST_PATH = "/remove-from-list";

  private static final TaskImpl task = AdminApiTestUtil.getTask();
  private static final Candidate candidate = AdminApiTestUtil.getCandidate();
  private static final TaskAssignmentImpl taskAssignment = AdminApiTestUtil.getTaskAssignment();
  private static final SavedList savedList = AdminApiTestUtil.getSavedList();
  private static final TaskAssignmentImpl completedTaskAssignment = AdminApiTestUtil.getCompletedTaskAssignment();
  private static final List<TaskAssignmentImpl> taskAssignments = AdminApiTestUtil.getTaskAssignments();

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
            .with(csrf())
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
        .andExpect(jsonPath("$.[0].task.docLink", is("http://help.link")))
        .andExpect(jsonPath("$.[0].task.taskType", is("Simple")))
        .andExpect(jsonPath("$.[0].task.displayName", is("task display name")))
        .andExpect(jsonPath("$.[0].task.name", is("a test task")))
        .andExpect(jsonPath("$.[0].task.description", is("a test task description")))
        .andExpect(jsonPath("$.[0].task.optional", is(false)))
        .andExpect(jsonPath("$.[0].task.daysToComplete", is(7)))
        .andExpect(jsonPath("$[0].status", is("active")))
        .andExpect(jsonPath("$[0].dueDate", is("2025-01-01")))
        .andExpect(jsonPath("$[0].completedDate", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$[0].abandonedDate", is("2022-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$[0].candidateNotes", is("These are candidate notes.")));

    verify(taskAssignmentService).listTaskAssignments(any(TaskListRequest.class));
  }

  @Test
  @DisplayName("create task assignment succeeds")
  void createTaskAssignmentSucceeds() throws Exception {
    CreateTaskAssignmentRequest request = new CreateTaskAssignmentRequest();

    given(authService.getLoggedInUser()).willReturn(Optional.of(user));
    given(taskService.get(anyLong())).willReturn(task);
    given(candidateService.getCandidate(anyLong())).willReturn(candidate);
    given(taskAssignmentService.assignTaskToCandidate(user, task, candidate, null, request.getDueDate()))
        .willReturn(taskAssignment);

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
        .andExpect(jsonPath("$.task.id", is(148)))
        .andExpect(jsonPath("$.task.docLink", is("http://help.link")))
        .andExpect(jsonPath("$.task.taskType", is("Simple")))
        .andExpect(jsonPath("$.task.displayName", is("task display name")))
        .andExpect(jsonPath("$.task.name", is("a test task")))
        .andExpect(jsonPath("$.task.description", is("a test task description")))
        .andExpect(jsonPath("$.task.optional", is(false)))
        .andExpect(jsonPath("$.task.daysToComplete", is(7)))
        .andExpect(jsonPath("$.status", is("active")))
        .andExpect(jsonPath("$.dueDate", is("2025-01-01")));

    verify(authService).getLoggedInUser();
    verify(taskAssignmentService).assignTaskToCandidate(user, task, candidate, null, request.getDueDate());
  }

  @Test
  @DisplayName("update task assignment succeeds")
  void updateTaskAssignmentSucceeds() throws Exception {
    UpdateTaskAssignmentRequestAdmin request = new UpdateTaskAssignmentRequestAdmin();
    request.setCompleted(true);
    request.setCandidateNotes("These are candidate notes.");

    given(taskAssignmentService.get(TASK_ASSIGNMENT_ID)).willReturn(taskAssignment);

    given(taskAssignmentService
        .update(taskAssignment, request.isCompleted(), request.isAbandoned(), request.getCandidateNotes(), request.getDueDate()))
        .willReturn(completedTaskAssignment);

    mockMvc.perform(put(BASE_PATH + "/" + TASK_ASSIGNMENT_ID)
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
        .andExpect(jsonPath("$.task.id", is(148)))
        .andExpect(jsonPath("$.task.docLink", is("http://help.link")))
        .andExpect(jsonPath("$.task.taskType", is("Simple")))
        .andExpect(jsonPath("$.task.displayName", is("task display name")))
        .andExpect(jsonPath("$.task.name", is("a test task")))
        .andExpect(jsonPath("$.task.description", is("a test task description")))
        .andExpect(jsonPath("$.task.optional", is(false)))
        .andExpect(jsonPath("$.task.daysToComplete", is(7)))
        .andExpect(jsonPath("$.status", is("active")))
        .andExpect(jsonPath("$.dueDate", is("2025-01-01")))
        .andExpect(jsonPath("$.completedDate", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.abandonedDate", is("2022-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.candidateNotes", is("These are candidate notes.")));

    verify(taskAssignmentService).get(anyLong());
    verify(taskAssignmentService).update(
            taskAssignment, request.isCompleted(), request.isAbandoned(), request.getCandidateNotes(), request.getDueDate());
  }

  @Test
  @DisplayName("delete task assignment by id succeeds")
  void deleteTaskAssignmentByIdSucceeds() throws Exception {
    given(authService.getLoggedInUser()).willReturn(Optional.of(user));
    given(taskAssignmentService
        .deleteTaskAssignment(any(User.class), anyLong()))
        .willReturn(true);

    mockMvc.perform(delete(BASE_PATH + "/" + TASK_ASSIGNMENT_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", is(true)));

    verify(taskAssignmentService).deleteTaskAssignment(user, TASK_ASSIGNMENT_ID);
  }

  @Test
  @DisplayName("assign task to list succeeds")
  void assignTaskToListSucceeds() throws Exception {
    TaskListRequest request = new TaskListRequest();

    given(authService.getLoggedInUser()).willReturn(Optional.of(user));
    given(taskService.get(anyLong())).willReturn(task);
    given(savedListService.get(anyLong())).willReturn(savedList);

    mockMvc.perform(put(BASE_PATH + ASSIGN_TO_LIST_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk());

    verify(taskService).get(anyLong());
    verify(savedListService).get(anyLong());
    verify(savedListService).associateTaskWithList(user, task, savedList);
  }

  @Test
  @DisplayName("remove task from list succeeds")
  void removeTaskFromListSucceeds() throws Exception {
    TaskListRequest request = new TaskListRequest();

    given(authService.getLoggedInUser()).willReturn(Optional.of(user));
    given(taskService.get(anyLong())).willReturn(task);
    given(savedListService.get(anyLong())).willReturn(savedList);

    mockMvc.perform(put(BASE_PATH + REMOVE_FROM_LIST_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk());

    verify(taskService).get(anyLong());
    verify(savedListService).get(anyLong());
    verify(savedListService).deassociateTaskFromList(user, task, savedList);
  }



}
