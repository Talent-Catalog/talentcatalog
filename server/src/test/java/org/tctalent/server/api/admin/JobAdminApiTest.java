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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppExtended;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.request.job.JobIntakeData;
import org.tctalent.server.request.job.SearchJobRequest;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.request.link.UpdateLinkRequest;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.JobService;

/**
 * Unit tests for Candidate Job Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(JobAdminApi.class)
@AutoConfigureMockMvc
class JobAdminApiTest extends ApiTestBase {

    private static final long JOB_ID = 99L;

    private static final String BASE_PATH = "/api/admin/job";
    private static final String CREATE_SUGGESTED_SEARCH = "/{id}/create-search";
    private static final String UPDATE_INTAKE_DATA = "/{id}/intake";
    private static final String PUBLISH_JOB = "/{id}/publish";
    private static final String REMOVE_SEARCH = "/{id}/remove-search";
    private static final String UPDATE_JD_LINK = "/{id}/jd-link";
    private static final String UPDATE_JOI_LINK = "/{id}/joi-link";
    private static final String UPDATE_INTERVIEW_LINK = "/{id}/interview-link";
    private static final String UPDATE_STARRED = "/{id}/starred";
    private static final String UPDATE_SUMMARY = "/{id}/summary";
    private static final String UPLOAD_JD = "/{id}/upload/jd";
    private static final String UPLOAD_JOI = "/{id}/upload/joi";
    private static final String UPLOAD_INTERVIEW_GUIDANCE = "/{id}/upload/interview";
    private static final String SEARCH_PATH = "/search-paged";
    private static final SalesforceJobOpp job = getSalesforceJobOppExtended();

    private final Page<SalesforceJobOpp> jobPage =
            new PageImpl<>(
                    List.of(job),
                    PageRequest.of(0,10, Sort.unsorted()),
                    1
            );

    @MockBean CountryService countryService;
    @MockBean JobService jobService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JobAdminApi jobAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(jobAdminApi).isNotNull();
    }

    @Test
    @DisplayName("create job succeeds")
    void createJobSucceeds() throws Exception {
        UpdateJobRequest request = new UpdateJobRequest();
        request.setContactUserId(1L);
        request.setSfJoblink("www.sfjoblink.com");
        request.setStage(JobOpportunityStage.cvReview);
        request.setSubmissionDueDate(LocalDate.parse("2020-01-01"));

        given(jobService
                .createJob(any(UpdateJobRequest.class)))
                .willReturn(job);

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
                .andExpect(jsonPath("$.sfId", is("123456")))
                .andExpect(jsonPath("$.contactUser.firstName", is("contact")))
                .andExpect(jsonPath("$.contactUser.lastName", is("user")))
                .andExpect(jsonPath("$.contactUser.email", is("test.contact@tbb.org")))
                .andExpect(jsonPath("$.country.name", is("Canada")))
                .andExpect(jsonPath("$.employerEntity.name", is("ABC Accounts")))
                .andExpect(jsonPath("$.employerEntity.website", is("www.ABCAccounts.com")))
                .andExpect(jsonPath("$.employerEntity.hasHiredInternationally", is(true)))
                .andExpect(jsonPath("$.employerEntity.description", is("This is an employer description.")))
                .andExpect(jsonPath("$.hiringCommitment", is(1)))
                .andExpect(jsonPath("$.opportunityScore", is("Opp Score")))
                .andExpect(jsonPath("$.name", is("Opp Name")))
                .andExpect(jsonPath("$.nextStep", is("This is the next step.")))
                .andExpect(jsonPath("$.nextStepDueDate", is("2020-01-01")))
                .andExpect(jsonPath("$.publishedDate", is("2023-10-30T12:30:00+02:00")))
                .andExpect(jsonPath("$.stage", is("cvReview")))
                .andExpect(jsonPath("$.submissionDueDate", is("2020-01-01")));

        verify(jobService).createJob(any(UpdateJobRequest.class));
    }

    @Test
    @DisplayName("get job by id succeeds")
    void getJobByIdSucceeds() throws Exception {
        given(jobService
                .getJob(anyLong()))
                .willReturn(job);

        mockMvc.perform(get(BASE_PATH + "/" + JOB_ID)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.sfId", is("123456")))

                .andExpect(jsonPath("$.contactUser.firstName", is("contact")))
                .andExpect(jsonPath("$.contactUser.lastName", is("user")))
                .andExpect(jsonPath("$.contactUser.email", is("test.contact@tbb.org")))

                .andExpect(jsonPath("$.country.name", is("Canada")))

                .andExpect(jsonPath("$.createdBy.firstName", is("test")))
                .andExpect(jsonPath("$.createdBy.lastName", is("user")))
                .andExpect(jsonPath("$.createdBy.email", is("audit.user@ngo.org")))

                .andExpect(jsonPath("$.createdDate", is("2023-10-30T12:30:00+02:00")))
                .andExpect(jsonPath("$.employerEntity.name", is("ABC Accounts")))
                .andExpect(jsonPath("$.employerEntity.website", is("www.ABCAccounts.com")))
                .andExpect(jsonPath("$.employerEntity.hasHiredInternationally", is(true)))
                .andExpect(jsonPath("$.employerEntity.description", is("This is an employer description.")))
                .andExpect(jsonPath("$.hiringCommitment", is(1)))
                .andExpect(jsonPath("$.opportunityScore", is("Opp Score")))
                .andExpect(jsonPath("$.exclusionList", notNullValue()))
                .andExpect(jsonPath("$.jobSummary", is("This is a job summary.")))
                .andExpect(jsonPath("$.name", is("Opp Name")))
                .andExpect(jsonPath("$.nextStep", is("This is the next step.")))
                .andExpect(jsonPath("$.nextStepDueDate", is("2020-01-01")))

                .andExpect(jsonPath("$.publishedBy.firstName", is("test")))
                .andExpect(jsonPath("$.publishedBy.lastName", is("user")))
                .andExpect(jsonPath("$.publishedBy.email", is("audit.user@ngo.org")))

                .andExpect(jsonPath("$.publishedDate", is("2023-10-30T12:30:00+02:00")))

                .andExpect(jsonPath("$.jobCreator.name", is("TC Partner")))
                .andExpect(jsonPath("$.jobCreator.abbreviation", is("TCP")))
                .andExpect(jsonPath("$.jobCreator.websiteUrl", is("website_url")))

                .andExpect(jsonPath("$.stage", is("cvReview")))

                .andExpect(jsonPath("$.starringUsers[0].firstName", is("test")))
                .andExpect(jsonPath("$.starringUsers[0].lastName", is("user")))
                .andExpect(jsonPath("$.starringUsers[0].email", is("audit.user@ngo.org")))

                .andExpect(jsonPath("$.submissionDueDate", is("2020-01-01")))
                .andExpect(jsonPath("$.submissionList", notNullValue()))
                .andExpect(jsonPath("$.suggestedList", notNullValue()))
                .andExpect(jsonPath("$.suggestedSearches[0].id", is(123)))
                .andExpect(jsonPath("$.suggestedSearches[0].name", is("My Search")))
                .andExpect(jsonPath("$.updatedBy.firstName", is("test")))
                .andExpect(jsonPath("$.updatedBy.lastName", is("user")))
                .andExpect(jsonPath("$.updatedBy.email", is("audit.user@ngo.org")))
                .andExpect(jsonPath("$.updatedDate", is("2023-10-30T12:30:00+02:00")))

                .andExpect(jsonPath("$.jobOppIntake.id", is(99)))
                .andExpect(jsonPath("$.jobOppIntake.salaryRange", is("80-90k")))
                .andExpect(jsonPath("$.jobOppIntake.recruitmentProcess", is("The recruitment process.")))
                .andExpect(jsonPath("$.jobOppIntake.employerCostCommitment", is("Employer cost commitments.")))
                .andExpect(jsonPath("$.jobOppIntake.location", is("Melbourne")))
                .andExpect(jsonPath("$.jobOppIntake.locationDetails", is("Western suburbs")))
                .andExpect(jsonPath("$.jobOppIntake.benefits", is("These are the benefits.")))
                .andExpect(jsonPath("$.jobOppIntake.languageRequirements", is("These are the language reqs.")))
                .andExpect(jsonPath("$.jobOppIntake.educationRequirements", is("These are the education reqs.")))
                .andExpect(jsonPath("$.jobOppIntake.skillRequirements", is("These are the skill reqs.")))
                .andExpect(jsonPath("$.jobOppIntake.employmentExperience", is("This is the employment experience.")))
                .andExpect(jsonPath("$.jobOppIntake.occupationCode", is("Occupation code")))
                .andExpect(jsonPath("$.jobOppIntake.minSalary", is("80k")))
                .andExpect(jsonPath("$.jobOppIntake.visaPathways", is("The visa pathways")));

        verify(jobService).getJob(anyLong());
    }

    @Test
    @DisplayName("create suggested search succeeds")
    void createSuggestedSearchSucceeds() throws Exception {
        String suffix = "suffix";
        given(jobService
                .createSuggestedSearch(anyLong(), anyString()))
                .willReturn(job);

        mockMvc.perform(post(BASE_PATH + CREATE_SUGGESTED_SEARCH.replace("{id}", String.valueOf(JOB_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(suffix)
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.sfId", is("123456")))

                .andExpect(jsonPath("$.contactUser.firstName", is("contact")))
                .andExpect(jsonPath("$.contactUser.lastName", is("user")))
                .andExpect(jsonPath("$.contactUser.email", is("test.contact@tbb.org")))

                .andExpect(jsonPath("$.country.name", is("Canada")))

                .andExpect(jsonPath("$.createdBy.firstName", is("test")))
                .andExpect(jsonPath("$.createdBy.lastName", is("user")))
                .andExpect(jsonPath("$.createdBy.email", is("audit.user@ngo.org")))

                .andExpect(jsonPath("$.createdDate", is("2023-10-30T12:30:00+02:00")))
                .andExpect(jsonPath("$.employerEntity.name", is("ABC Accounts")))
                .andExpect(jsonPath("$.employerEntity.website", is("www.ABCAccounts.com")))
                .andExpect(jsonPath("$.employerEntity.hasHiredInternationally", is(true)))
                .andExpect(jsonPath("$.employerEntity.description", is("This is an employer description.")))
                .andExpect(jsonPath("$.hiringCommitment", is(1)))
                .andExpect(jsonPath("$.opportunityScore", is("Opp Score")))
                .andExpect(jsonPath("$.exclusionList", notNullValue()))
                .andExpect(jsonPath("$.jobSummary", is("This is a job summary.")))
                .andExpect(jsonPath("$.name", is("Opp Name")))
                .andExpect(jsonPath("$.nextStep", is("This is the next step.")))
                .andExpect(jsonPath("$.nextStepDueDate", is("2020-01-01")))

                .andExpect(jsonPath("$.publishedBy.firstName", is("test")))
                .andExpect(jsonPath("$.publishedBy.lastName", is("user")))
                .andExpect(jsonPath("$.publishedBy.email", is("audit.user@ngo.org")))

                .andExpect(jsonPath("$.publishedDate", is("2023-10-30T12:30:00+02:00")))

                .andExpect(jsonPath("$.jobCreator.name", is("TC Partner")))
                .andExpect(jsonPath("$.jobCreator.abbreviation", is("TCP")))
                .andExpect(jsonPath("$.jobCreator.websiteUrl", is("website_url")))

                .andExpect(jsonPath("$.stage", is("cvReview")))

                .andExpect(jsonPath("$.starringUsers[0].firstName", is("test")))
                .andExpect(jsonPath("$.starringUsers[0].lastName", is("user")))
                .andExpect(jsonPath("$.starringUsers[0].email", is("audit.user@ngo.org")))

                .andExpect(jsonPath("$.submissionDueDate", is("2020-01-01")))
                .andExpect(jsonPath("$.submissionList", notNullValue()))
                .andExpect(jsonPath("$.suggestedList", notNullValue()))
                .andExpect(jsonPath("$.suggestedSearches[0].id", is(123)))
                .andExpect(jsonPath("$.suggestedSearches[0].name", is("My Search")))
                .andExpect(jsonPath("$.updatedBy.firstName", is("test")))
                .andExpect(jsonPath("$.updatedBy.lastName", is("user")))
                .andExpect(jsonPath("$.updatedBy.email", is("audit.user@ngo.org")))
                .andExpect(jsonPath("$.updatedDate", is("2023-10-30T12:30:00+02:00")))

                .andExpect(jsonPath("$.jobOppIntake.id", is(99)))
                .andExpect(jsonPath("$.jobOppIntake.salaryRange", is("80-90k")))
                .andExpect(jsonPath("$.jobOppIntake.recruitmentProcess", is("The recruitment process.")))
                .andExpect(jsonPath("$.jobOppIntake.employerCostCommitment", is("Employer cost commitments.")))
                .andExpect(jsonPath("$.jobOppIntake.location", is("Melbourne")))
                .andExpect(jsonPath("$.jobOppIntake.locationDetails", is("Western suburbs")))
                .andExpect(jsonPath("$.jobOppIntake.benefits", is("These are the benefits.")))
                .andExpect(jsonPath("$.jobOppIntake.languageRequirements", is("These are the language reqs.")))
                .andExpect(jsonPath("$.jobOppIntake.educationRequirements", is("These are the education reqs.")))
                .andExpect(jsonPath("$.jobOppIntake.skillRequirements", is("These are the skill reqs.")))
                .andExpect(jsonPath("$.jobOppIntake.employmentExperience", is("This is the employment experience.")))
                .andExpect(jsonPath("$.jobOppIntake.occupationCode", is("Occupation code")))
                .andExpect(jsonPath("$.jobOppIntake.minSalary", is("80k")))
                .andExpect(jsonPath("$.jobOppIntake.visaPathways", is("The visa pathways")));

        verify(jobService).createSuggestedSearch(anyLong(), anyString());
    }

    @Test
    @DisplayName("update intake data succeeds")
    void updateIntakeDataSucceeds() throws Exception {
        JobIntakeData request = new JobIntakeData();

        mockMvc.perform(put(BASE_PATH + UPDATE_INTAKE_DATA.replace("{id}", String.valueOf(JOB_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk());

        verify(jobService).updateIntakeData(anyLong(), any(JobIntakeData.class));
    }

    @Test
    @DisplayName("publish job succeeds")
    void publishJobSucceeds() throws Exception {
        given(jobService
                .publishJob(anyLong()))
                .willReturn(job);

        mockMvc.perform(put(BASE_PATH + PUBLISH_JOB.replace("{id}", String.valueOf(JOB_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.sfId", is("123456")))

                .andExpect(jsonPath("$.contactUser.firstName", is("contact")))
                .andExpect(jsonPath("$.contactUser.lastName", is("user")))
                .andExpect(jsonPath("$.contactUser.email", is("test.contact@tbb.org")))

                .andExpect(jsonPath("$.publishedBy.firstName", is("test")))
                .andExpect(jsonPath("$.publishedBy.lastName", is("user")))
                .andExpect(jsonPath("$.publishedBy.email", is("audit.user@ngo.org")))

                .andExpect(jsonPath("$.publishedDate", is("2023-10-30T12:30:00+02:00")));

        verify(jobService).publishJob(anyLong());
    }

    @Test
    @DisplayName("remove suggested search succeeds")
    void removeSuggestedSearchSucceeds() throws Exception {
        long savedSearchId = 1L;
        given(jobService
                .removeSuggestedSearch(anyLong(), anyLong()))
                .willReturn(job);

        mockMvc.perform(put(BASE_PATH + REMOVE_SEARCH.replace("{id}", String.valueOf(JOB_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(savedSearchId))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.sfId", is("123456")))
                .andExpect(jsonPath("$.suggestedSearches", hasSize(1)));

        verify(jobService).removeSuggestedSearch(anyLong(), anyLong());
    }

    @Test
    @DisplayName("search paged jobs succeeds")
    void searchPagedSucceeds() throws Exception {
        SearchJobRequest request = new SearchJobRequest();

        given(jobService
                .searchJobs(any(SearchJobRequest.class)))
                .willReturn(jobPage);

        mockMvc.perform(post(BASE_PATH + SEARCH_PATH)
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
                .andExpect(jsonPath("$.content.[0].id", is(99)))
                .andExpect(jsonPath("$.content.[0].sfId", is("123456")));

        verify(jobService).searchJobs(any(SearchJobRequest.class));
    }

    @Test
    @DisplayName("update job by id succeeds")
    void updateJobByIdSucceeds() throws Exception {
        UpdateJobRequest request = new UpdateJobRequest();

        given(jobService
                .updateJob(anyLong(), any(UpdateJobRequest.class)))
                .willReturn(job);

        mockMvc.perform(put(BASE_PATH + "/" + JOB_ID)
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
                .andExpect(jsonPath("$.sfId", is("123456")));

        verify(jobService).updateJob(anyLong(), any(UpdateJobRequest.class));
    }

    @Test
    @DisplayName("update jd link succeeds")
    void updateJdLinkSucceeds() throws Exception {
        UpdateLinkRequest request = new UpdateLinkRequest();

        given(jobService
                .updateJdLink(anyLong(), any(UpdateLinkRequest.class)))
                .willReturn(job);

        mockMvc.perform(put(BASE_PATH + "/" + UPDATE_JD_LINK.replace("{id}", String.valueOf(JOB_ID)))
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
                .andExpect(jsonPath("$.sfId", is("123456")));

        verify(jobService).updateJdLink(anyLong(), any(UpdateLinkRequest.class));
    }

    @Test
    @DisplayName("update joi link succeeds")
    void updateJoiLinkSucceeds() throws Exception {
        UpdateLinkRequest request = new UpdateLinkRequest();

        given(jobService
                .updateJoiLink(anyLong(), any(UpdateLinkRequest.class)))
                .willReturn(job);

        mockMvc.perform(put(BASE_PATH + "/" + UPDATE_JOI_LINK.replace("{id}", String.valueOf(JOB_ID)))
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
                .andExpect(jsonPath("$.sfId", is("123456")));

        verify(jobService).updateJoiLink(anyLong(), any(UpdateLinkRequest.class));
    }

    @Test
    @DisplayName("update interview guidance link succeeds")
    void updateInterviewGuidanceLinkSucceeds() throws Exception {
        UpdateLinkRequest request = new UpdateLinkRequest();

        given(jobService
                .updateInterviewGuidanceLink(anyLong(), any(UpdateLinkRequest.class)))
                .willReturn(job);

        mockMvc.perform(put(BASE_PATH + "/" + UPDATE_INTERVIEW_LINK.replace("{id}", String.valueOf(JOB_ID)))
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
                .andExpect(jsonPath("$.sfId", is("123456")));

        verify(jobService).updateInterviewGuidanceLink(anyLong(), any(UpdateLinkRequest.class));
    }

    @Test
    @DisplayName("update starred succeeds")
    void updateStarredSucceeds() throws Exception {
        boolean starred = true;

        given(jobService
                .updateStarred(anyLong(), anyBoolean()))
                .willReturn(job);

        mockMvc.perform(put(BASE_PATH + "/" + UPDATE_STARRED.replace("{id}", String.valueOf(JOB_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(starred))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.sfId", is("123456")));

        verify(jobService).updateStarred(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("update summary succeeds")
    void updateSummarySucceeds() throws Exception {
        String summary = "This is a job summary.";

        given(jobService
                .updateJobSummary(anyLong(), anyString()))
                .willReturn(job);

        mockMvc.perform(put(BASE_PATH + "/" + UPDATE_SUMMARY.replace("{id}", String.valueOf(JOB_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(summary)
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.jobSummary", is("This is a job summary.")));

        verify(jobService).updateJobSummary(anyLong(), anyString());
    }

    @Test
    @DisplayName("upload JD succeeds")
    void uploadJDSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", "some content".getBytes());

        given(jobService
                .uploadJd(anyLong(), any(MultipartFile.class)))
                .willReturn(job);

        mockMvc.perform(multipart(BASE_PATH + "/" + UPLOAD_JD.replace("{id}", String.valueOf(JOB_ID)))
                        .file("file", file.getBytes())
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.submissionList.fileJdLink", notNullValue()));

        verify(jobService).uploadJd(anyLong(), any(MultipartFile.class));
    }

    @Test
    @DisplayName("upload joi succeeds")
    void uploadJoiSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", "some content".getBytes());

        given(jobService
                .uploadJoi(anyLong(), any(MultipartFile.class)))
                .willReturn(job);

        mockMvc.perform(multipart(BASE_PATH + "/" + UPLOAD_JOI.replace("{id}", String.valueOf(JOB_ID)))
                        .file("file", file.getBytes())
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.submissionList.fileJoiLink", notNullValue()));

        verify(jobService).uploadJoi(anyLong(), any(MultipartFile.class));
    }

    @Test
    @DisplayName("upload interview guidance succeeds")
    void uploadInterviewGuidanceSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", "some content".getBytes());

        given(jobService
            .uploadInterviewGuidance(anyLong(), any(MultipartFile.class)))
            .willReturn(job);

        mockMvc.perform(multipart(BASE_PATH + "/" + UPLOAD_INTERVIEW_GUIDANCE.replace("{id}", String.valueOf(JOB_ID)))
                .file("file", file.getBytes())
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(99)))
            .andExpect(jsonPath("$.submissionList.fileInterviewGuidanceLink", notNullValue()));

        verify(jobService).uploadInterviewGuidance(anyLong(), any(MultipartFile.class));
    }
}
