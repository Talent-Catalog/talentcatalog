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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.CandidateTestData.getCandidateVisaJobCheck;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.CandidateVisaJobCheck;
import org.tctalent.server.request.candidate.visa.job.CreateCandidateVisaJobCheckRequest;
import org.tctalent.server.service.db.CandidateVisaJobCheckService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.service.db.SalesforceService;

/**
 * Unit tests for Candidate Visa Job Check Admin Api endpoints.
 *
 * @author Caroline Cameron
 */
@WebMvcTest(CandidateVisaJobCheckAdminApi.class)
@AutoConfigureMockMvc
public class CandidateVisaJobCheckAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/candidate-visa-job";

    private static final CandidateVisaJobCheck candidateVisaJobCheck = getCandidateVisaJobCheck(false);
    private static final CandidateVisaJobCheck candidateVisaJobCheckComplete = getCandidateVisaJobCheck(true);

    @MockBean
    CandidateVisaJobCheckService candidateVisaJobCheckService;
    @MockBean
    OccupationService occupationService;

    @MockBean
    SalesforceService salesforceService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateVisaJobCheckAdminApi candidateVisaJobCheckAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateVisaJobCheckAdminApi).isNotNull();
    }

    @Test
    @DisplayName("get by id succeeds")
    void getByIdSucceeds() throws Exception {
        long visaId = 1L;
        given(candidateVisaJobCheckService
                .getVisaJobCheck(anyLong()))
                .willReturn(candidateVisaJobCheckComplete);

        mockMvc.perform(get(BASE_PATH + "/" + visaId)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.jobOpp", notNullValue()))
                .andExpect(jsonPath("$.interest", is("Yes")))
                .andExpect(jsonPath("$.interestNotes", is("These are some interest notes.")))
                .andExpect(jsonPath("$.regional", is("No")))
                .andExpect(jsonPath("$.salaryTsmit", is("Yes")))
                .andExpect(jsonPath("$.qualification", is("Yes")))
                .andExpect(jsonPath("$.eligible_494", is("No")))
                .andExpect(jsonPath("$.eligible_494_Notes", is("These are some eligible for visa 494 notes.")))
                .andExpect(jsonPath("$.eligible_186", is("Yes")))
                .andExpect(jsonPath("$.eligible_186_Notes", is("These are some eligible for visa 186 notes.")))
                .andExpect(jsonPath("$.eligibleOther", is("SpecialHum")))
                .andExpect(jsonPath("$.eligibleOtherNotes", is("These are some eligible for other visa notes.")))
                .andExpect(jsonPath("$.putForward", is("DiscussFurther")))
                .andExpect(jsonPath("$.tbbEligibility", is("Discuss")))
                .andExpect(jsonPath("$.notes", is("These are some notes.")))
                .andExpect(jsonPath("$.occupation", notNullValue()))
                .andExpect(jsonPath("$.occupationNotes", is("These are some occupation notes.")))
                .andExpect(jsonPath("$.qualificationNotes", is("These are some qualification notes.")))
                .andExpect(jsonPath("$.relevantWorkExp", is("These are some relevant work experience notes.")))
                .andExpect(jsonPath("$.ageRequirement", is("There are some age requirements.")))
                .andExpect(jsonPath("$.preferredPathways", is("These are some preferred pathways.")))
                .andExpect(jsonPath("$.ineligiblePathways", is("These are some ineligible pathways.")))
                .andExpect(jsonPath("$.eligiblePathways", is("These are some eligible pathways.")))
                .andExpect(jsonPath("$.occupationCategory", is("This is the occupation category.")))
                .andExpect(jsonPath("$.occupationSubCategory", is("This is the occupation subcategory.")))
                .andExpect(jsonPath("$.englishThreshold", is("Yes")))
                .andExpect(jsonPath("$.languagesRequired.[0]", is(342)))
                .andExpect(jsonPath("$.languagesThresholdMet", is("Yes")))
                .andExpect(jsonPath("$.languagesThresholdNotes", is("These are some language threshold notes.")));

        verify(candidateVisaJobCheckService).getVisaJobCheck(anyLong());
    }

    @Test
    @DisplayName("create visa job check succeeds")
    void createVisaJobCheckSucceeds() throws Exception {
        CreateCandidateVisaJobCheckRequest request = new CreateCandidateVisaJobCheckRequest();
        request.setJobOppId(99L);

        given(candidateVisaJobCheckService
                .createVisaJobCheck(anyLong(), any(CreateCandidateVisaJobCheckRequest.class)))
                .willReturn(candidateVisaJobCheck);

        mockMvc.perform(post(BASE_PATH + "/" + 99L)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.jobOpp.id", is(99)));

        verify(candidateVisaJobCheckService).createVisaJobCheck(anyLong(), any(CreateCandidateVisaJobCheckRequest.class));
    }

    @Test
    @DisplayName("delete visa job check by id succeeds")
    void deleteByIdSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/" + anyLong())
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andDo(print())
                .andExpect(status().isOk());

        verify(candidateVisaJobCheckService).deleteVisaJobCheck(anyLong());
    }
}
