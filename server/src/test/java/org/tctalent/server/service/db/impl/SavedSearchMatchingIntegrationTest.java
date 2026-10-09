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

package org.tctalent.server.service.db.impl;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.Matching;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.SavedSearchType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.MatchingRepository;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.repository.db.SavedSearchRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.search.CreateFromDefaultSavedSearchRequest;
import org.tctalent.server.request.search.UpdateSavedSearchJobRequest;
import org.tctalent.server.request.search.UpdateSavedSearchRequest;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.service.db.SavedSearchService;

/**
 * Integration tests of the Matching lifecycle of saved searches - see {@link Matching}.
 */
@SpringBootTest
@Transactional
class SavedSearchMatchingIntegrationTest extends BaseDBIntegrationTest {

    private static final String JOB_TEXT = "<p>Job summary: ICU nurses</p>";

    @Autowired private SavedSearchService savedSearchService;
    @Autowired private SavedSearchRepository savedSearchRepository;
    @Autowired private SalesforceJobOppRepository salesforceJobOppRepository;
    @Autowired private MatchingRepository matchingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EntityManager entityManager;

    @MockitoBean private CandidateRedisCache candidateRedisCache;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.createUser(null);
        String token = "matching-" + System.nanoTime();
        user.setUsername(token);
        user.setEmail(token + "@example.com");
        user = userRepository.saveAndFlush(user);
        //A new context rather than modifying the current one, which may be left over from
        //an earlier test on this thread.
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(
            new UsernamePasswordAuthenticationToken(new TcUserDetails(user), null, List.of()));
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("a new search with no job and no requirements has no Matching")
    void newSearchWithoutRequirements_hasNoMatching() {
        SavedSearch search = createSearch("plain", null, null);

        assertThat(reload(search).getMatching()).isNull();
    }

    @Test
    @DisplayName("a new non-job search with requirements gets its own Matching")
    void newNonJobSearch_getsOwnMatching() {
        SavedSearch search = createSearch("own", null, "<p>Welders</p>");

        SavedSearch reloaded = reload(search);
        assertThat(reloaded.getMatching()).isNotNull();
        assertThat(reloaded.getMatching().getMatchingDescription()).isEqualTo("<p>Welders</p>");
        assertThat(reloaded.getRequirements()).isEqualTo("<p>Welders</p>");
    }

    @Test
    @DisplayName("an existing search with a null Matching uses its legacy requirements without creating a Matching")
    void legacySearch_readsLegacyRequirementsWithoutCreatingMatching() {
        SavedSearch search = createLegacySearch("legacy-read", null, "<p>Legacy welders</p>");
        long matchingCount = matchingRepository.count();

        SearchCandidateRequest loaded = savedSearchService.loadSavedSearch(search.getId());

        assertThat(loaded.getRequirements()).isEqualTo("<p>Legacy welders</p>");
        assertThat(reload(search).getMatching()).isNull();
        assertThat(matchingRepository.count()).isEqualTo(matchingCount);
    }

    @Test
    @DisplayName("updating a legacy non-job search creates its Matching from the requirements and clears the legacy value")
    void legacySearch_updateCreatesMatchingAndClearsLegacyRequirements() {
        SavedSearch search = createLegacySearch("legacy-update", null, "<p>Legacy welders</p>");

        //As the UI does: the form is loaded with the search's requirements and sent back.
        updateSearch(search, "<p>Legacy welders</p>", null);

        SavedSearch reloaded = reload(search);
        assertThat(reloaded.getMatching()).isNotNull();
        assertThat(reloaded.getMatching().getMatchingDescription())
            .isEqualTo("<p>Legacy welders</p>");
        assertThat(reloaded.getLegacyRequirements()).isNull();
    }

    @Test
    @DisplayName("clearing the requirements of a legacy search clears them, rather than reverting to the legacy value")
    void legacySearch_clearingRequirementsClearsThem() {
        SavedSearch search = createLegacySearch("legacy-clear", null, "<p>Legacy welders</p>");

        updateSearch(search, "<p></p>", null);

        SavedSearch reloaded = reload(search);
        assertThat(reloaded.getRequirements()).isNull();
        assertThat(reloaded.getLegacyRequirements()).isNull();
    }

    @Test
    @DisplayName("a job search whose job has no Matching creates the job's Matching and shares it")
    void jobSearch_jobWithoutMatching_createsJobMatching() {
        SalesforceJobOpp job = createJob();

        SavedSearch search = createSearch("job-new", job.getId(), null);

        SavedSearch reloaded = reload(search);
        assertThat(reloaded.getMatching()).isNotNull();
        //No search requirements, so the job's own text is used.
        assertThat(reloaded.getMatching().getMatchingDescription()).isEqualTo(JOB_TEXT);
        assertThat(jobMatchingId(job)).isEqualTo(reloaded.getMatching().getId());
    }

