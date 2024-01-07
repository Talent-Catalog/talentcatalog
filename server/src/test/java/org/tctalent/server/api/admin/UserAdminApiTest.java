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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.user.SearchUserRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for User Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(UserAdminApi.class)
@AutoConfigureMockMvc
class UserAdminApiTest extends ApiTestBase {

  private static final long USER_ID = 465L;

  private static final String BASE_PATH = "/api/admin/user";
  private static final String RESTRICTED_LIST_PATH = "/restricted";
  private static final String DESTINATIONS_LIST_PATH = "/destinations";
  private static final String SEARCH_PATH = "/search";
  private static final String SEARCH_PAGED_PATH = "/search-paged";

  private static final List<User> users = List.of(AdminApiTestUtil.getFullUser());

  private final Page<User> userPage =
      new PageImpl<>(
          users,
          PageRequest.of(0,10, Sort.unsorted()),
          1
      );

  @MockBean AuthService authService;
  @MockBean UserService userService;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired UserAdminApi userAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(userAdminApi).isNotNull();
  }

  @Test
  @DisplayName("search users succeeds")
  void searchUsersSucceeds() throws Exception {
    SearchUserRequest request = new SearchUserRequest();
    given(userService
            .search(any(SearchUserRequest.class)))
            .willReturn(users);

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
            .andExpect(jsonPath("$[0].username", is("full_user")))
            .andExpect(jsonPath("$[0].firstName", is("full")))
            .andExpect(jsonPath("$[0].lastName", is("user")))
            .andExpect(jsonPath("$[0].email", is("full.user@tbb.org")))
            .andExpect(jsonPath("$[0].role", is("admin")))
            .andExpect(jsonPath("$[0].jobCreator", is(true)))
            .andExpect(jsonPath("$[0].approver.firstName", is("test")))
            .andExpect(jsonPath("$[0].approver.lastName", is("user")))
            .andExpect(jsonPath("$[0].purpose", is("Complete intakes")))
            .andExpect(jsonPath("$[0].sourceCountries.[0].name", is("Jordan")))
            .andExpect(jsonPath("$[0].readOnly", is(false)))
            .andExpect(jsonPath("$[0].status", is("active")))
            .andExpect(jsonPath("$[0].createdDate", is("2023-10-30T12:30:00+02:00")))
            .andExpect(jsonPath("$[0].createdBy.role", is("admin")))
            .andExpect(jsonPath("$[0].createdBy.status", is("active")))
            .andExpect(jsonPath("$[0].createdBy.usingMfa", is(false)))
            .andExpect(jsonPath("$[0].createdBy.mfaConfigured", is(false)))
            .andExpect(jsonPath("$[0].lastLogin", is("2023-10-30T12:30:00+02:00")))
            .andExpect(jsonPath("$[0].usingMfa", is(true)))
            .andExpect(jsonPath("$[0].mfaConfigured", is(false)))
            .andExpect(jsonPath("$[0].partner.abbreviation", is("TCP")))
            .andExpect(jsonPath("$[0].partner.jobCreator", is(true)))
            .andExpect(jsonPath("$[0].partner.defaultJobCreator", is(false)))
            .andExpect(jsonPath("$[0].partner.defaultPartnerRef", is(false)))
            .andExpect(jsonPath("$[0].partner.defaultSourcePartner", is(false)))
            .andExpect(jsonPath("$[0].partner.registrationLandingPage", is("registration_landing_page")))
            .andExpect(jsonPath("$[0].partner.sourceCountries", is(empty())))
            .andExpect(jsonPath("$[0].partner.notificationEmail", is("notification@email.address")))
            .andExpect(jsonPath("$[0].partner.sourcePartner", is(true)))
            .andExpect(jsonPath("$[0].partner.autoAssignable", is(false)))
            .andExpect(jsonPath("$[0].partner.websiteUrl", is("website_url")))
            .andExpect(jsonPath("$[0].partner.name", is("TC Partner")))
            .andExpect(jsonPath("$[0].partner.logo", is("logo_url")))
            .andExpect(jsonPath("$[0].partner.status", is("active")));

    verify(userService).search(any(SearchUserRequest.class));
  }

  @Test
  @DisplayName("search paged users succeeds")
  void searchPagedUsersSucceeds() throws Exception {
    SearchUserRequest request = new SearchUserRequest();
    given(userService
            .searchPaged(any(SearchUserRequest.class)))
            .willReturn(userPage);

    mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH)
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
            .andExpect(jsonPath("$.content.[0].username", is("full_user")))
            .andExpect(jsonPath("$.content.[0].firstName", is("full")))
            .andExpect(jsonPath("$.content.[0].lastName", is("user")))
            .andExpect(jsonPath("$.content.[0].email", is("full.user@tbb.org")))
            .andExpect(jsonPath("$.content.[0].role", is("admin")))
            .andExpect(jsonPath("$.content.[0].jobCreator", is(true)))
            .andExpect(jsonPath("$.content.[0].approver.firstName", is("test")))
            .andExpect(jsonPath("$.content.[0].approver.lastName", is("user")))
            .andExpect(jsonPath("$.content.[0].purpose", is("Complete intakes")))
            .andExpect(jsonPath("$.content.[0].sourceCountries.[0].name", is("Jordan")))
            .andExpect(jsonPath("$.content.[0].readOnly", is(false)))
            .andExpect(jsonPath("$.content.[0].status", is("active")))
            .andExpect(jsonPath("$.content.[0].createdDate", is("2023-10-30T12:30:00+02:00")))
            .andExpect(jsonPath("$.content.[0].createdBy.role", is("admin")))
            .andExpect(jsonPath("$.content.[0].createdBy.status", is("active")))
            .andExpect(jsonPath("$.content.[0].createdBy.usingMfa", is(false)))
            .andExpect(jsonPath("$.content.[0].createdBy.mfaConfigured", is(false)))
            .andExpect(jsonPath("$.content.[0].lastLogin", is("2023-10-30T12:30:00+02:00")))
            .andExpect(jsonPath("$.content.[0].usingMfa", is(true)))
            .andExpect(jsonPath("$.content.[0].mfaConfigured", is(false)))
            .andExpect(jsonPath("$.content.[0].partner.abbreviation", is("TCP")))
            .andExpect(jsonPath("$.content.[0].partner.jobCreator", is(true)))
            .andExpect(jsonPath("$.content.[0].partner.defaultJobCreator", is(false)))
            .andExpect(jsonPath("$.content.[0].partner.defaultPartnerRef", is(false)))
            .andExpect(jsonPath("$.content.[0].partner.defaultSourcePartner", is(false)))
            .andExpect(jsonPath("$.content.[0].partner.registrationLandingPage", is("registration_landing_page")))
            .andExpect(jsonPath("$.content.[0].partner.sourceCountries", is(empty())))
            .andExpect(jsonPath("$.content.[0].partner.notificationEmail", is("notification@email.address")))
            .andExpect(jsonPath("$.content.[0].partner.sourcePartner", is(true)))
            .andExpect(jsonPath("$.content.[0].partner.autoAssignable", is(false)))
            .andExpect(jsonPath("$.content.[0].partner.websiteUrl", is("website_url")))
            .andExpect(jsonPath("$.content.[0].partner.name", is("TC Partner")))
            .andExpect(jsonPath("$.content.[0].partner.logo", is("logo_url")))
            .andExpect(jsonPath("$.content.[0].partner.status", is("active")));

    verify(userService).searchPaged(any(SearchUserRequest.class));
  }

