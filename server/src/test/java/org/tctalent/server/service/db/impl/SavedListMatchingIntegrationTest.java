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
import java.util.Set;
import java.util.stream.Collectors;
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
import org.tctalent.server.api.admin.SavedListCandidateAdminApi;
import org.tctalent.server.api.admin.SavedSearchAdminApi;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Matching;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.SavedSearchType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.MatchingRepository;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.SavedSearchRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tctalent.server.request.list.ContentUpdateType;
import org.tctalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.request.search.UpdateSavedSearchJobRequest;
import org.tctalent.server.request.search.UpdateSavedSearchRequest;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;

/**
 * Integration tests of the Matching (and job) context of saved lists when candidates are saved
 * into them - see {@link SavedListService#inheritMatching}.
 * <p>
 * Candidates are saved through the same API methods the UI uses:
 * <ul>
 *     <li>
 *         {@link SavedSearchAdminApi#saveSelection} - a search's selected candidates
 *     </li>
 *     <li>
 *         {@link SavedListCandidateAdminApi#saveSelection} and
 *         {@link SavedListCandidateAdminApi#create} - a matching search's results, or a list's
 *         selected candidates
 *     </li>
 * </ul>
 */
@SpringBootTest
@Transactional
class SavedListMatchingIntegrationTest extends BaseDBIntegrationTest {

    private static final String JOB_TEXT = "<p>Job summary: ICU nurses</p>";

    @Autowired private SavedSearchAdminApi savedSearchAdminApi;
    @Autowired private SavedListCandidateAdminApi savedListCandidateAdminApi;
    @Autowired private SavedSearchService savedSearchService;
    @Autowired private SavedListService savedListService;
    @Autowired private SavedSearchRepository savedSearchRepository;
    @Autowired private SavedListRepository savedListRepository;
    @Autowired private SalesforceJobOppRepository salesforceJobOppRepository;
    @Autowired private MatchingRepository matchingRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EntityManager entityManager;

    @MockitoBean private CandidateRedisCache candidateRedisCache;

    private User user;
    private Candidate candidate;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.createUser(null);
        String token = "list-matching-" + System.nanoTime();
        user.setUsername(token);
        user.setEmail(token + "@example.com");
        user = userRepository.saveAndFlush(user);
        //A new context rather than modifying the current one, which may be left over from
        //an earlier test on this thread.
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(
            new UsernamePasswordAuthenticationToken(new TcUserDetails(user), null, List.of()));
        SecurityContextHolder.setContext(securityContext);