    @Test
    @DisplayName("a legacy job search with no Matching gets the job's existing Matching when it is next updated")
    void legacyJobSearch_convergesOnJobMatching() {
        SalesforceJobOpp job = createJob();
        SavedSearch first = createSearch("job-first", job.getId(), "<p>Refined</p>");
        SavedSearch legacy = createLegacySearch("job-legacy", job.getId(), "<p>Old text</p>");

        updateSearch(legacy, "<p>Refined</p>", null);

        assertThat(reload(legacy).getMatching().getId())
            .isEqualTo(reload(first).getMatching().getId());
    }

    @Test
    @DisplayName("two searches associated with the same job resolve to the job's single Matching")
    void twoJobSearches_shareJobMatching() {
        SalesforceJobOpp job = createJob();

        SavedSearch first = createSearch("job-a", job.getId(), "<p>Refined</p>");
        SavedSearch second = createSearch("job-b", job.getId(), null);

        Long firstMatchingId = reload(first).getMatching().getId();
        assertThat(reload(second).getMatching().getId()).isEqualTo(firstMatchingId);
        assertThat(jobMatchingId(job)).isEqualTo(firstMatchingId);
        //Creating the second search without requirements did not clear the shared description.
        assertThat(reload(second).getRequirements()).isEqualTo("<p>Refined</p>");
    }

    @Test
    @DisplayName("editing a job search's requirements edits the job's shared Matching, not the job")
    void jobSearch_editingRequirementsEditsSharedMatching() {
        SalesforceJobOpp job = createJob();
        SavedSearch first = createSearch("job-edit-a", job.getId(), null);
        SavedSearch second = createSearch("job-edit-b", job.getId(), null);

        updateSearch(first, "<p>Refined after talking to employer</p>", null);

        SavedSearch reloadedFirst = reload(first);
        assertThat(reloadedFirst.getSfJobOpp().getId()).isEqualTo(job.getId());
        assertThat(reload(second).getRequirements())
            .isEqualTo("<p>Refined after talking to employer</p>");
        assertThat(salesforceJobOppRepository.findById(job.getId()).orElseThrow().getJobSummary())
            .isEqualTo(JOB_TEXT);
    }

    @Test
    @DisplayName("editing requirements changes the Matching's updatedDate, and resending them doesn't")
    void editingRequirements_updatedDateOnlyChangesOnRealChange() {
        SavedSearch search = createSearch("dates", null, "<p>Original</p>");
        Matching matching = reload(search).getMatching();
        var originalDate = matching.getUpdatedDate();

        updateSearch(search, "<p>Original</p>", null);
        assertThat(reload(search).getMatching().getUpdatedDate()).isEqualTo(originalDate);

        updateSearch(search, "<p>Changed</p>", null);
        assertThat(reload(search).getMatching().getUpdatedDate()).isAfterOrEqualTo(originalDate);
        assertThat(reload(search).getRequirements()).isEqualTo("<p>Changed</p>");
    }

    @Test
    @DisplayName("blank rich text clears a search's requirements, stored as no description")
    void blankRichText_clearsRequirements() {
        SavedSearch search = createSearch("blank", null, "<p>Original</p>");

        updateSearch(search, "<p>&nbsp;</p>", null);

        assertThat(reload(search).getMatching().getMatchingDescription()).isNull();
    }

    @Test
    @DisplayName("creating a job search with blank rich text requirements does not clear the job's Matching")
    void blankRichText_newJobSearchDoesNotClearJobMatching() {
        SalesforceJobOpp job = createJob();
        createSearch("job-text", job.getId(), "<p>Refined</p>");

        SavedSearch second = createSearch("job-blank", job.getId(), "<p></p>");

        assertThat(reload(second).getRequirements()).isEqualTo("<p>Refined</p>");
    }

