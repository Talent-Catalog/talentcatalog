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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.CandidateTestData.getListOfCandidates;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDestination;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.request.candidate.CandidateEmailPhoneOrWhatsappSearchRequest;
import org.tctalent.server.request.candidate.CandidateEmailSearchRequest;
import org.tctalent.server.request.candidate.CandidateExternalIdSearchRequest;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tctalent.server.request.candidate.CandidateNumberOrNameSearchRequest;
import org.tctalent.server.request.candidate.DownloadCvRequest;
import org.tctalent.server.request.candidate.ResolveTaskAssignmentsRequest;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.UpdateCandidateAdditionalInfoRequest;
import org.tctalent.server.request.candidate.UpdateCandidateLinksRequest;
import org.tctalent.server.request.candidate.UpdateCandidateListOppsRequest;
import org.tctalent.server.request.candidate.UpdateCandidateMediaRequest;
import org.tctalent.server.request.candidate.UpdateCandidateOppsRequest;
import org.tctalent.server.request.candidate.UpdateCandidateRegistrationRequest;
import org.tctalent.server.request.candidate.UpdateCandidateRequest;
import org.tctalent.server.request.candidate.UpdateCandidateShareableDocsRequest;
import org.tctalent.server.request.candidate.UpdateCandidateShareableNotesRequest;
import org.tctalent.server.request.candidate.UpdateCandidateStatusRequest;
import org.tctalent.server.request.candidate.UpdateCandidateSurveyRequest;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.security.CandidateTokenProvider;
import org.tctalent.server.security.CvClaims;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Unit tests for Candidate Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateAdminApi.class)
@AutoConfigureMockMvc
class CandidateAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/candidate";
    private static final String SEARCH_PATH = "/search";
    private static final String FIND_BY_EMAIL_PATH = "/findbyemail";
    private static final String FIND_BY_EMAIL_PHONE_OR_WHATSAPP_PATH = "/findbyemailphoneorwhatsapp";
    private static final String FIND_BY_NUMBER_OR_NAME_PATH = "/findbynumberorname";
    private static final String FIND_BY_EXTERNAL_ID_PATH = "/findbyexternalid";
    private static final String GET_BY_NUMBER_PATH = "/number/{number}";
    private static final String GET_BY_ID_PATH = "/{id}";
    private static final String GET_INTAKE_DATA_BY_ID_PATH = "/{id}/intake";
    private static final String UPDATE_LINKS_BY_ID_PATH = "/{id}/links";
    private static final String UPDATE_STATUS_PATH = "/status";
    private static final String UPDATE_CONTACT_DETAILS_BY_ID_PATH = "/{id}";
    private static final String UPDATE_ADDITIONAL_INFO_BY_ID_PATH = "/{id}/info";
    private static final String UPDATE_SHAREABLE_NOTES_BY_ID_PATH = "/{id}/shareable-notes";
    private static final String UPDATE_SHAREABLE_DOCS_BY_ID_PATH = "/{id}/shareable-docs";
    private static final String UPDATE_SURVEY_BY_ID_PATH = "/{id}/survey";
    private static final String UPDATE_MEDIA_BY_ID_PATH = "/{id}/media";
    private static final String UPDATE_REGISTRATION_BY_ID_PATH = "/{id}/registration";
    private static final String EXPORT_CSV_PATH = "/export/csv";
    private static final String DOWNLOAD_CV_PDF_BY_ID_PATH = "/{id}/cv.pdf";
    private static final String CREATE_CANDIDATE_FOLDER_BY_ID_PATH = "/{id}/create-folder";
    private static final String UPDATE_LIVE_CANDIDATE_BY_ID_PATH = "/{id}/update-live";
    private static final String UPDATE_OPPS_FROM_CANDIDATE_PATH = "/update-opps";
    private static final String UPDATE_OPPS_FROM_CANDIDATE_LIST_PATH = "/update-opps-by-list";
    private static final String UPDATE_INTAKE_DATA_BY_ID_PATH = "/{id}/intake";
    private static final String RESOLVE_TASKS_PATH = "/resolve-tasks";
    private static final String GENERATE_TOKEN_PATH = "/token/{cn}";

    private final Page<Candidate> candidates =
            new PageImpl<>(
                    getListOfCandidates(),
                    PageRequest.of(0,10, Sort.unsorted()),
                    getListOfCandidates().size()
            );

    private final Candidate candidate = getCandidate();

    @MockBean
    CandidateService candidateService;
    @MockBean
    CandidateOpportunityService candidateOpportunityService;
    @MockBean
    CandidateSavedListService candidateSavedListService;
    @MockBean
    SavedListService savedListService;
    @MockBean
    SavedSearchService savedSearchService;
    @MockBean
    CandidateTokenProvider candidateTokenProvider;
    @MockBean
    CandidateBuilderSelector candidateBuilderSelector;
    @MockBean
    CandidateIntakeDataBuilderSelector candidateIntakeDataBuilderSelector;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateAdminApi candidateAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();

        // Minimal builder for Candidate
        DtoBuilder nationalityDto = new DtoBuilder()
            .add("name");

        DtoBuilder candidateDto = new DtoBuilder()
            .add("id")
            .add("nationality", nationalityDto);

        given(candidateBuilderSelector.selectBuilder()).willReturn(candidateDto);
        given(candidateBuilderSelector.selectBuilder(any())).willReturn(candidateDto);

        // Minimal builder for Intake
        DtoBuilder countryDto = new DtoBuilder()
            .add("name");

        DtoBuilder destinationDto = new DtoBuilder()
            .add("country", countryDto);

        DtoBuilder intakeDto = new DtoBuilder()
            .add("id")
            .add("candidateDestinations", destinationDto);

        given(candidateIntakeDataBuilderSelector.selectBuilder()).willReturn(intakeDto);
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateAdminApi).isNotNull();
    }

    @Test
    @DisplayName("search succeeds")
    void searchSucceeds() throws Exception {
        SearchCandidateRequest request = new SearchCandidateRequest();

        given(savedSearchService
                .searchCandidates(any(SearchCandidateRequest.class)))
                .willReturn(candidates);

        postSearchRequestAndVerifyResponse(SEARCH_PATH, objectMapper.writeValueAsString(request));
        verify(savedSearchService).searchCandidates(any(SearchCandidateRequest.class));
    }

    @Test
    @DisplayName("find by email succeeds")
    void findByEmailSucceeds() throws Exception {
        CandidateEmailSearchRequest request = new CandidateEmailSearchRequest();

        given(candidateService
                .searchCandidates(any(CandidateEmailSearchRequest.class)))
                .willReturn(candidates);

        postSearchRequestAndVerifyResponse(FIND_BY_EMAIL_PATH, objectMapper.writeValueAsString(request));
        verify(candidateService).searchCandidates(any(CandidateEmailSearchRequest.class));
    }

    @Test
    @DisplayName("find by email phone or whatsapp succeeds")
    void findByEmailPhoneOrWhatsappSucceeds() throws Exception {
        CandidateEmailPhoneOrWhatsappSearchRequest request = new CandidateEmailPhoneOrWhatsappSearchRequest();

        given(candidateService
                .searchCandidates(any(CandidateEmailPhoneOrWhatsappSearchRequest.class)))
                .willReturn(candidates);

        postSearchRequestAndVerifyResponse(FIND_BY_EMAIL_PHONE_OR_WHATSAPP_PATH, objectMapper.writeValueAsString(request));
        verify(candidateService).searchCandidates(any(CandidateEmailPhoneOrWhatsappSearchRequest.class));
    }

    @Test
    @DisplayName("find by number or name succeeds")
    void findByNumberOrNameSucceeds() throws Exception {
        CandidateNumberOrNameSearchRequest request = new CandidateNumberOrNameSearchRequest();

        given(candidateService
                .searchCandidates(any(CandidateNumberOrNameSearchRequest.class)))
                .willReturn(candidates);

        postSearchRequestAndVerifyResponse(FIND_BY_NUMBER_OR_NAME_PATH, objectMapper.writeValueAsString(request));
        verify(candidateService).searchCandidates(any(CandidateNumberOrNameSearchRequest.class));
    }

    @Test
    @DisplayName("find by external id succeeds")
    void findByExternalIdSucceeds() throws Exception {
        CandidateExternalIdSearchRequest request = new CandidateExternalIdSearchRequest();

        given(candidateService
                .searchCandidates(any(CandidateExternalIdSearchRequest.class)))
                .willReturn(candidates);

        postSearchRequestAndVerifyResponse(FIND_BY_EXTERNAL_ID_PATH, objectMapper.writeValueAsString(request));
        verify(candidateService).searchCandidates(any(CandidateExternalIdSearchRequest.class));
    }

    @Test
    @DisplayName("get by number succeeds")
    void getByNumberSucceeds() throws Exception {
        String number = "99";

        given(candidateService
                .findByCandidateNumberRestricted(anyString()))
                .willReturn(candidate);

        getCandidateAndVerifyResponse(GET_BY_NUMBER_PATH.replace("{number}", number));

        verify(candidateService).findByCandidateNumberRestricted(anyString());
    }

    @Test
    @DisplayName("get by id succeeds")
    void getByIdSucceeds() throws Exception {
        long id = 99L;

        given(candidateService
                .getCandidate(anyLong()))
                .willReturn(candidate);

        getCandidateAndVerifyResponse(GET_BY_ID_PATH.replace("{id}", Long.toString(id)));

        verify(candidateService).getCandidate(anyLong());
    }

    @Test
    @DisplayName("get intake data by id succeeds")
    void getIntakeDataByIdSucceeds() throws Exception {
        long id = 99L;

        given(candidateService
                .getCandidate(anyLong()))
                .willReturn(candidate);

        given(candidateService
                .addMissingDestinations(any(Candidate.class)))
                .willReturn(addMissingDestinations(candidate));

//        getIntakeDataAndVerifyResponse(GET_INTAKE_DATA_BY_ID_PATH.replace("{id}", Long.toString(id)));
      mockMvc.perform(get(BASE_PATH + GET_INTAKE_DATA_BY_ID_PATH.replace("{id}", Long.toString(id)))
              .header("Authorization", "Bearer " + "jwt-token")
              .accept(MediaType.APPLICATION_JSON))

          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.candidateDestinations").isArray())
          .andExpect(jsonPath("$.candidateDestinations", hasSize(2)))
          .andExpect(jsonPath("$.candidateDestinations[0].country.name", is("Canada")))
          .andExpect(jsonPath("$.candidateDestinations[1].country.name", is("UK")));

        verify(candidateService).getCandidate(anyLong());
    }

    @Test
    @DisplayName("update links by id succeeds")
    void updateLinksByIdSucceeds() throws Exception {
        long id = 99L;
        UpdateCandidateLinksRequest request = new UpdateCandidateLinksRequest();

        given(candidateService
                .updateCandidateLinks(anyLong(), any(UpdateCandidateLinksRequest.class)))
                .willReturn(candidate);

        updateCandidateAndVerifyResponse(
                UPDATE_LINKS_BY_ID_PATH.replace("{id}", Long.toString(id)),
                objectMapper.writeValueAsString(request));

        verify(candidateService).updateCandidateLinks(anyLong(), any(UpdateCandidateLinksRequest.class));
    }

    @Test
    @DisplayName("update status succeeds")
    void updateStatusSucceeds() throws Exception {
        UpdateCandidateStatusRequest request = new UpdateCandidateStatusRequest();

        updateCandidate(UPDATE_STATUS_PATH, objectMapper.writeValueAsString(request));

        verify(candidateService).updateCandidateStatus(any(UpdateCandidateStatusRequest.class));
    }

    @Test
    @DisplayName("update contact details by id succeeds")
    void updateContactDetailsByIdSucceeds() throws Exception {
        long id = 99L;
        UpdateCandidateRequest request = new UpdateCandidateRequest();

        given(candidateService
                .updateCandidate(anyLong(), any(UpdateCandidateRequest.class)))
                .willReturn(candidate);

        updateCandidateAndVerifyResponse(
                UPDATE_CONTACT_DETAILS_BY_ID_PATH.replace("{id}", Long.toString(id)),
                objectMapper.writeValueAsString(request));

        verify(candidateService).updateCandidate(anyLong(), any(UpdateCandidateRequest.class));
    }

    @Test
    @DisplayName("update additional info by id succeeds")
    void updateAdditionalInfoByIdSucceeds() throws Exception {
        long id = 99L;
        UpdateCandidateAdditionalInfoRequest request = new UpdateCandidateAdditionalInfoRequest();

        given(candidateService
                .updateCandidateAdditionalInfo(anyLong(), any(UpdateCandidateAdditionalInfoRequest.class)))
                .willReturn(candidate);

        updateCandidateAndVerifyResponse(
                UPDATE_ADDITIONAL_INFO_BY_ID_PATH.replace("{id}", Long.toString(id)),
                objectMapper.writeValueAsString(request));

        verify(candidateService).updateCandidateAdditionalInfo(
                anyLong(), any(UpdateCandidateAdditionalInfoRequest.class));
    }

    @Test
    @DisplayName("update shareable notes by id succeeds")
    void updateShareableNotesByIdSucceeds() throws Exception {
        long id = 99L;
        UpdateCandidateShareableNotesRequest request = new UpdateCandidateShareableNotesRequest();

        given(candidateService
                .updateShareableNotes(anyLong(), any(UpdateCandidateShareableNotesRequest.class)))
                .willReturn(candidate);

        updateCandidateAndVerifyResponse(
                UPDATE_SHAREABLE_NOTES_BY_ID_PATH.replace("{id}", Long.toString(id)),
                objectMapper.writeValueAsString(request));

        verify(candidateService).updateShareableNotes(anyLong(), any(UpdateCandidateShareableNotesRequest.class));
    }

    @Test
    @DisplayName("update shareable docs by id succeeds")
    void updateShareableDocsByIdSucceeds() throws Exception {
        long id = 99L;
        UpdateCandidateShareableDocsRequest request = new UpdateCandidateShareableDocsRequest();

        given(candidateSavedListService
                .updateShareableDocs(anyLong(), any(UpdateCandidateShareableDocsRequest.class)))
                .willReturn(candidate);

        updateCandidateAndVerifyResponse(
                UPDATE_SHAREABLE_DOCS_BY_ID_PATH.replace("{id}", Long.toString(id)),
                objectMapper.writeValueAsString(request));

        verify(candidateSavedListService).updateShareableDocs(
                anyLong(), any(UpdateCandidateShareableDocsRequest.class));
    }

    @Test
    @DisplayName("update survey by id succeeds")
    void updateSurveyByIdSucceeds() throws Exception {
        long id = 99L;
        UpdateCandidateSurveyRequest request = new UpdateCandidateSurveyRequest();

        given(candidateService
                .updateCandidateSurvey(anyLong(), any(UpdateCandidateSurveyRequest.class)))
                .willReturn(candidate);

        updateCandidateAndVerifyResponse(
                UPDATE_SURVEY_BY_ID_PATH.replace("{id}", Long.toString(id)),
                objectMapper.writeValueAsString(request));

        verify(candidateService).updateCandidateSurvey(anyLong(), any(UpdateCandidateSurveyRequest.class));
    }

    @Test
    @DisplayName("update media by id succeeds")
    void updateMediaByIdSucceeds() throws Exception {
        long id = 99L;
        UpdateCandidateMediaRequest request = new UpdateCandidateMediaRequest();

        given(candidateService
                .updateCandidateMedia(anyLong(), any(UpdateCandidateMediaRequest.class)))
                .willReturn(candidate);

        updateCandidateAndVerifyResponse(
                UPDATE_MEDIA_BY_ID_PATH.replace("{id}", Long.toString(id)),
                objectMapper.writeValueAsString(request));

        verify(candidateService).updateCandidateMedia(anyLong(), any(UpdateCandidateMediaRequest.class));
    }

    @Test
    @DisplayName("update registration by id succeeds")
    void updateRegistrationByIdSucceeds() throws Exception {
        long id = 99L;
        UpdateCandidateRegistrationRequest request = new UpdateCandidateRegistrationRequest();

        given(candidateService
                .updateCandidateRegistration(anyLong(), any(UpdateCandidateRegistrationRequest.class)))
                .willReturn(candidate);

        updateCandidateAndVerifyResponse(
                UPDATE_REGISTRATION_BY_ID_PATH.replace("{id}", Long.toString(id)),
                objectMapper.writeValueAsString(request));

        verify(candidateService).updateCandidateRegistration(anyLong(), any(UpdateCandidateRegistrationRequest.class));
    }

    @Test
    @DisplayName("delete by id returns true")
    void deleteByIdReturnsTrue() throws Exception {
        long id = 99L;

        given(candidateService
                .deleteCandidate(anyLong()))
                .willReturn(true);

        mockMvc.perform(delete(BASE_PATH + "/" + id)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(true)));

        verify(candidateService).deleteCandidate(anyLong());
    }

    @Test
    @DisplayName("delete by id returns false")
    void deleteByIdReturnsFalse() throws Exception {
        long id = 99L;

        given(candidateService
                .deleteCandidate(anyLong()))
                .willReturn(false);

        mockMvc.perform(delete(BASE_PATH + "/" + id)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(false)));

        verify(candidateService).deleteCandidate(anyLong());
    }

    @Test
    @DisplayName("export csv succeeds")
    void exportCsvSucceeds() throws Exception {
        SearchCandidateRequest request = new SearchCandidateRequest();

        postApiRequest(EXPORT_CSV_PATH, objectMapper.writeValueAsString(request));

        verify(savedSearchService).exportToCsv(any(SearchCandidateRequest.class), any(PrintWriter.class));
    }

    @Test
    @DisplayName("download candidate cv pdf succeeds")
    void downloadCandidateCvPdfSucceeds() throws Exception {
        long id = 99L;
        DownloadCvRequest request = new DownloadCvRequest();
        request.setCandidateId(id);
        request.setShowName(true);
        request.setShowContact(true);

        Resource report = new ByteArrayResource("report".getBytes());

        given(candidateService
                .getCandidate(anyLong()))
                .willReturn(candidate);

        given(candidateService
                .generateCv(any(Candidate.class), anyBoolean(), anyBoolean()))
                .willReturn(report);

        postApiRequest(
                DOWNLOAD_CV_PDF_BY_ID_PATH.replace("{id}", Long.toString(id)),
                objectMapper.writeValueAsString(request));

        verify(candidateService).getCandidate(anyLong());
        verify(candidateService).generateCv(any(Candidate.class), anyBoolean(), anyBoolean());
    }

    @Test
    @DisplayName("create candidate folder by id succeeds")
    void createCandidateFolderByIdSucceeds() throws Exception {
        long id = 99L;

        given(candidateService
                .createCandidateFolder(anyLong()))
                .willReturn(candidate);

        updateCandidateAndVerifyResponse(
                CREATE_CANDIDATE_FOLDER_BY_ID_PATH.replace("{id}", Long.toString(id)), "");

        verify(candidateService).createCandidateFolder(anyLong());
    }

    @Test
    @DisplayName("create update live candidate by id succeeds")
    void createUpdateLiveCandidateByIdSucceeds() throws Exception {
        long id = 99L;

        given(candidateService
                .createUpdateSalesforce(id))
                .willReturn(candidate);

        updateCandidateAndVerifyResponse(
                UPDATE_LIVE_CANDIDATE_BY_ID_PATH.replace("{id}", Long.toString(id)), "");

        verify(candidateService).createUpdateSalesforce(id);
    }

    @Test
    @DisplayName("create update opps from candidate succeeds")
    void createUpdateOppsFromCandidateSucceeds() throws Exception {
        UpdateCandidateOppsRequest request = new UpdateCandidateOppsRequest();

        updateCandidate(UPDATE_OPPS_FROM_CANDIDATE_PATH, objectMapper.writeValueAsString(request));

        verify(candidateOpportunityService).createUpdateCandidateOpportunities(any(UpdateCandidateOppsRequest.class));
    }

    @Test
    @DisplayName("create update opps from candidate list succeeds")
    void createUpdateOppsFromCandidateListSucceeds() throws Exception {
        UpdateCandidateListOppsRequest request = new UpdateCandidateListOppsRequest();
        request.setSavedListId(786L);
        request.setCandidateOppParams(new CandidateOpportunityParams());

        updateCandidate(UPDATE_OPPS_FROM_CANDIDATE_LIST_PATH, objectMapper.writeValueAsString(request));

        verify(savedListService).createUpdateSalesforce(any(UpdateCandidateListOppsRequest.class));
    }

    @Test
    @DisplayName("update intake data by id succeeds")
    void updateIntakeDataByIdSucceeds() throws Exception {
        long id = 99L;
        CandidateIntakeDataUpdate request = new CandidateIntakeDataUpdate();

        updateCandidate(
                UPDATE_INTAKE_DATA_BY_ID_PATH.replace("{id}", Long.toString(id)),
                objectMapper.writeValueAsString(request));

        verify(candidateService).updateIntakeData(anyLong(), any(CandidateIntakeDataUpdate.class));
    }

    @Test
    @DisplayName("resolve outstanding tasks succeeds")
    void resolveOutstandingTasksSucceeds() throws Exception {
        ResolveTaskAssignmentsRequest request = new ResolveTaskAssignmentsRequest();

        updateCandidate(RESOLVE_TASKS_PATH, objectMapper.writeValueAsString(request));

        verify(candidateService).resolveOutstandingTaskAssignments(any(ResolveTaskAssignmentsRequest.class));
    }

    @Test
    @DisplayName("generate token succeeds")
    void generateTokenSucceeds() throws Exception {
        String cn = "99";
        String token = "token";

        given(candidateTokenProvider.generateCvToken(any(CvClaims.class), anyLong()))
                .willReturn(token);

        mockMvc.perform(get(BASE_PATH + GENERATE_TOKEN_PATH.replace("{cn}", cn))
                        .header("Authorization", "Bearer " + "jwt-token")
                        .accept(MediaType.TEXT_PLAIN_VALUE))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(jsonPath("$", is(token)));

        verify(candidateTokenProvider).generateCvToken(any(CvClaims.class), anyLong());
    }

    private void postSearchRequestAndVerifyResponse(String path, String body) throws Exception {
        mockMvc.perform(post(BASE_PATH + path)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(
                    getListOfCandidates().size())))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    private void postApiRequest(String path, String body) throws Exception {
        mockMvc.perform(post(BASE_PATH + path)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                .andDo(print())
                .andExpect(status().isOk());
    }

    private void getCandidateAndVerifyResponse(String path) throws Exception {
        mockMvc.perform(get(BASE_PATH + path)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.nationality.name", is("Pakistan")));
    }

    private Candidate addMissingDestinations(Candidate candidate) {
        CandidateDestination uk = new CandidateDestination();
        uk.setCountry(new Country("UK", Status.active));

        CandidateDestination canada = new CandidateDestination();
        canada.setCountry(new Country("Canada", Status.active));

        candidate.setCandidateDestinations(new ArrayList<>(List.of(uk, canada)));
        candidate.setCandidateVisaChecks(new ArrayList<>(List.of(new CandidateVisaCheck())));

        return candidate;
    }

    private void getIntakeDataAndVerifyResponse(String path) throws Exception {
        mockMvc.perform(get(BASE_PATH + path)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.candidateDestinations").isArray())
                .andExpect(jsonPath("$.candidateDestinations", hasSize(2)))
                .andExpect(jsonPath("$.candidateDestinations[0].country.name", is("Canada")))
                .andExpect(jsonPath("$.candidateDestinations[1].country.name", is("UK")));
    }

    private void updateCandidate(String path, String body) throws Exception {
        mockMvc.perform(put(BASE_PATH + path)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                .andDo(print())
                .andExpect(status().isOk());
    }

    private void updateCandidateAndVerifyResponse(String path, String body) throws Exception {
        mockMvc.perform(put(BASE_PATH + path)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.nationality.name", is("Pakistan")));
    }

}