//  @Test
//  @DisplayName("search paged countries succeeds")
//  void searchPagedCountriesSucceeds() throws Exception {
//    SearchUserRequest request = new SearchUserRequest();
//
//    given(userService
//        .searchCountries(any(SearchUserRequest.class)))
//        .willReturn(userPage);
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
//    verify(userService).searchCountries(any(SearchUserRequest.class));
//  }
//
//  @Test
//  @DisplayName("get user by id succeeds")
//  void getUserByIdSucceeds() throws Exception {
//
//    given(userService
//        .getUser(USER_ID))
//        .willReturn(new User("Ukraine", Status.active));
//
//    mockMvc.perform(get(BASE_PATH + "/" + USER_ID)
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
//    verify(userService).getUser(USER_ID);
//  }
//
//  @Test
//  @DisplayName("create user succeeds")
//  void createUserSucceeds() throws Exception {
//    UpdateUserRequest request = new UpdateUserRequest();
//    request.setName("Ukraine");
//    request.setStatus(Status.active);
//
//    given(userService
//        .createUser(any(UpdateUserRequest.class)))
//        .willReturn(new User("Ukraine", Status.active));
//
//    mockMvc.perform(post(BASE_PATH)
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
//    verify(userService).createUser(any(UpdateUserRequest.class));
//  }
//
//  @Test
//  @DisplayName("update user succeeds")
//  void updateUserSucceeds() throws Exception {
//    UpdateUserRequest request = new UpdateUserRequest();
//    request.setName("Ukraine");
//    request.setStatus(Status.active);
//
//    given(userService
//        .updateUser(anyLong(), any(UpdateUserRequest.class)))
//        .willReturn(new User("Ukraine", Status.active));
//
//    mockMvc.perform(put(BASE_PATH + "/" + USER_ID)
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
//    verify(userService).updateUser(anyLong(), any(UpdateUserRequest.class));
//  }
//
//  @Test
//  @DisplayName("delete user by id succeeds")
//  void deleteUserByIdSucceeds() throws Exception {
//
//    given(userService
//        .deleteUser(USER_ID))
//        .willReturn(true);
//
//    mockMvc.perform(delete(BASE_PATH + "/" + USER_ID)
//            .header("Authorization", "Bearer " + "jwt-token")
//            .accept(MediaType.APPLICATION_JSON))
//
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$", notNullValue()))
//        .andExpect(jsonPath("$", is(true)));
//
//    verify(userService).deleteUser(USER_ID);
//  }

}