    @Test
    @DisplayName("saving a job-related default search as a named search keeps the job's Matching, and resetting the default doesn't blank it")
    void jobDefaultSearch_savedAsNamedSearch_keepsJobMatching() {
        SalesforceJobOpp job = createJob();
        SavedSearch defaultSearch = savedSearchService.getDefaultSavedSearch();
        assignJob(defaultSearch, job.getId());
        runDefaultSearch(defaultSearch, "<p>Refined in default search</p>");
        Long jobMatchingId = reload(defaultSearch).getMatching().getId();

        CreateFromDefaultSavedSearchRequest request = new CreateFromDefaultSavedSearchRequest();
        request.setName("named-from-job-default");
        request.setJobId(job.getId());
        SavedSearch named = savedSearchService.createFromDefaultSavedSearch(request);

        SavedSearch reloadedNamed = reload(named);
        assertThat(reloadedNamed.getMatching().getId()).isEqualTo(jobMatchingId);
        assertThat(reloadedNamed.getRequirements()).isEqualTo("<p>Refined in default search</p>");
        //The default search was reset, but the shared Matching was not blanked.
        SavedSearch reloadedDefault = reload(defaultSearch);
        assertThat(reloadedDefault.getMatching().getId()).isEqualTo(jobMatchingId);
        assertThat(matchingRepository.findById(jobMatchingId).orElseThrow()
            .getMatchingDescription()).isEqualTo("<p>Refined in default search</p>");
    }

    @Test
    @DisplayName("saving a non-job default search gives the saved search its own Matching, unaffected by later default search changes")
    void nonJobDefaultSearch_savedAsNamedSearch_getsOwnMatching() {
        SavedSearch defaultSearch = savedSearchService.getDefaultSavedSearch();
        runDefaultSearch(defaultSearch, "<p>Welders</p>");
        Long defaultMatchingId = reload(defaultSearch).getMatching().getId();

        //As the UI does: create with the default search's current search request.
        SavedSearch named = createSearch("named-from-default", null, "<p>Welders</p>");

        SavedSearch reloadedNamed = reload(named);
        assertThat(reloadedNamed.getMatching().getId()).isNotEqualTo(defaultMatchingId);
        assertThat(reloadedNamed.getRequirements()).isEqualTo("<p>Welders</p>");

        //Resetting the default search dropped its working Matching rather than blanking it.
        assertThat(reload(defaultSearch).getMatching()).isNull();
        assertThat(matchingRepository.findById(defaultMatchingId).orElseThrow()
            .getMatchingDescription()).isEqualTo("<p>Welders</p>");

        runDefaultSearch(reload(defaultSearch), "<p>Something unrelated</p>");
        assertThat(reload(named).getRequirements()).isEqualTo("<p>Welders</p>");
    }

    @Test
    @DisplayName("assigning a job without a Matching to a named search keeps its requirements as the job's Matching")
    void assigningJobWithoutMatching_keepsSearchRequirements() {
        SavedSearch search = createSearch("assign-new", null, "<p>Refined welders</p>");
        SalesforceJobOpp job = createJob();

        assignJob(search, job.getId());

        SavedSearch reloaded = reload(search);
        assertThat(reloaded.getSfJobOpp().getId()).isEqualTo(job.getId());
        assertThat(reloaded.getRequirements()).isEqualTo("<p>Refined welders</p>");
        assertThat(jobMatchingId(job)).isEqualTo(reloaded.getMatching().getId());
    }

    @Test
    @DisplayName("assigning a job with a Matching to a search uses the job's Matching")
    void assigningJobWithMatching_usesJobMatching() {
        SalesforceJobOpp job = createJob();
        SavedSearch jobSearch = createSearch("assign-job", job.getId(), "<p>Job context</p>");
        SavedSearch search = createSearch("assign-other", null, "<p>Other</p>");

        assignJob(search, job.getId());

        assertThat(reload(search).getMatching().getId())
            .isEqualTo(reload(jobSearch).getMatching().getId());
        assertThat(reload(search).getRequirements()).isEqualTo("<p>Job context</p>");
    }

    @Test
    @DisplayName("changing the default search's job switches it to the new job's Matching, leaving the old job's unchanged")
    void defaultSearchJobChange_switchesToNewJobMatching() {
        SalesforceJobOpp jobJ = createJob();
        SalesforceJobOpp jobK = createJob();
        SavedSearch defaultSearch = savedSearchService.getDefaultSavedSearch();
        assignJob(defaultSearch, jobJ.getId());
        runDefaultSearch(defaultSearch, "<p>J refined</p>");

        assignJob(reload(defaultSearch), jobK.getId());
        runDefaultSearch(reload(defaultSearch), "<p>K refined</p>");

        assertThat(reload(defaultSearch).getMatching().getId()).isEqualTo(jobMatchingId(jobK));
        assertThat(matchingRepository.findById(jobMatchingId(jobJ)).orElseThrow()
            .getMatchingDescription()).isEqualTo("<p>J refined</p>");
        assertThat(matchingRepository.findById(jobMatchingId(jobK)).orElseThrow()
            .getMatchingDescription()).isEqualTo("<p>K refined</p>");
    }

