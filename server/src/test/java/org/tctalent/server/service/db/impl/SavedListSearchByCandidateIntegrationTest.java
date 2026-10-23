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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;

/**
 * Integration tests of {@link SavedListService#search(long, SearchSavedListRequest)} which
 * combines the request's saved list filters with a filter on the lists containing a candidate.
 * <p>
 * Both filters must apply - ie the results are the lists that match the request AND contain
 * the candidate.
 */
@SpringBootTest
@Transactional
class SavedListSearchByCandidateIntegrationTest extends BaseDBIntegrationTest {

    @Autowired private SavedListService savedListService;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EntityManager entityManager;

    //Same mocks as other saved list integration tests so that the Spring context is shared.
    @MockitoBean private CandidateRedisCache candidateRedisCache;
    @MockitoBean private SalesforceService salesforceService;

    /**
     * Unique to each test so that only lists created by the test match the keyword.
     */
    private String token;
    private Candidate candidate;
    private SavedList alphaWithCandidate;
    private SavedList betaWithCandidate;
    private SavedList alphaWithoutCandidate;

    @BeforeEach
    void setUp() {
        token = "bycandidate" + System.nanoTime();

        User user = TestDataFactory.createUser(null);
        user.setUsername(token);
        user.setEmail(token + "@example.com");
        logIn(userRepository.saveAndFlush(user));

        candidate = createCandidate("in-lists");
        Candidate otherCandidate = createCandidate("other");

        alphaWithCandidate = createList("alpha-" + token + "-1", candidate);
        betaWithCandidate = createList("beta-" + token, candidate);
        alphaWithoutCandidate = createList("alpha-" + token + "-2", otherCandidate);

        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("returns only lists that match the request filters and contain the candidate")
    void returnsListsMatchingRequestAndContainingCandidate() {
        SearchSavedListRequest request = new SearchSavedListRequest();
        request.setKeyword("alpha-" + token);
        request.setOwned(true);

        List<SavedList> results = savedListService.search(candidate.getId(), request);

        //beta is excluded by the keyword, alpha-2 because it does not contain the candidate
        assertThat(results).extracting(SavedList::getId)
            .containsExactly(alphaWithCandidate.getId());
    }

    @Test
    @DisplayName("returns all the candidate's lists matching the request, sorted by name")
    void returnsAllCandidateListsMatchingRequestSortedByName() {
        SearchSavedListRequest request = new SearchSavedListRequest();
        request.setKeyword(token);
        request.setOwned(true);

        List<SavedList> results = savedListService.search(candidate.getId(), request);

        assertThat(results).extracting(SavedList::getId)
            .containsExactly(alphaWithCandidate.getId(), betaWithCandidate.getId())
            .doesNotContain(alphaWithoutCandidate.getId());
    }

    @Test
    @DisplayName("returns no lists for a candidate who is not in any list")
    void returnsNoListsForCandidateNotInAnyList() {
        Candidate notInAnyList = createCandidate("not-in-lists");
        SearchSavedListRequest request = new SearchSavedListRequest();
        request.setKeyword(token);
        request.setOwned(true);

        assertThat(savedListService.search(notInAnyList.getId(), request)).isEmpty();
    }

    private void logIn(User user) {
        TcUserDetails userDetails = new TcUserDetails(user);
        userDetails.setAuthorities(List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(
            new UsernamePasswordAuthenticationToken(userDetails, null, List.of()));
        SecurityContextHolder.setContext(securityContext);
    }

    private Candidate createCandidate(String name) {
        User candidateUser = TestDataFactory.createUser(null);
        String userToken = name + "-" + System.nanoTime();
        candidateUser.setUsername(userToken);
        candidateUser.setEmail(userToken + "@example.com");
        return TestDataFactory.createAndSaveCandidate(
            candidateRepository, userRepository.saveAndFlush(candidateUser));
    }

    private SavedList createList(String name, Candidate member) {
        UpdateSavedListInfoRequest request = new UpdateSavedListInfoRequest();
        request.setName(name);
        SavedList list = savedListService.createSavedList(request);
        savedListService.addCandidatesToList(list, Set.of(member), null);
        return savedListService.saveIt(list);
    }
}