        candidate = createCandidate("candidate");
    }

    private Candidate createCandidate(String name) {
        User candidateUser = TestDataFactory.createUser(null);
        String token = name + "-" + System.nanoTime();
        candidateUser.setUsername(token);
        candidateUser.setEmail(token + "@example.com");
        return TestDataFactory.createAndSaveCandidate(
            candidateRepository, userRepository.saveAndFlush(candidateUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("default job search selection -> existing list with no context: list gets the job's Matching but keeps no job")
    void defaultJobSearchSelection_toExistingList_inheritsJobMatching() {
        SalesforceJobOpp job = createJob();
        SavedSearch defaultSearch = defaultJobSearch(job, "<p>Refined</p>");
        SavedList list = createList("existing", null);

        saveSearchSelection(defaultSearch, list.getId(), false);

        SavedList reloaded = reload(list);
        assertThat(reloaded.getMatching().getId()).isEqualTo(jobMatchingId(job));
        assertThat(reloaded.getSfJobOpp()).isNull();
        assertThat(reloaded.getCandidates()).extracting(Candidate::getId)
            .contains(candidate.getId());
        //No second Matching was created for the job
        assertThat(matchingDescription(reloaded)).isEqualTo("<p>Refined</p>");
    }

    @Test
    @DisplayName("default job matching search results -> existing list with no context: list gets the job's Matching")
    void defaultJobMatchingSearchResults_toExistingList_inheritsJobMatching() {
        SalesforceJobOpp job = createJob();
        SavedSearch defaultSearch = defaultJobSearch(job, "<p>Refined</p>");
        SavedList list = createList("existing-matching", null);

        saveSearchResults(defaultSearch, list.getId(), false);

        SavedList reloaded = reload(list);
        assertThat(reloaded.getMatching().getId()).isEqualTo(jobMatchingId(job));
        assertThat(reloaded.getSfJobOpp()).isNull();
        assertThat(reloaded.getCandidates()).extracting(Candidate::getId)
            .contains(candidate.getId());
    }

    @Test
    @DisplayName("default job search selection -> new list: list gets the job and the job's Matching")
    void defaultJobSearchSelection_toNewList_getsJobAndMatching() {
        SalesforceJobOpp job = createJob();
        SavedSearch defaultSearch = defaultJobSearch(job, "<p>Refined</p>");

        //As the UI does: the save dialog presets the new list's job to the search's job.
        CopySourceContentsRequest request = new CopySourceContentsRequest();
        request.setSavedListId(0L);
        request.setNewListName("new-from-selection-" + System.nanoTime());
        request.setJobId(job.getId());
        request.setUpdateType(ContentUpdateType.add);
        long listId = (Long) savedSearchAdminApi.saveSelection(defaultSearch.getId(), request)
            .get("id");

        SavedList reloaded = reloadList(listId);
        assertThat(reloaded.getSfJobOpp().getId()).isEqualTo(job.getId());
        assertThat(reloaded.getMatching().getId()).isEqualTo(jobMatchingId(job));
    }

    @Test
    @DisplayName("default job matching search results -> new list: list gets the job and the job's Matching")
    void defaultJobMatchingSearchResults_toNewList_getsJobAndMatching() {
        SalesforceJobOpp job = createJob();
        SavedSearch defaultSearch = defaultJobSearch(job, "<p>Refined</p>");

        UpdateExplicitSavedListContentsRequest request = resultsRequest(defaultSearch, false);
        request.setName("new-from-results-" + System.nanoTime());
        request.setJobId(job.getId());
        long listId = (Long) savedListCandidateAdminApi.create(request).get("id");

        SavedList reloaded = reloadList(listId);
        assertThat(reloaded.getSfJobOpp().getId()).isEqualTo(job.getId());
        assertThat(reloaded.getMatching().getId()).isEqualTo(jobMatchingId(job));
        assertThat(reloaded.getCandidates()).extracting(Candidate::getId)
            .contains(candidate.getId());
    }

    @Test
    @DisplayName("named job search -> list with no context: list gets the job's Matching")
    void namedJobSearch_toListWithoutContext_inheritsJobMatching() {
        SalesforceJobOpp job = createJob();
        SavedSearch search = createSearch("named-job", job.getId(), "<p>Named job text</p>");
        selectCandidate(search);
        SavedList list = createList("for-named", null);

        saveSearchSelection(search, list.getId(), false);

        assertThat(reload(list).getMatching().getId()).isEqualTo(jobMatchingId(job));
    }

    @Test
    @DisplayName("list already has the source's Matching: no change")
    void listWithSameMatching_unchanged() {
        SalesforceJobOpp job = createJob();
        SavedSearch defaultSearch = defaultJobSearch(job, "<p>Refined</p>");
        SavedList list = createList("same", null);
        saveSearchSelection(defaultSearch, list.getId(), false);
        long matchingCount = matchingRepository.count();

        saveSearchSelection(reloadSearch(defaultSearch), list.getId(), false);

        assertThat(reload(list).getMatching().getId()).isEqualTo(jobMatchingId(job));
        assertThat(matchingRepository.count()).isEqualTo(matchingCount);
    }

    @Test
    @DisplayName("list with a different job and Matching: neither is overwritten")
    void listWithDifferentJobAndMatching_notOverwritten() {
        SalesforceJobOpp jobA = createJob();
        SalesforceJobOpp jobB = createJob();
        SavedList list = createList("job-a", jobA.getId());
        //Give the list job A's Matching
        saveSearchSelection(defaultJobSearch(jobA, "<p>A text</p>"), list.getId(), false);
        Long matchingA = jobMatchingId(jobA);
        assertThat(reload(list).getMatching().getId()).isEqualTo(matchingA);

        saveSearchSelection(defaultJobSearch(jobB, "<p>B text</p>"), list.getId(), false);

        SavedList reloaded = reload(list);
        assertThat(reloaded.getSfJobOpp().getId()).isEqualTo(jobA.getId());
        assertThat(reloaded.getMatching().getId()).isEqualTo(matchingA);
    }

    @Test
    @DisplayName("list with a Matching but no job: keeps its Matching, and its job is unchanged")
    void listWithMatchingNoJob_keepsMatching() {
        SavedList list = createList("own-matching", null);
        Matching own = matchingRepository.saveAndFlush(new Matching("<p>List's own</p>"));
        list.setMatching(own);
        savedListRepository.saveAndFlush(list);
        SalesforceJobOpp job = createJob();

        saveSearchSelection(defaultJobSearch(job, "<p>Job text</p>"), list.getId(), false);

        SavedList reloaded = reload(list);
        assertThat(reloaded.getMatching().getId()).isEqualTo(own.getId());
        assertThat(reloaded.getSfJobOpp()).isNull();
    }

    @Test
    @DisplayName("list with a job but no Matching: gets its own job's Matching, not the unrelated source Matching")
    void listWithJobNoMatching_getsOwnJobMatching() {
        SalesforceJobOpp listJob = createJob();
        SalesforceJobOpp sourceJob = createJob();
        SavedList list = createList("job-only", listJob.getId());

        saveSearchSelection(defaultJobSearch(sourceJob, "<p>Source job text</p>"),
            list.getId(), false);

        SavedList reloaded = reload(list);
        assertThat(reloaded.getSfJobOpp().getId()).isEqualTo(listJob.getId());
        assertThat(reloaded.getMatching().getId()).isEqualTo(jobMatchingId(listJob));
        assertThat(reloaded.getMatching().getId()).isNotEqualTo(jobMatchingId(sourceJob));
        assertThat(matchingDescription(reloaded)).isEqualTo(JOB_TEXT);
    }

    @Test
    @DisplayName("named non-job search with a Matching -> list with no Matching: list shares the search's Matching")
    void namedNonJobSearch_toList_sharesMatching() {
        SavedSearch search = createSearch("named-plain", null, "<p>Welders</p>");
        selectCandidate(search);
        SavedList list = createList("for-plain", null);

        saveSearchSelection(search, list.getId(), false);

        assertThat(reload(list).getMatching().getId())
            .isEqualTo(reloadSearch(search).getMatching().getId());
    }

    @Test
    @DisplayName("non-job default search -> list: list gets its own copy of the working Matching")
    void nonJobDefaultSearch_toList_getsOwnCopy() {
        SavedSearch defaultSearch = savedSearchService.getDefaultSavedSearch();
        runDefaultSearch(defaultSearch, "<p>Exploring welders</p>");
        selectCandidate(defaultSearch);
        SavedList list = createList("from-default", null);

        saveSearchSelection(defaultSearch, list.getId(), false);

        SavedList reloaded = reload(list);
        Long defaultMatchingId = reloadSearch(defaultSearch).getMatching().getId();
        assertThat(reloaded.getMatching().getId()).isNotEqualTo(defaultMatchingId);
        assertThat(matchingDescription(reloaded)).isEqualTo("<p>Exploring welders</p>");

        //Later exploration in the default search doesn't change the list's context.
        runDefaultSearch(reloadSearch(defaultSearch), "<p>Something else</p>");
        assertThat(matchingDescription(reload(list))).isEqualTo("<p>Exploring welders</p>");
    }

    @Test
    @DisplayName("source with no job or Matching: list context unchanged")
    void sourceWithoutContext_listUnchanged() {
        SavedSearch search = createSearch("no-context", null, null);
        selectCandidate(search);
        SavedList emptyContextList = createList("stays-empty", null);
        SavedList ownMatchingList = createList("keeps-own", null);
        Matching own = matchingRepository.saveAndFlush(new Matching("<p>Own</p>"));
        ownMatchingList.setMatching(own);
        savedListRepository.saveAndFlush(ownMatchingList);
        long matchingCount = matchingRepository.count();

        saveSearchSelection(search, emptyContextList.getId(), false);
        saveSearchSelection(reloadSearch(search), ownMatchingList.getId(), false);

        assertThat(reload(emptyContextList).getMatching()).isNull();
        assertThat(reload(ownMatchingList).getMatching().getId()).isEqualTo(own.getId());
        assertThat(matchingRepository.count()).isEqualTo(matchingCount);
    }

    @Test
    @DisplayName("replacing a list's contents keeps its established job and Matching")
    void replacingContents_keepsContext() {
        SalesforceJobOpp jobA = createJob();
        SalesforceJobOpp jobB = createJob();
        SavedList list = createList("replace", jobA.getId());
        saveSearchSelection(defaultJobSearch(jobA, "<p>A</p>"), list.getId(), false);
        Long matchingA = reload(list).getMatching().getId();

        //Replace with a different candidate. (Replacing a list's contents with a candidate
        //already in it fails within a single persistence context - unrelated to Matching.)
        Candidate other = createCandidate("other");
        SavedSearch defaultSearchB = defaultJobSearch(jobB, "<p>B</p>");
        savedSearchService.clearSelection(defaultSearchB.getId(), user.getId());
        selectCandidate(defaultSearchB, other);
        saveSearchSelection(reloadSearch(defaultSearchB), list.getId(), true);
        saveSearchResults(reloadSearch(defaultSearchB), list.getId(), true, Set.of(other));

        SavedList reloaded = reload(list);
        assertThat(reloaded.getSfJobOpp().getId()).isEqualTo(jobA.getId());
        assertThat(reloaded.getMatching().getId()).isEqualTo(matchingA);
    }

    @Test
    @DisplayName("merging from another list fills in a missing Matching, and only that")
    void mergingFromList_fillsMissingMatchingOnly() {
        SalesforceJobOpp job = createJob();
        SavedList source = createList("source", null);
        saveSearchSelection(defaultJobSearch(job, "<p>Job context</p>"), source.getId(), false);
        SavedList destination = createList("destination", null);

        UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();
        request.setSourceListId(source.getId());
        request.setCandidateIds(Set.of(candidate.getId()));
        request.setUpdateType(ContentUpdateType.add);
        savedListCandidateAdminApi.merge(destination.getId(), request);

        SavedList reloaded = reload(destination);
        assertThat(reloaded.getMatching().getId()).isEqualTo(jobMatchingId(job));
        assertThat(reloaded.getSfJobOpp()).isNull();
    }

    @Test
    @DisplayName("a selection list follows its search's job and Matching")
    void selectionList_followsSearch() {
        SalesforceJobOpp jobJ = createJob();
        SalesforceJobOpp jobK = createJob();
        SavedSearch defaultSearch = defaultJobSearch(jobJ, "<p>J</p>");

        SavedList selection = savedSearchService.getSelectionListForLoggedInUser(
            defaultSearch.getId());
        assertThat(selection.getSfJobOpp().getId()).isEqualTo(jobJ.getId());
        assertThat(selection.getMatching().getId()).isEqualTo(jobMatchingId(jobJ));

        assignJob(reloadSearch(defaultSearch), jobK.getId());
        runDefaultSearch(reloadSearch(defaultSearch), "<p>K</p>");
        entityManager.flush();
        entityManager.clear();

        selection = savedSearchService.getSelectionListForLoggedInUser(defaultSearch.getId());
        assertThat(selection.getSfJobOpp().getId()).isEqualTo(jobK.getId());
        assertThat(selection.getMatching().getId()).isEqualTo(jobMatchingId(jobK));
    }

    @Test
    @DisplayName("a legacy list with no Matching remains valid, and reading it creates nothing")
    void legacyList_readingHasNoSideEffect() {
        SalesforceJobOpp job = createJob();
        SavedList list = createList("legacy", job.getId());
        entityManager.flush();
        entityManager.clear();
        long matchingCount = matchingRepository.count();

        SavedList read = savedListService.get(list.getId());

        assertThat(read.getMatching()).isNull();
        assertThat(read.getSfJobOpp().getId()).isEqualTo(job.getId());
        assertThat(matchingRepository.count()).isEqualTo(matchingCount);
        assertThat(jobMatchingId(job)).isNull();
    }

    private SalesforceJobOpp createJob() {
        SalesforceJobOpp job = TestDataFactory.createSalesforceJobOpportunity();
        job.setSfId("LISTMATCHING" + System.nanoTime());
        job.setJobSummary(JOB_TEXT);
        job.setCreatedBy(user);
        return salesforceJobOppRepository.saveAndFlush(job);
    }

    /**
     * The user's default search, associated with the given job and run with the given
     * requirements, with the candidate selected - as the UI does with ?job=.
     */
    private SavedSearch defaultJobSearch(SalesforceJobOpp job, String requirements) {
        SavedSearch defaultSearch = savedSearchService.getDefaultSavedSearch();
        assignJob(defaultSearch, job.getId());
        runDefaultSearch(reloadSearch(defaultSearch), requirements);
        selectCandidate(defaultSearch);
        return reloadSearch(defaultSearch);
    }

    private SavedSearch createSearch(String name, @Nullable Long jobId,
        @Nullable String requirements) {
        UpdateSavedSearchRequest request = new UpdateSavedSearchRequest();
        request.setName(name + "-" + System.nanoTime());
        request.setSavedSearchType(SavedSearchType.other);
        request.setJobId(jobId);
        SearchCandidateRequest searchRequest = new SearchCandidateRequest();
        searchRequest.setRequirements(requirements);
        request.setSearchCandidateRequest(searchRequest);
        return savedSearchService.createSavedSearch(request);
    }

    private void assignJob(SavedSearch search, Long jobId) {
        UpdateSavedSearchJobRequest request = new UpdateSavedSearchJobRequest();
        request.setJobId(jobId);
        savedSearchService.updateSavedSearchJob(search.getId(), request);
    }

    private void runDefaultSearch(SavedSearch defaultSearch, String requirements) {
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setRequirements(requirements);
        request.setSavedSearchId(defaultSearch.getId());
        savedSearchService.updateUserDefaultSavedSearchIfNeeded(request);
    }

    private void selectCandidate(SavedSearch search) {
        selectCandidate(search, candidate);
    }

    private void selectCandidate(SavedSearch search, Candidate selected) {
        SavedList selection = savedSearchService.getSelectionListForLoggedInUser(search.getId());
        savedListService.addCandidatesToList(selection, Set.of(selected), null);
        savedListService.saveIt(selection);
    }

    private SavedList createList(String name, @Nullable Long jobId) {
        UpdateSavedListInfoRequest request = new UpdateSavedListInfoRequest();
        request.setName(name + "-" + System.nanoTime());
        request.setJobId(jobId);
        return savedListService.createSavedList(request);
    }

    /**
     * Saves the search's selected candidates to the list - as the UI does for a search.
     */
    private void saveSearchSelection(SavedSearch search, long listId, boolean replace) {
        newRequest();
        CopySourceContentsRequest request = new CopySourceContentsRequest();
        request.setSavedListId(listId);
        request.setUpdateType(replace ? ContentUpdateType.replace : ContentUpdateType.add);
        savedSearchAdminApi.saveSelection(search.getId(), request);
    }

    /**
     * Saves the results of a matching search to the list - as the UI does for a search with
     * requirements.
     */
    private void saveSearchResults(SavedSearch search, long listId, boolean replace) {
        saveSearchResults(search, listId, replace, Set.of(candidate));
    }

    private void saveSearchResults(SavedSearch search, long listId, boolean replace,
        Set<Candidate> results) {
        newRequest();
        UpdateExplicitSavedListContentsRequest request = resultsRequest(search, replace);
        request.setCandidateIds(
            results.stream().map(Candidate::getId).collect(Collectors.toSet()));
        savedListCandidateAdminApi.saveSelection(listId, request);
    }

    private UpdateExplicitSavedListContentsRequest resultsRequest(
        SavedSearch search, boolean replace) {
        UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();
        request.setSourceSearchId(search.getId());
        request.setCandidateIds(Set.of(candidate.getId()));
        request.setUpdateType(replace ? ContentUpdateType.replace : ContentUpdateType.add);
        return request;
    }

    /**
     * Each save is a separate request in the app, with its own persistence context.
     */
    private void newRequest() {
        entityManager.flush();
        entityManager.clear();
    }

    private SavedList reload(SavedList list) {
        return reloadList(list.getId());
    }

    private SavedList reloadList(long listId) {
        entityManager.flush();
        entityManager.clear();
        return savedListRepository.findByIdLoadCandidates(listId).orElseThrow();
    }

    private SavedSearch reloadSearch(SavedSearch search) {
        entityManager.flush();
        entityManager.clear();
        return savedSearchRepository.findById(search.getId()).orElseThrow();
    }

    private Long jobMatchingId(SalesforceJobOpp job) {
        entityManager.flush();
        return salesforceJobOppRepository.findMatchingIdByJobId(job.getId()).orElse(null);
    }

    private String matchingDescription(SavedList list) {
        return matchingRepository.findById(list.getMatching().getId()).orElseThrow()
            .getMatchingDescription();
    }
}