    @Test
    @DisplayName("removing a search's job gives it its own copy of the requirements, so later edits don't change the job's")
    void removingJob_forksMatching() {
        SalesforceJobOpp job = createJob();
        SavedSearch search = createSearch("unassign", job.getId(), "<p>Job context</p>");

        assignJob(search, null);
        updateSearch(reload(search), "<p>No longer about the job</p>", null);

        SavedSearch reloaded = reload(search);
        assertThat(reloaded.getSfJobOpp()).isNull();
        assertThat(reloaded.getMatching().getId()).isNotEqualTo(jobMatchingId(job));
        assertThat(matchingRepository.findById(jobMatchingId(job)).orElseThrow()
            .getMatchingDescription()).isEqualTo("<p>Job context</p>");
    }

    @Test
    @DisplayName("editing requirements leaves the search's filters and job unchanged")
    void editingRequirements_leavesFiltersAndJobUnchanged() {
        SalesforceJobOpp job = createJob();
        SavedSearch search = createSearch("filters", job.getId(), "<p>Original</p>");

        SearchCandidateRequest request = searchRequest("<p>Changed</p>");
        request.setGender(Gender.female);
        request.setMinAge(25);
        update(search, request, null);

        SearchCandidateRequest loaded = savedSearchService.loadSavedSearch(search.getId());
        assertThat(loaded.getGender()).isEqualTo(Gender.female);
        assertThat(loaded.getMinAge()).isEqualTo(25);
        assertThat(loaded.getRequirements()).isEqualTo("<p>Changed</p>");
        assertThat(reload(search).getSfJobOpp().getId()).isEqualTo(job.getId());
    }

    private SalesforceJobOpp createJob() {
        SalesforceJobOpp job = TestDataFactory.createSalesforceJobOpportunity();
        job.setSfId("MATCHINGTEST" + System.nanoTime());
        job.setJobSummary(JOB_TEXT);
        job.setCreatedBy(user);
        return salesforceJobOppRepository.saveAndFlush(job);
    }

    private SavedSearch createSearch(String name, @Nullable Long jobId,
        @Nullable String requirements) {
        UpdateSavedSearchRequest request = new UpdateSavedSearchRequest();
        request.setName(name + "-" + System.nanoTime());
        request.setSavedSearchType(SavedSearchType.other);
        request.setJobId(jobId);
        request.setSearchCandidateRequest(searchRequest(requirements));
        return savedSearchService.createSavedSearch(request);
    }

    /**
     * Creates a search as it would have been stored before Matchings were introduced.
     */
    private SavedSearch createLegacySearch(String name, @Nullable Long jobId,
        String legacyRequirements) {
        SavedSearch search = createSearch(name, null, null);
        search.setLegacyRequirements(legacyRequirements);
        if (jobId != null) {
            search.setSfJobOpp(salesforceJobOppRepository.findById(jobId).orElseThrow());
        }
        search.setMatching(null);
        return savedSearchRepository.saveAndFlush(search);
    }

    private void updateSearch(SavedSearch search, @Nullable String requirements,
        @Nullable Long jobId) {
        update(search, searchRequest(requirements), jobId);
    }

    private void update(SavedSearch search, SearchCandidateRequest searchRequest,
        @Nullable Long jobId) {
        UpdateSavedSearchRequest request = new UpdateSavedSearchRequest();
        request.setName(search.getName());
        request.setSavedSearchType(SavedSearchType.other);
        request.setJobId(jobId);
        request.setSearchCandidateRequest(searchRequest);
        savedSearchService.updateSavedSearch(search.getId(), request);
    }

    private void assignJob(SavedSearch search, @Nullable Long jobId) {
        UpdateSavedSearchJobRequest request = new UpdateSavedSearchJobRequest();
        request.setJobId(jobId);
        savedSearchService.updateSavedSearchJob(search.getId(), request);
    }

    /**
     * Runs the default search, which saves its search request - as the UI does.
     */
    private void runDefaultSearch(SavedSearch defaultSearch, String requirements) {
        SearchCandidateRequest request = searchRequest(requirements);
        request.setSavedSearchId(defaultSearch.getId());
        savedSearchService.updateUserDefaultSavedSearchIfNeeded(request);
    }

    private static SearchCandidateRequest searchRequest(@Nullable String requirements) {
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setRequirements(requirements);
        return request;
    }

    private SavedSearch reload(SavedSearch search) {
        entityManager.flush();
        entityManager.clear();
        return savedSearchRepository.findById(search.getId()).orElseThrow();
    }

    private Long jobMatchingId(SalesforceJobOpp job) {
        entityManager.flush();
        return salesforceJobOppRepository.findMatchingIdByJobId(job.getId()).orElse(null);
    }
}
