/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.explanation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.MatchExplanationException;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Matching;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.explanation.CandidateMatchingExplanation;
import org.tctalent.server.model.db.explanation.CandidateMatchingExplanationKey;
import org.tctalent.server.repository.db.CandidateMatchingExplanationRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.MatchingRepository;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.response.CandidateMatchExplanation;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.service.db.MatchingService;
import org.tctalent.server.service.explanation.CandidateMatchExplanationService;
import org.tctalent.server.service.explanation.dto.CandidateMatchExplanationServiceClient;
import org.tctalent.server.service.explanation.dto.ExplanationResult;
import org.tctalent.server.service.explanation.dto.ExplanationsRequest;
import org.tctalent.server.service.explanation.dto.ExplanationsResponse;

/**
 * Match explanations against real Postgres, with only the Python explanation service mocked.
 * <p/>
 * Deliberately not {@code @Transactional}: the service holds no transaction open while
 * generating, and concurrency protection depends on what other transactions have committed, so
 * each step must commit just as it does in the application. The data created is deleted after
 * each test.
 */
@SpringBootTest
class CandidateMatchExplanationIntegrationTest extends BaseDBIntegrationTest {

    private static final String DESCRIPTION = "<p>Experienced ICU nurse</p>";
    private static final OffsetDateTime GENERATED_AT =
        OffsetDateTime.parse("2026-10-02T03:30:00Z");

    @Autowired private CandidateMatchExplanationService explanationService;
    @Autowired private MatchingService matchingService;
    @Autowired private CandidateMatchingExplanationRepository explanationRepository;
    @Autowired private MatchingRepository matchingRepository;
    @Autowired private SalesforceJobOppRepository salesforceJobOppRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PlatformTransactionManager transactionManager;

    @MockitoBean private CandidateMatchExplanationServiceClient client;
    @MockitoBean private CandidateRedisCache candidateRedisCache;

    private final List<Long> userIds = new ArrayList<>();
    private final List<Long> candidateIds = new ArrayList<>();
    private final List<Long> jobIds = new ArrayList<>();
    private final List<Long> matchingIds = new ArrayList<>();

    private User user;
    private Candidate candidate;

    @BeforeEach
    void setUp() {
        user = createUser("explanation-user");
        logIn(user);
        //The candidate (with its user) is created in one transaction, as the factory expects.
        candidate = new TransactionTemplate(transactionManager).execute(status ->
            TestDataFactory.createAndSaveCandidate(
                candidateRepository, createUser("explanation-candidate")));
        candidateIds.add(candidate.getId());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        //Jobs reference Matchings, and explanations are deleted with their Matching.
        jobIds.forEach(id -> jdbcTemplate.update("delete from salesforce_job_opp where id = ?", id));
        matchingIds.forEach(id -> jdbcTemplate.update("delete from matching where id = ?", id));
        candidateIds.forEach(id -> jdbcTemplate.update("delete from candidate where id = ?", id));
        userIds.forEach(id -> jdbcTemplate.update("delete from users where id = ?", id));
    }

    @Test
    @DisplayName("a Matching with no job: generates, caches and then reuses the explanation")
    void nonJobMatching_generatesThenReusesExplanation() {
        Matching matching = createMatching(DESCRIPTION);
        givenPythonReturns("First summary");

        CandidateMatchExplanation first = explain(matching);
        CandidateMatchExplanation second = explain(matching);

        assertThat(first.getSummary()).isEqualTo("First summary");
        assertThat(second.getSummary()).isEqualTo("First summary");
        ArgumentCaptor<ExplanationsRequest> request =
            ArgumentCaptor.forClass(ExplanationsRequest.class);
        verify(client, times(1)).generateExplanations(request.capture());
        assertThat(request.getValue().getOpportunityDescription()).isEqualTo(DESCRIPTION);
        assertThat(explanationCount(matching)).isEqualTo(1);
    }

