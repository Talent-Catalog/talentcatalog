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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.SavedListService;

/**
 * Unit tests for Candidate Saved List Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateSavedListAdminApi.class)
@AutoConfigureMockMvc
class CandidateSavedListAdminApiTest extends ApiTestBase {

  @MockBean CandidateSavedListService candidateSavedListService;
  @MockBean SavedListService savedListService;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired CandidateSavedListAdminApi candidateSavedListAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(candidateSavedListAdminApi).isNotNull();
  }



}