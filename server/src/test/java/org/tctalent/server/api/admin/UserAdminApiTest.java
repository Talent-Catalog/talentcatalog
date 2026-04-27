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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.UserTestData.getAuditUser;
import static org.tctalent.server.data.UserTestData.getFullUser;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.user.SearchUserRequest;
import org.tctalent.server.request.user.UpdateUserPasswordRequest;
import org.tctalent.server.request.user.UpdateUserRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.UserService;

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
  private static final String UPDATE_PASSWORD_PATH = "/password/{id}";
  private static final String MFA_RESET_PATH = "/mfa-reset/{id}";
  private static final String SEARCH_PATH = "/search";
  private static final String SEARCH_PAGED_PATH = "/search-paged";

  private static final List<User> users = List.of(getFullUser());

  private final Page<User> userPage =
      new PageImpl<>(
          users,
          PageRequest.of(0,10, Sort.unsorted()),
          1
      );

  private static final User fullUser = getFullUser();
  private static final User loggedInAdminUser = getAuditUser();
  private static final User loggedInNonAdminUser = new User(
      "nonAdminUser",
      "Not",
      "Admin",
      "notadmin@gmailcom",
      Role.limited);

  @MockBean AuthService authService;
  @MockBean CountryService countryService;
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
            .andExpect(jsonPath("$[0].partner.jobCreator", is(false)))
            .andExpect(jsonPath("$[0].partner.defaultJobCreator", is(false)))
            .andExpect(jsonPath("$[0].partner.defaultPartnerRef", is(false)))
            .andExpect(jsonPath("$[0].partner.defaultSourcePartner", is(false)))
            .andExpect(jsonPath("$[0].partner.registrationLandingPage", is("www.registration.com")))
            .andExpect(jsonPath("$[0].partner.sourceCountries[*].name", containsInAnyOrder("Jordan", "Lebanon")))
            .andExpect(jsonPath("$[0].partner.notificationEmail", is("notification@email.address")))
            .andExpect(jsonPath("$[0].partner.sourcePartner", is(true)))
            .andExpect(jsonPath("$[0].partner.autoAssignable", is(false)))
            .andExpect(jsonPath("$[0].partner.websiteUrl", is("www.website.com")))
            .andExpect(jsonPath("$[0].partner.name", is("TC Partner")))
            .andExpect(jsonPath("$[0].partner.logo", is("www.logo.com")))
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
            .andExpect(jsonPath("$.content.[0].partner.jobCreator", is(false)))
            .andExpect(jsonPath("$.content.[0].partner.defaultJobCreator", is(false)))
            .andExpect(jsonPath("$.content.[0].partner.defaultPartnerRef", is(false)))
            .andExpect(jsonPath("$.content.[0].partner.defaultSourcePartner", is(false)))
            .andExpect(jsonPath("$.content.[0].partner.registrationLandingPage", is("www.registration.com")))
            .andExpect(jsonPath("$.content.[0].partner.sourceCountries[*].name", containsInAnyOrder("Jordan", "Lebanon")))
            .andExpect(jsonPath("$.content.[0].partner.notificationEmail", is("notification@email.address")))
            .andExpect(jsonPath("$.content.[0].partner.sourcePartner", is(true)))
            .andExpect(jsonPath("$.content.[0].partner.autoAssignable", is(false)))
            .andExpect(jsonPath("$.content.[0].partner.websiteUrl", is("www.website.com")))
            .andExpect(jsonPath("$.content.[0].partner.name", is("TC Partner")))
            .andExpect(jsonPath("$.content.[0].partner.logo", is("www.logo.com")))
            .andExpect(jsonPath("$.content.[0].partner.status", is("active")));

    verify(userService).searchPaged(any(SearchUserRequest.class));
  }

  @Test
  @DisplayName("get user by id succeeds")
  void getUserByIdAdminRoleSucceeds() throws Exception {

    given(userService.getUser(USER_ID)).willReturn(fullUser);
    // The user returned here has an Admin role. See user object creation.
    given(authService.getLoggedInUser()).willReturn(Optional.of(loggedInAdminUser));

    mockMvc.perform(get(BASE_PATH + "/" + USER_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.username", is("full_user")))
        .andExpect(jsonPath("$.firstName", is("full")))
        .andExpect(jsonPath("$.lastName", is("user")))
        .andExpect(jsonPath("$.email", is("full.user@tbb.org")))
        .andExpect(jsonPath("$.role", is("admin")))
        .andExpect(jsonPath("$.jobCreator", is(true)))
        .andExpect(jsonPath("$.approver.firstName", is("test")))
        .andExpect(jsonPath("$.approver.lastName", is("user")))
        .andExpect(jsonPath("$.purpose", is("Complete intakes")))
        .andExpect(jsonPath("$.sourceCountries.[0].name", is("Jordan")))
        .andExpect(jsonPath("$.readOnly", is(false)))
        .andExpect(jsonPath("$.status", is("active")))
        .andExpect(jsonPath("$.createdDate", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.createdBy.role", is("admin")))
        .andExpect(jsonPath("$.createdBy.status", is("active")))
        .andExpect(jsonPath("$.createdBy.usingMfa", is(false)))
        .andExpect(jsonPath("$.createdBy.mfaConfigured", is(false)))
        .andExpect(jsonPath("$.lastLogin", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.usingMfa", is(true)))
        .andExpect(jsonPath("$.mfaConfigured", is(false)))
        .andExpect(jsonPath("$.partner.abbreviation", is("TCP")))
        .andExpect(jsonPath("$.partner.jobCreator", is(false)))
        .andExpect(jsonPath("$.partner.defaultJobCreator", is(false)))
        .andExpect(jsonPath("$.partner.defaultPartnerRef", is(false)))
        .andExpect(jsonPath("$.partner.defaultSourcePartner", is(false)))
        .andExpect(jsonPath("$.partner.registrationLandingPage", is("www.registration.com")))
        .andExpect(jsonPath("$.partner.sourceCountries[*].name", containsInAnyOrder("Jordan", "Lebanon")))
        .andExpect(jsonPath("$.partner.notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.partner.sourcePartner", is(true)))
        .andExpect(jsonPath("$.partner.autoAssignable", is(false)))
        .andExpect(jsonPath("$.partner.websiteUrl", is("www.website.com")))
        .andExpect(jsonPath("$.partner.name", is("TC Partner")))
        .andExpect(jsonPath("$.partner.logo", is("www.logo.com")))
        .andExpect(jsonPath("$.partner.status", is("active")));

    verify(userService).getUser(USER_ID);
    verify(authService).getLoggedInUser();
  }

  @Test
  @DisplayName("get user by id non admin role succeeds")
  void getUserByIdNonAdminRoleSucceeds() throws Exception {

    given(userService.getUser(USER_ID)).willReturn(fullUser);
    // The user returned here has a limited role.
    given(authService.getLoggedInUser()).willReturn(Optional.of(loggedInNonAdminUser));

    mockMvc.perform(get(BASE_PATH + "/" + USER_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.role", is("admin")))
        .andExpect(jsonPath("$.status", is("active")))
        .andExpect(jsonPath("$.createdDate", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.lastLogin", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.usingMfa", is(true)))
        .andExpect(jsonPath("$.mfaConfigured", is(false)));

    verify(userService).getUser(USER_ID);
    verify(authService).getLoggedInUser();
  }

  @Test
  @DisplayName("create user succeeds")
  void createUserSucceeds() throws Exception {
    UpdateUserRequest request = new UpdateUserRequest();

    given(userService
        .createUser(any(UpdateUserRequest.class)))
        .willReturn(fullUser);

    mockMvc.perform(post(BASE_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.username", is("full_user")))
        .andExpect(jsonPath("$.firstName", is("full")))
        .andExpect(jsonPath("$.lastName", is("user")))
        .andExpect(jsonPath("$.email", is("full.user@tbb.org")))
        .andExpect(jsonPath("$.role", is("admin")))
        .andExpect(jsonPath("$.jobCreator", is(true)))
        .andExpect(jsonPath("$.approver.firstName", is("test")))
        .andExpect(jsonPath("$.approver.lastName", is("user")))
        .andExpect(jsonPath("$.purpose", is("Complete intakes")))
        .andExpect(jsonPath("$.sourceCountries.[0].name", is("Jordan")))
        .andExpect(jsonPath("$.readOnly", is(false)))
        .andExpect(jsonPath("$.status", is("active")))
        .andExpect(jsonPath("$.createdDate", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.createdBy.role", is("admin")))
        .andExpect(jsonPath("$.createdBy.status", is("active")))
        .andExpect(jsonPath("$.createdBy.usingMfa", is(false)))
        .andExpect(jsonPath("$.createdBy.mfaConfigured", is(false)))
        .andExpect(jsonPath("$.lastLogin", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.usingMfa", is(true)))
        .andExpect(jsonPath("$.mfaConfigured", is(false)))
        .andExpect(jsonPath("$.partner.abbreviation", is("TCP")))
        .andExpect(jsonPath("$.partner.jobCreator", is(false)))
        .andExpect(jsonPath("$.partner.defaultJobCreator", is(false)))
        .andExpect(jsonPath("$.partner.defaultPartnerRef", is(false)))
        .andExpect(jsonPath("$.partner.defaultSourcePartner", is(false)))
        .andExpect(jsonPath("$.partner.registrationLandingPage", is("www.registration.com")))
        .andExpect(jsonPath("$.partner.sourceCountries[*].name", containsInAnyOrder("Jordan", "Lebanon")))
        .andExpect(jsonPath("$.partner.notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.partner.sourcePartner", is(true)))
        .andExpect(jsonPath("$.partner.autoAssignable", is(false)))
        .andExpect(jsonPath("$.partner.websiteUrl", is("www.website.com")))
        .andExpect(jsonPath("$.partner.name", is("TC Partner")))
        .andExpect(jsonPath("$.partner.logo", is("www.logo.com")))
        .andExpect(jsonPath("$.partner.status", is("active")));

    verify(userService).createUser(any(UpdateUserRequest.class));
  }

  @Test
  @DisplayName("update user succeeds")
  void updateUserSucceeds() throws Exception {
    UpdateUserRequest request = new UpdateUserRequest();

    given(userService
        .updateUser(anyLong(), any(UpdateUserRequest.class)))
        .willReturn(fullUser);

    mockMvc.perform(put(BASE_PATH + "/" + USER_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.username", is("full_user")))
        .andExpect(jsonPath("$.firstName", is("full")))
        .andExpect(jsonPath("$.lastName", is("user")))
        .andExpect(jsonPath("$.email", is("full.user@tbb.org")))
        .andExpect(jsonPath("$.role", is("admin")))
        .andExpect(jsonPath("$.jobCreator", is(true)))
        .andExpect(jsonPath("$.approver.firstName", is("test")))
        .andExpect(jsonPath("$.approver.lastName", is("user")))
        .andExpect(jsonPath("$.purpose", is("Complete intakes")))
        .andExpect(jsonPath("$.sourceCountries.[0].name", is("Jordan")))
        .andExpect(jsonPath("$.readOnly", is(false)))
        .andExpect(jsonPath("$.status", is("active")))
        .andExpect(jsonPath("$.createdDate", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.createdBy.role", is("admin")))
        .andExpect(jsonPath("$.createdBy.status", is("active")))
        .andExpect(jsonPath("$.createdBy.usingMfa", is(false)))
        .andExpect(jsonPath("$.createdBy.mfaConfigured", is(false)))
        .andExpect(jsonPath("$.lastLogin", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.usingMfa", is(true)))
        .andExpect(jsonPath("$.mfaConfigured", is(false)))
        .andExpect(jsonPath("$.partner.abbreviation", is("TCP")))
        .andExpect(jsonPath("$.partner.jobCreator", is(false)))
        .andExpect(jsonPath("$.partner.defaultJobCreator", is(false)))
        .andExpect(jsonPath("$.partner.defaultPartnerRef", is(false)))
        .andExpect(jsonPath("$.partner.defaultSourcePartner", is(false)))
        .andExpect(jsonPath("$.partner.registrationLandingPage", is("www.registration.com")))
        .andExpect(jsonPath("$.partner.sourceCountries[*].name", containsInAnyOrder("Jordan", "Lebanon")))
        .andExpect(jsonPath("$.partner.notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.partner.sourcePartner", is(true)))
        .andExpect(jsonPath("$.partner.autoAssignable", is(false)))
        .andExpect(jsonPath("$.partner.websiteUrl", is("www.website.com")))
        .andExpect(jsonPath("$.partner.name", is("TC Partner")))
        .andExpect(jsonPath("$.partner.logo", is("www.logo.com")))
        .andExpect(jsonPath("$.partner.status", is("active")));

    verify(userService).updateUser(anyLong(), any(UpdateUserRequest.class));
  }

  @Test
  @DisplayName("update password succeeds")
  void updatePasswordSucceeds() throws Exception {
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest();

    mockMvc.perform(put(BASE_PATH + "/" + UPDATE_PASSWORD_PATH
            .replace("{id}", Long.toString(USER_ID)))
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andExpect(status().isOk());

    verify(userService).updateUserPassword(anyLong(), any(UpdateUserPasswordRequest.class));
  }

  @Test
  @DisplayName("update mfa reset succeeds")
  void updateMfaReset() throws Exception {

    mockMvc.perform(put(BASE_PATH + "/" + MFA_RESET_PATH
            .replace("{id}", Long.toString(USER_ID)))
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andExpect(status().isOk());

    verify(userService).mfaReset(USER_ID);
  }

  @Test
  @DisplayName("delete user by id succeeds")
  void deleteUserByIdSucceeds() throws Exception {

    mockMvc.perform(delete(BASE_PATH + "/" + USER_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .with(csrf()))

        .andExpect(status().isOk());

    verify(userService).deleteUser(USER_ID);
  }

}