    @Test
    @DisplayName("saves the explanation json, the Matching's updatedDate to the microsecond, and audit columns")
    void savesExplanationWithSnapshotDateAndAuditColumns() {
        Matching matching = createMatching(DESCRIPTION);
        givenPythonReturns("Summary");

        explain(matching);

        CandidateMatchingExplanation saved = findExplanation(matching);
        assertThat(saved.getExplanation().getSummary()).isEqualTo("Summary");
        assertThat(saved.getExplanation().getGeneratedAt()).isEqualTo(GENERATED_AT);
        assertThat(saved.getExplanation().getModelName()).isEqualTo("test-model");
        //Postgres stores microseconds - the snapshot must survive the round trip exactly.
        OffsetDateTime matchingUpdatedDate = matchingRepository.findSnapshot(matching.getId())
            .orElseThrow().updatedDate();
        assertThat(saved.getMatchingUpdatedDate()).isEqualTo(matchingUpdatedDate);
        assertThat(saved.isCurrent(matchingUpdatedDate)).isTrue();
        assertThat(jdbcTemplate.queryForObject("select count(*) from candidate_matching_explanation e"
                + " join matching m on m.id = e.matching_id"
                + " where e.matching_id = ? and e.matching_updated_date = m.updated_date",
            Long.class, matching.getId())).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject("select explanation ->> 'generatedAt'"
                + " from candidate_matching_explanation where matching_id = ?",
            String.class, matching.getId())).isEqualTo("2026-10-02T03:30:00Z");
        assertThat(jdbcTemplate.queryForObject("select created_by from"
                + " candidate_matching_explanation where matching_id = ?",
            Long.class, matching.getId())).isEqualTo(user.getId());
        assertThat(saved.getCreatedDate()).isNotNull();
    }

    @Test
    @DisplayName("a changed Matching description makes the explanation stale: it is regenerated and replaced")
    void staleExplanation_isRegeneratedAndReplaced() {
        Matching matching = createMatching(DESCRIPTION);
        givenPythonReturns("First summary", "Second summary");
        explain(matching);

        matchingService.updateMatchingDescription(matching.getId(), "<p>Paediatric nurse</p>");
        CandidateMatchExplanation explanation = explain(matching);

        assertThat(explanation.getSummary()).isEqualTo("Second summary");
        assertThat(lastDescriptionSentToPython(2)).isEqualTo("<p>Paediatric nurse</p>");
        assertThat(explanationCount(matching)).isEqualTo(1);
        assertThat(findExplanation(matching).getExplanation().getSummary())
            .isEqualTo("Second summary");
    }

    @Test
    @DisplayName("an unchanged description doesn't make the explanation stale")
    void unchangedDescription_keepsExplanationCurrent() {
        Matching matching = createMatching(DESCRIPTION);
        givenPythonReturns("First summary");
        explain(matching);

        matchingService.updateMatchingDescription(matching.getId(), DESCRIPTION);

        assertThat(explain(matching).getSummary()).isEqualTo("First summary");
        verify(client, times(1)).generateExplanations(any());
    }

    @Test
    @DisplayName("regenerating replaces a current explanation, keeping its creator but updating who updated it")
    void regenerate_replacesCurrentExplanation() {
        Matching matching = createMatching(DESCRIPTION);
        givenPythonReturns("First summary", "Regenerated summary");
        explain(matching);
        User otherUser = createUser("explanation-other-user");
        logIn(otherUser);

        CandidateMatchExplanation explanation =
            explanationService.regenerateExplanation(candidate.getId(), matching.getId());

        assertThat(explanation.getSummary()).isEqualTo("Regenerated summary");
        verify(client, times(2)).generateExplanations(any());
        assertThat(explanationCount(matching)).isEqualTo(1);
        assertThat(findExplanation(matching).getExplanation().getSummary())
            .isEqualTo("Regenerated summary");
        assertThat(jdbcTemplate.queryForObject("select created_by from"
                + " candidate_matching_explanation where matching_id = ?",
            Long.class, matching.getId())).isEqualTo(user.getId());
        assertThat(jdbcTemplate.queryForObject("select updated_by from"
                + " candidate_matching_explanation where matching_id = ?",
            Long.class, matching.getId())).isEqualTo(otherUser.getId());
    }

    @Test
    @DisplayName("a job's Matching: explains against the Matching's refined description, never the job's text")
    void jobMatching_usesRefinedDescription() {
        SalesforceJobOpp job = createJob("<p>Original job summary</p>");
        Matching matching = matchingService.getOrCreateJobMatching(job, "<p>Refined</p>");
        matchingIds.add(matching.getId());
        givenPythonReturns("Summary");

        explain(matching);

        assertThat(lastDescriptionSentToPython(1)).isEqualTo("<p>Refined</p>");
        assertThat(matchingRepository.findSnapshot(matching.getId()).orElseThrow()
            .matchingDescription()).isEqualTo("<p>Refined</p>");
    }

    @Test
    @DisplayName("a job's blank Matching is initialised from the job's current text when an explanation is needed")
    void blankJobMatching_isInitialisedFromJobText() {
        SalesforceJobOpp job = createJob(null);
        Matching matching = matchingService.getOrCreateJobMatching(job, null);
        matchingIds.add(matching.getId());
        assertThat(matching.getMatchingDescription()).isNull();
        job.setJobSummary("<p>Job summary added later</p>");
        salesforceJobOppRepository.saveAndFlush(job);
        givenPythonReturns("Summary");

        explain(matching);

        assertThat(lastDescriptionSentToPython(1)).isEqualTo("<p>Job summary added later</p>");
        assertThat(matchingRepository.findSnapshot(matching.getId()).orElseThrow()
            .matchingDescription()).isEqualTo("<p>Job summary added later</p>");
        assertThat(explanationCount(matching)).isEqualTo(1);
    }

    @Test
    @DisplayName("a blank Matching with no job, or whose job has no text, is rejected without calling Python")
    void blankMatching_isRejected() {
        Matching nonJobMatching = createMatching("<p>&nbsp;</p>");
        SalesforceJobOpp job = createJob(null);
        Matching jobMatching = matchingService.getOrCreateJobMatching(job, null);
        matchingIds.add(jobMatching.getId());

        assertThatThrownBy(() -> explain(nonJobMatching))
            .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> explain(jobMatching))
            .isInstanceOf(InvalidRequestException.class);

        verify(client, never()).generateExplanations(any());
        assertThat(explanationCount(nonJobMatching)).isZero();
        assertThat(explanationCount(jobMatching)).isZero();
    }

    @Test
    @DisplayName("an explanation of a description which changed during generation is discarded, and regenerated from the new description")
    void matchingChangedDuringGeneration_discardsAndRegenerates() {
        Matching matching = createMatching(DESCRIPTION);
        AtomicInteger calls = new AtomicInteger();
        when(client.generateExplanations(any())).thenAnswer(invocation -> {
            if (calls.incrementAndGet() == 1) {
                //Another user edits the requirements while the LLM is working.
                matchingService.updateMatchingDescription(
                    matching.getId(), "<p>Paediatric nurse</p>");
                return response("Explanation of old description");
            }
            return response("Explanation of new description");
        });

        CandidateMatchExplanation explanation = explain(matching);

        assertThat(explanation.getSummary()).isEqualTo("Explanation of new description");
        assertThat(lastDescriptionSentToPython(2)).isEqualTo("<p>Paediatric nurse</p>");
        CandidateMatchingExplanation saved = findExplanation(matching);
        assertThat(saved.getExplanation().getSummary())
            .isEqualTo("Explanation of new description");
        assertThat(saved.isCurrent(matchingRepository.findSnapshot(matching.getId())
            .orElseThrow().updatedDate())).isTrue();
    }

    @Test
    @DisplayName("an explanation of an older description never overwrites a current one saved meanwhile")
    void olderExplanation_neverOverwritesCurrentExplanation() {
        Matching matching = createMatching(DESCRIPTION);
        AtomicInteger calls = new AtomicInteger();
        when(client.generateExplanations(any())).thenAnswer(invocation -> {
            if (calls.incrementAndGet() == 1) {
                //While this (slow) request is generating, the description changes, and another
                //request explains the candidate against the new description.
                matchingService.updateMatchingDescription(
                    matching.getId(), "<p>Paediatric nurse</p>");
                explain(matching);
                return response("Explanation of old description");
            }
            return response("Explanation of new description");
        });

        CandidateMatchExplanation explanation = explain(matching);

        //The slow request returns the current explanation rather than generating a third time.
        assertThat(explanation.getSummary()).isEqualTo("Explanation of new description");
        verify(client, times(2)).generateExplanations(any());
        assertThat(findExplanation(matching).getExplanation().getSummary())
            .isEqualTo("Explanation of new description");
    }

    @Test
    @DisplayName("a description which keeps changing during generation fails cleanly, saving nothing")
    void matchingKeepsChanging_failsWithoutSaving() {
        Matching matching = createMatching(DESCRIPTION);
        AtomicInteger calls = new AtomicInteger();
        when(client.generateExplanations(any())).thenAnswer(invocation -> {
            matchingService.updateMatchingDescription(
                matching.getId(), "<p>Description " + calls.incrementAndGet() + "</p>");
            return response("Out of date explanation");
        });

        assertThatThrownBy(() -> explain(matching))
            .isInstanceOf(MatchExplanationException.class);

        verify(client, times(CandidateMatchExplanationServiceImpl.MAX_GENERATION_ATTEMPTS))
            .generateExplanations(any());
        assertThat(explanationCount(matching)).isZero();
    }

    @Test
    @DisplayName("concurrent requests for the same candidate and Matching both succeed, leaving one explanation")
    void concurrentRequests_leaveOneExplanation() throws Exception {
        Matching matching = createMatching(DESCRIPTION);
        //Both requests are generating at the same time before either saves.
        CyclicBarrier bothGenerating = new CyclicBarrier(2);
        AtomicInteger calls = new AtomicInteger();
        when(client.generateExplanations(any())).thenAnswer(invocation -> {
            final int call = calls.incrementAndGet();
            bothGenerating.await(10, TimeUnit.SECONDS);
            return response("Explanation " + call);
        });

        CompletableFuture<CandidateMatchExplanation> first =
            CompletableFuture.supplyAsync(() -> explain(matching));
        CompletableFuture<CandidateMatchExplanation> second =
            CompletableFuture.supplyAsync(() -> explain(matching));

        List<String> summaries = List.of(
            first.get(30, TimeUnit.SECONDS).getSummary(),
            second.get(30, TimeUnit.SECONDS).getSummary());
        assertThat(summaries).allMatch(summary -> summary.startsWith("Explanation "));
        assertThat(explanationCount(matching)).isEqualTo(1);
        assertThat(summaries).contains(findExplanation(matching).getExplanation().getSummary());
    }

    private CandidateMatchExplanation explain(Matching matching) {
        return explanationService.getOrGenerateExplanation(candidate.getId(), matching.getId());
    }

    private void givenPythonReturns(String summary, String... moreSummaries) {
        ExplanationsResponse[] more = new ExplanationsResponse[moreSummaries.length];
        for (int i = 0; i < moreSummaries.length; i++) {
            more[i] = response(moreSummaries[i]);
        }
        when(client.generateExplanations(any())).thenReturn(response(summary), more);
    }

    private ExplanationsResponse response(String summary) {
        //The test candidate has no job experiences, so there are no experience explanations.
        return ExplanationsResponse.builder()
            .requested(1).succeeded(1).failed(0)
            .results(List.of(ExplanationResult.builder()
                .candidateId(String.valueOf(candidate.getId()))
                .generatedAt(GENERATED_AT)
                .modelName("test-model")
                .summary(summary)
                .experienceExplanations(List.of())
                .limitations(List.of())
                .build()))
            .build();
    }

    private String lastDescriptionSentToPython(int expectedCalls) {
        ArgumentCaptor<ExplanationsRequest> request =
            ArgumentCaptor.forClass(ExplanationsRequest.class);
        verify(client, times(expectedCalls)).generateExplanations(request.capture());
        return request.getValue().getOpportunityDescription();
    }

    private CandidateMatchingExplanation findExplanation(Matching matching) {
        return explanationRepository.findById(
                new CandidateMatchingExplanationKey(candidate.getId(), matching.getId()))
            .orElseThrow();
    }

    private long explanationCount(Matching matching) {
        return jdbcTemplate.queryForObject(
            "select count(*) from candidate_matching_explanation where matching_id = ?",
            Long.class, matching.getId());
    }

    private Matching createMatching(@Nullable String description) {
        Matching matching = matchingService.createMatching(description);
        matchingIds.add(matching.getId());
        return matching;
    }

    private SalesforceJobOpp createJob(@Nullable String jobSummary) {
        SalesforceJobOpp job = TestDataFactory.createSalesforceJobOpportunity();
        job.setSfId("EXPLANATION" + System.nanoTime());
        job.setJobSummary(jobSummary);
        job.setCreatedBy(user);
        job = salesforceJobOppRepository.saveAndFlush(job);
        jobIds.add(job.getId());
        return job;
    }

    private User createUser(String prefix) {
        User newUser = TestDataFactory.createUser(null);
        String token = prefix + "-" + System.nanoTime();
        newUser.setUsername(token);
        newUser.setEmail(token + "@example.com");
        newUser = userRepository.saveAndFlush(newUser);
        userIds.add(newUser.getId());
        return newUser;
    }

    private void logIn(User loggedInUser) {
        TcUserDetails userDetails = new TcUserDetails(loggedInUser);
        userDetails.setAuthorities(List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(
            new UsernamePasswordAuthenticationToken(userDetails, null, List.of()));
        SecurityContextHolder.setContext(securityContext);
    }
}
