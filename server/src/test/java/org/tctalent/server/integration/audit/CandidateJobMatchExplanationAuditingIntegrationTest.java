/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.integration.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.explanation.CandidateJobMatchExplanation;
import org.tctalent.server.model.db.explanation.CandidateJobMatchExplanationData;
import org.tctalent.server.model.db.explanation.CandidateJobMatchExplanationKey;
import org.tctalent.server.repository.db.CandidateJobMatchExplanationRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;

/**
 * Integration test verifying that Spring Data JPA auditing (@CreatedDate/@CreatedBy/
 * @LastModifiedDate/@LastModifiedBy) is correctly populated on {@link CandidateJobMatchExplanation},
 * and that the upsert pattern used by the service layer (find-or-new, mutate, save) collapses to a
 * single persisted row per (candidate, job) pair.
 * <p/>
 * Follows the same {@link BaseDBIntegrationTest} (real Postgres via Testcontainers) pattern as
 * {@link CandidateDataAuditingIntegrationTest}, since {@link CandidateJobMatchExplanation} cannot
 * extend {@code AbstractCandidateDataDomainObject} (see the doc on that entity).
 */
@SpringBootTest
@Transactional
class CandidateJobMatchExplanationAuditingIntegrationTest extends BaseDBIntegrationTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private SalesforceJobOppRepository salesforceJobOppRepository;
    @Autowired private CandidateJobMatchExplanationRepository candidateJobMatchExplanationRepository;

    @MockitoBean private SavedSearchService savedSearchService;
    @MockitoBean private CandidateRedisCache candidateRedisCache;

    private User systemAdmin;

    @BeforeEach
    void setUp() {
        systemAdmin = userService.getSystemAdminUser();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("sets audit fields on create, and preserves created* while advancing updated* on update by a different user")
    void candidateJobMatchExplanationAudits() {
        User creator = createUser("explanation-creator");
        User updater = createUser("explanation-updater");

        authenticateAs(creator);
        Candidate candidate = TestDataFactory.createAndSaveCandidate(
            candidateRepository, createUser("explanation-candidate-user"));
        SalesforceJobOpp job =
            TestDataFactory.createAndSaveSalesforceJobOpportunity(salesforceJobOppRepository);

        CandidateJobMatchExplanation entity = new CandidateJobMatchExplanation(candidate, job);
        entity.setExplanation(CandidateJobMatchExplanationData.builder()
            .summary("Initial summary")
            .experienceExplanations(List.of())
            .limitations(List.of())
            .build());
        entity = candidateJobMatchExplanationRepository.saveAndFlush(entity);

        assertNotNull(entity.getCreatedBy());
        assertNotNull(entity.getCreatedDate());
        assertNotNull(entity.getUpdatedBy());
        assertNotNull(entity.getUpdatedDate());
        assertEquals(creator.getId(), entity.getCreatedBy().getId());
        assertEquals(creator.getId(), entity.getUpdatedBy().getId());

        OffsetDateTime createdDate = entity.getCreatedDate();
        Long createdById = entity.getCreatedBy().getId();
        OffsetDateTime firstUpdatedDate = entity.getUpdatedDate();

        authenticateAs(updater);
        entity.setExplanation(CandidateJobMatchExplanationData.builder()
            .summary("Updated summary")
            .experienceExplanations(List.of())
            .limitations(List.of())
            .build());
        entity = candidateJobMatchExplanationRepository.saveAndFlush(entity);

        assertEquals(createdDate, entity.getCreatedDate());
        assertEquals(createdById, entity.getCreatedBy().getId());
        assertEquals(updater.getId(), entity.getUpdatedBy().getId());
        assertNotNull(entity.getUpdatedDate());
        assertFalse(entity.getUpdatedDate().isBefore(firstUpdatedDate));
    }

    @Test
    @DisplayName("upserting the same candidate/job pair updates the existing row rather than inserting a second one")
    void candidateJobMatchExplanationUpsert_updatesExistingRowRatherThanInserting() {
        authenticateAs(systemAdmin);
        Candidate candidate = TestDataFactory.createAndSaveCandidate(
            candidateRepository, createUser("upsert-candidate-user"));
        SalesforceJobOpp job =
            TestDataFactory.createAndSaveSalesforceJobOpportunity(salesforceJobOppRepository);
        CandidateJobMatchExplanationKey key =
            new CandidateJobMatchExplanationKey(candidate.getId(), job.getId());

        // Mirrors the service layer's own find-or-new upsert pattern.
        CandidateJobMatchExplanation firstWrite = candidateJobMatchExplanationRepository.findById(key)
            .orElseGet(() -> new CandidateJobMatchExplanation(candidate, job));
        firstWrite.setExplanation(
            CandidateJobMatchExplanationData.builder().summary("First").build());
        candidateJobMatchExplanationRepository.saveAndFlush(firstWrite);

        assertEquals(1, candidateJobMatchExplanationRepository.count());

        CandidateJobMatchExplanation secondWrite = candidateJobMatchExplanationRepository.findById(key)
            .orElseGet(() -> new CandidateJobMatchExplanation(candidate, job));
        secondWrite.setExplanation(
            CandidateJobMatchExplanationData.builder().summary("Second").build());
        candidateJobMatchExplanationRepository.saveAndFlush(secondWrite);

        assertEquals(1, candidateJobMatchExplanationRepository.count());
        assertEquals("Second", candidateJobMatchExplanationRepository.findById(key)
            .orElseThrow().getExplanation().getSummary());
    }

    private User createUser(String usernamePrefix) {
        User user = TestDataFactory.createUser(null);
        String token = usernamePrefix + "-" + System.nanoTime();
        user.setUsername(token);
        user.setEmail(token + "@example.com");
        return userRepository.saveAndFlush(user);
    }

    private void authenticateAs(User user) {
        TcUserDetails userDetails = new TcUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userDetails, null, List.of()));
    }
}
