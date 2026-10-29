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

package org.tctalent.server.integration.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.BatchMetadataCleaner;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateSavedListRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.security.JwtTokenProvider;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.security.TcUserDetailsService;
import org.tctalent.server.service.db.BatchJobService;

/**
 * Runs the real candidate text batch job, as launched from System Admin, end to end: the API
 * launches it asynchronously with Spring Batch, it reads the candidates of a list, updates their
 * text and writes them back, and its execution is reported by the batch job service.
 * <p>
 * Added in TC-1189 (Spring Batch 5 to 6).
 * </p>
 */
@SpringBootTest
class CandidateTextJobIntegrationTest extends BaseDBIntegrationTest {

    private static final String JOB_NAME = "candidateTextJob";

    @MockitoBean
    private CandidateRedisCache candidateRedisCache;

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserRepository userRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private SavedListRepository savedListRepository;
    @Autowired private CandidateSavedListRepository candidateSavedListRepository;
    @Autowired private TcUserDetailsService tcUserDetailsService;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private BatchJobService batchJobService;

    private MockMvc mockMvc;
    private String jwt;
    private Long savedListId;
    private final List<Long> userIds = new ArrayList<>();
    private final List<Long> candidateIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            User admin = user(Role.systemadmin);
            SavedList list = TestDataFactory.createSavedList();
            list.setName("tc1189 text job " + UUID.randomUUID());
            list.setCreatedBy(admin);
            list = savedListRepository.save(list);
            savedListId = list.getId();
            for (String note : List.of("TC1189 first note", "TC1189 second note")) {
                Candidate candidate = candidate(note, admin);
                candidateSavedListRepository.save(
                    TestDataFactory.createCandidateSavedList(candidate, list));
            }

            TcUserDetails userDetails = tcUserDetailsService.loadUserByUsername(admin.getUsername());
            jwt = jwtTokenProvider.generateToken(new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()));
        });
    }

    @AfterEach
    void tearDown() {
        BatchMetadataCleaner.deleteJobs(jdbc, JOB_NAME);

        jdbc.update("delete from candidate_saved_list where saved_list_id = ?", savedListId);
        jdbc.update("delete from saved_list where id = ?", savedListId);
        for (Long id : candidateIds) {
            jdbc.update("delete from candidate where id = ?", id);
        }
        for (Long id : userIds) {
            jdbc.update("delete from users where id = ?", id);
        }
    }

    @Test
    @DisplayName("the candidate text job launched from System Admin updates the list's candidates")
    void candidateTextJob() throws Exception {
        Long previousExecution = latestExecutionId();

        mockMvc.perform(post("/api/admin/system/set_candidate_text/list-" + savedListId + "-cpu-100")
                .header("Authorization", "Bearer " + jwt))
            .andExpect(status().isOk())
            .andExpect(content().string("Job '" + JOB_NAME + "' launched successfully"));

        long executionId = waitForCompletedExecution(previousExecution);

        Map<String, Object> step = jdbc.queryForMap(
            "select status, read_count, write_count from batch_step_execution"
                + " where job_execution_id = ?", executionId);
        assertThat(step).containsEntry("status", "COMPLETED")
            .containsEntry("read_count", 2L)
            .containsEntry("write_count", 2L);
        assertThat(jdbc.queryForList(
            "select text from candidate where id in (?, ?) order by id", String.class,
            candidateIds.get(0), candidateIds.get(1)))
            .containsExactly("TC1189 first note", "TC1189 second note");

        assertThat(batchJobService.getJobExecutionsSummary())
            .contains("Execution ID: " + executionId + System.lineSeparator()
                + "Job Name    : " + JOB_NAME + System.lineSeparator()
                + "Status      : COMPLETED");
    }

    private long waitForCompletedExecution(Long previousExecution) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 60_000;
        while (System.currentTimeMillis() < deadline) {
            Long latest = latestExecutionId();
            if (latest != null && !latest.equals(previousExecution)) {
                String status = jdbc.queryForObject(
                    "select status from batch_job_execution where job_execution_id = ?",
                    String.class, latest);
                if ("COMPLETED".equals(status)) {
                    return latest;
                }
                assertThat(status).as("job status").isIn("STARTING", "STARTED");
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Candidate text job did not complete");
    }

    private Long latestExecutionId() {
        return jdbc.queryForObject("select max(e.job_execution_id) from batch_job_execution e"
            + " join batch_job_instance i on i.job_instance_id = e.job_instance_id"
            + " where i.job_name = ?", Long.class, JOB_NAME);
    }

    private User user(Role role) {
        String name = "tc1189_textjob_" + UUID.randomUUID();
        User user = TestDataFactory.createUser(null);
        user.setUsername(name);
        user.setEmail(name + "@example.com");
        user.setRole(role);
        user = userRepository.save(user);
        userIds.add(0, user.getId());
        return user;
    }

    private Candidate candidate(String shareableNotes, User createdBy) {
        User user = user(Role.user);
        Candidate candidate = new Candidate();
        candidate.setUser(user);
        candidate.setCandidateNumber("TC1189T" + (Math.abs(user.getUsername().hashCode()) % 1_000_000));
        candidate.setStatus(CandidateStatus.active);
        candidate.setPhone("+61 400 000 000");
        candidate.setWhatsapp("+61 400 000 001");
        candidate.setContactConsentRegistration(true);
        candidate.setContactConsentPartners(true);
        candidate.setUnhcrStatus(UnhcrStatus.Unsure);
        candidate.setGender(Gender.female);
        candidate.setShareableNotes(shareableNotes);
        candidate.setCreatedBy(createdBy);
        candidate = candidateRepository.save(candidate);
        candidateIds.add(candidate.getId());
        return candidate;
    }
}
