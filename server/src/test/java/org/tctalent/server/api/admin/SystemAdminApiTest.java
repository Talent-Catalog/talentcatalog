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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.configuration.SalesforceConfig;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.repository.db.CandidateAttachmentRepository;
import org.tctalent.server.repository.db.CandidateNoteRepository;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.ChatPostRepository;
import org.tctalent.server.repository.db.JobChatRepository;
import org.tctalent.server.repository.db.JobChatUserRepository;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.SavedSearchRepository;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.api.TcApiService;
import org.tctalent.server.service.db.BackgroundProcessingService;
import org.tctalent.server.service.db.CandidateOppBackgroundProcessingService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.DataSharingService;
import org.tctalent.server.service.db.DuolingoApiService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.JobService;
import org.tctalent.server.service.db.LanguageService;
import org.tctalent.server.service.db.NotificationService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.PopulateElasticsearchService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.aws.S3ResourceHelper;
import org.tctalent.server.service.db.cache.CacheService;

/**
 * Unit tests for System Admin Api endpoints.
 */
@WebMvcTest(SystemAdminApi.class)
@AutoConfigureMockMvc
class SystemAdminApiTest extends ApiTestBase {

  private static final String BASE_PATH = "/api/admin/system";

  @Autowired MockMvc mockMvc;
  @Autowired SystemAdminApi systemAdminApi;
  @Autowired ObjectMapper objectMapper;

  @MockBean private AuthService authService;
  @MockBean private DataSharingService dataSharingService;
  @MockBean private CandidateAttachmentRepository candidateAttachmentRepository;
  @MockBean private CandidateNoteRepository candidateNoteRepository;
  @MockBean private CandidateRepository candidateRepository;
  @MockBean private CandidateOpportunityRepository candidateOpportunityRepository;
  @MockBean private CandidateOpportunityService candidateOpportunityService;
  @MockBean private CandidateService candidateService;
  @MockBean private CountryService countryService;
  @MockBean private FileSystemService fileSystemService;
  @MockBean private JobService jobService;
  @MockBean private LanguageService languageService;
  @MockBean private NotificationService notificationService;
  @MockBean private PopulateElasticsearchService populateElasticsearchService;
  @MockBean private SalesforceService salesforceService;
  @MockBean private SalesforceConfig salesforceConfig;
  @MockBean private SavedListRepository savedListRepository;
  @MockBean private SalesforceJobOppRepository salesforceJobOppRepository;
  @MockBean private SavedListService savedListService;
  @MockBean private SavedSearchRepository savedSearchRepository;
  @MockBean private JobChatRepository jobChatRepository;
  @MockBean private JobChatUserRepository jobChatUserRepository;
  @MockBean private ChatPostRepository chatPostRepository;
  @MockBean private S3ResourceHelper s3ResourceHelper;
  @MockBean private CacheService cacheService;
  @MockBean private GoogleDriveConfig googleDriveConfig;
  @MockBean private TaskScheduler taskScheduler;
  @MockBean private BackgroundProcessingService backgroundProcessingService;
  @MockBean private SavedSearchService savedSearchService;
  @MockBean private PartnerService partnerService;
  @MockBean private CandidateOppBackgroundProcessingService candidateOppBackgroundProcessingService;
  @MockBean private TcApiService tcApiService;
  @MockBean private DuolingoApiService duolingoApiService;

  private static final PartnerImpl partner = AdminApiTestUtil.getPartner();

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(systemAdminApi).isNotNull();
  }

  @Test
  void sanityCheck_autowiredControllerDependencies() {
    assertNotNull(systemAdminApi, "Controller not injected");
    assertNotNull(partnerService, "MockBean not injected");
    assertNotNull(
        ReflectionTestUtils.getField(systemAdminApi, "partnerService"),
        "partnerService not injected into controller"
    );
  }

  @Test
  @DisplayName("")
  void reassignCandidatesWithValidListAndPartnerIdShouldReturnOk() throws Exception {
    SavedList mockList = new SavedList();
    Page<Candidate> mockPage = new PageImpl<>(List.of(new Candidate()));

    given(partnerService.getPartner(anyLong())).willReturn(partner);
    given(savedListService.get(anyInt())).willReturn(mockList);
    given(candidateService.getSavedListCandidates(any(), any())).willReturn(mockPage);

    mockMvc.perform(get(BASE_PATH + "/reassign-candidates/list-123-to-partner-456"))
        .andExpect(status().isOk());

    verify(candidateService).reassignCandidatesOnPage(any(), eq(partner));
  }

}
