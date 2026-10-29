/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.integration.api;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Stat;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateSavedListRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.security.JwtTokenProvider;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.security.TcUserDetailsService;

/**
 * Runs every candidate statistic through the stats API, both over all candidates and over the
 * candidates in a list - each uses different native SQL queries returning Object[] rows, whose
 * values our code casts (eg the count to Long).
 * <p>
 * Added in TC-1189 because these native queries were not otherwise run with data by the Gradle
 * build (CandidateStatsServiceImplTest needs a development database).
 * </p>
 */
@SpringBootTest
@Transactional
class CandidateStatsIntegrationTest extends BaseDBIntegrationTest {

    @MockitoBean
    private CandidateRedisCache candidateRedisCache;

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private UserRepository userRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private SavedListRepository savedListRepository;
    @Autowired private CandidateSavedListRepository candidateSavedListRepository;
    @Autowired private TcUserDetailsService tcUserDetailsService;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private MockMvc mockMvc;
    private String jwt;
    private SavedList savedList;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();

        User admin = TestDataFactory.createUser(null);
        admin.setUsername("tc1189_stats_admin");
        admin.setEmail("tc1189_stats_admin@example.com");
        admin.setRole(Role.systemadmin);
        admin = userRepository.saveAndFlush(admin);

        User candidateUser = TestDataFactory.createUser(null);
        candidateUser.setUsername("tc1189_stats_candidate");
        candidateUser.setEmail("tc1189_stats_candidate@example.com");
        candidateUser = userRepository.saveAndFlush(candidateUser);

        Candidate candidate = new Candidate();
        candidate.setUser(candidateUser);
        candidate.setCandidateNumber("TC1189STATS1");
        candidate.setStatus(CandidateStatus.active);
        candidate.setGender(Gender.female);
        candidate.setDob(LocalDate.of(1990, 5, 17));
        candidate.setLinkedInLink("https://linkedin.example/tc1189");
        candidate.setContactConsentRegistration(true);
        candidate.setContactConsentPartners(true);
        candidate.setCreatedBy(admin);
        candidate.setCountry(countryRepository.findAll().get(0));
        candidate.setNationality(candidate.getCountry());
        candidate = candidateRepository.saveAndFlush(candidate);
        candidateUser.setCandidate(candidate);
        userRepository.saveAndFlush(candidateUser);

        savedList = TestDataFactory.createSavedList();
        savedList.setName("tc1189 stats list");
        savedList.setCreatedBy(admin);
        savedList = savedListRepository.saveAndFlush(savedList);
        candidateSavedListRepository.saveAndFlush(
            TestDataFactory.createCandidateSavedList(candidate, savedList));
        //So that the list is reloaded, with its candidate
        entityManager.flush();
        entityManager.clear();

        TcUserDetails userDetails = tcUserDetailsService.loadUserByUsername(admin.getUsername());
        jwt = jwtTokenProvider.generateToken(new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    @DisplayName("all statistics are computed over all candidates")
    void allStatsOverAllCandidates() throws Exception {
        mockMvc.perform(post("/api/admin/candidate/stat/all")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dateFrom\": \"2000-01-01\", \"dateTo\": \"2100-01-01\", " + selectedStats() + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(Stat.values().length)))
            .andExpect(jsonPath("$[*].rows[*].value", everyItem(greaterThanOrEqualTo(0))));
    }

    @Test
    @DisplayName("all statistics are computed over the candidates in a list")
    void allStatsOverListCandidates() throws Exception {
        mockMvc.perform(post("/api/admin/candidate/stat/all")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"listId\": " + savedList.getId() + ", \"dateFrom\": \"2000-01-01\", \"dateTo\": \"2100-01-01\", "
                    + selectedStats() + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(Stat.values().length)))
            .andExpect(jsonPath("$[*].rows[*].value", everyItem(greaterThanOrEqualTo(0))))
            //The list contains just our female candidate born in 1990
            .andExpect(jsonPath("$[?(@.name == 'Gender')].rows[*].label", hasItem("female")))
            .andExpect(jsonPath("$[?(@.name == 'Gender')].rows[*].value", hasItem(1)))
            .andExpect(jsonPath("$[?(@.name == 'Birth Year')].rows[*].label", hasItem("1990")));
    }

    private static String selectedStats() {
        return "\"selectedStats\": [" + Arrays.stream(Stat.values())
            .map(s -> "\"" + s.name() + "\"")
            .collect(Collectors.joining(", ")) + "]";
    }
}
