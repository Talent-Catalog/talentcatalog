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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.security.JwtTokenProvider;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.security.TcUserDetailsService;

/**
 * Candidate search as done by the admin portal: get the user's default saved search, then search
 * with it. The results are read from candidate JSON built by the database (CandidateReadDto).
 * <p>
 * Added in TC-1189: the upgrade to Spring Boot 4 / Jackson 3 initially broke reading that JSON
 * (CandidateReadDto has no default constructor), and no existing test returned candidates through
 * this path. Set TC_CANDIDATE_SEARCH_OUT to a file path to also write the search response.
 * </p>
 */
@SpringBootTest
class CandidateSearchIntegrationTest extends BaseDBIntegrationTest {

    @MockitoBean
    private CandidateRedisCache candidateRedisCache;

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserRepository userRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private TcUserDetailsService tcUserDetailsService;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private MockMvc mockMvc;
    private String jwt;
    private Long adminId;
    private Long candidateId;
    private String candidateNumber;
    private final List<Long> userIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            User admin = user(Role.systemadmin);
            adminId = admin.getId();

            User candidateUser = user(Role.user);
            //Search only returns candidates of source partners. As set when candidates register.
            PartnerImpl defaultSourcePartner = new PartnerImpl();
            defaultSourcePartner.setId(2L);
            candidateUser.setPartner(defaultSourcePartner);
            candidateUser = userRepository.save(candidateUser);
            Candidate candidate = new Candidate();
            candidate.setUser(candidateUser);
            candidateNumber = "TC1189S" + (Math.abs(candidateUser.getUsername().hashCode()) % 1_000_000);
            candidate.setCandidateNumber(candidateNumber);
            candidate.setStatus(CandidateStatus.active);
            candidate.setPhone("+61 400 000 000");
            candidate.setWhatsapp("+61 400 000 001");
            candidate.setContactConsentRegistration(true);
            candidate.setContactConsentPartners(true);
            candidate.setUnhcrStatus(UnhcrStatus.RegisteredAsylum);
            candidate.setGender(Gender.female);
            candidate.setDob(LocalDate.of(1990, 5, 17));
            //As set when candidates register
            candidate.setCountry(countryRepository.getReferenceById(0L));
            candidate.setNationality(countryRepository.getReferenceById(0L));
            candidate.setCreatedBy(admin);
            candidateId = candidateRepository.save(candidate).getId();

            TcUserDetails userDetails = tcUserDetailsService.loadUserByUsername(admin.getUsername());
            jwt = jwtTokenProvider.generateToken(new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()));
        });
    }

    @AfterEach
    void tearDown() {
        //Getting the default saved search also creates the user's selection list for it
        jdbc.update("delete from candidate_saved_list where saved_list_id in"
            + " (select id from saved_list where created_by = ?)", adminId);
        jdbc.update("delete from saved_list where created_by = ?", adminId);
        jdbc.update("delete from saved_search where created_by = ?", adminId);
        jdbc.update("delete from candidate where id = ?", candidateId);
        for (Long id : userIds) {
            jdbc.update("delete from users where id = ?", id);
        }
    }

    @Test
    @DisplayName("candidate search returns candidates read from database JSON")
    void candidateSearch() throws Exception {
        String defaultSearch = mockMvc.perform(get("/api/admin/saved-search/default")
                .header("Authorization", "Bearer " + jwt))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        long savedSearchId = ((Number) JsonPath.read(defaultSearch, "$.id")).longValue();

        MvcResult result = mockMvc.perform(post("/api/admin/candidate/search")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"savedSearchId\": " + savedSearchId + ", \"candidateNumbers\": [\""
                    + candidateNumber + "\"], \"pageNumber\": 0, \"pageSize\": 5}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].id").value(candidateId))
            .andExpect(jsonPath("$.content[0].candidateNumber").value(candidateNumber))
            .andExpect(jsonPath("$.content[0].status").value("active"))
            .andExpect(jsonPath("$.content[0].gender").value("female"))
            .andExpect(jsonPath("$.content[0].dob").value("1990-05-17"))
            .andReturn();

        String out = System.getenv("TC_CANDIDATE_SEARCH_OUT");
        if (out != null) {
            Files.writeString(Path.of(out),
                result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        }
    }

    private User user(Role role) {
        String name = "tc1189_search_" + UUID.randomUUID();
        User user = TestDataFactory.createUser(null);
        user.setUsername(name);
        user.setEmail(name + "@example.com");
        user.setRole(role);
        user = userRepository.save(user);
        userIds.add(0, user.getId());
        return user;
    }
}
