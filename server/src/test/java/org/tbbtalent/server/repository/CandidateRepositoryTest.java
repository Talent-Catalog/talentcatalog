/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.candidate.SavedListGetRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class CandidateRepositoryTest {
    
    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private SavedListRepository savedListRepository;

    @Autowired
    UserRepository userRepository;

    private User owningUser;
    private SavedList savedList;

    @BeforeEach
    void setUpListAndOwningUser() {
        assertNotNull(savedListRepository);
        assertNotNull(userRepository);

        //Set up owning user
        owningUser = new User(
                "username", "first", "last",
                "email@test.com", Role.admin);
        owningUser.setPasswordEnc("xxxx");
        userRepository.save(owningUser);
        assertNotNull(owningUser);

        //Create a test list
        savedList = new SavedList();
        savedList.setName("TestList");
        savedList.setAuditFields(owningUser);

        savedListRepository.save(savedList);
        assertNotNull(savedList);
    }

    private Candidate createTestCandidate(String username, String firstName, String lastName, String email) {
        User candidateUser = createTestUser(username, firstName, lastName, email);
        Candidate candidate = new Candidate();
        candidate.setUser(candidateUser);
        return candidateRepository.save(candidate);
    }

    private Set<Candidate> createNCandidates(int n) {
        String username = "username";
        String firstName = "firstName";
        String lastName = "lastName";
        String email = "email@x.com";
        
        Set<Candidate> candidates = new HashSet<>();

        for (int i = 0; i < n; i++) {
            Candidate c = createTestCandidate(n+username, 
                    n+firstName,n+lastName, n+email);
            candidates.add(c);
        }
        
        return candidates;
    }

    private User createTestUser(String username, String firstName, String lastName, String email) {
        User user = new User(
                username, firstName, lastName,
                email, Role.user);
        user.setPasswordEnc("xxxx");
        userRepository.save(user);
        return user;
    }

    @Test
    void getCandidateList() {

        int totalCandidates = 10;
        Set<Candidate> candidates = createNCandidates(totalCandidates);
        savedList.setCandidates(candidates);
        savedListRepository.save(savedList);

        SavedListGetRequest request;
        request = new SavedListGetRequest();
        request.setSavedListId(savedList.getId());
        request.setPageSize(4);
        
        int expectedNPages = 3; 

        for (int pageNumber = 0; pageNumber < expectedNPages; pageNumber++) {
            request.setPageNumber(pageNumber);

            Page<Candidate> candidatesPage = candidateRepository.findAll(
                    CandidateListSpecification.buildSearchQuery(request), request.getPageRequestWithoutSort());

            assertNotNull(candidatesPage);
            assertEquals(candidatesPage.getTotalElements(), totalCandidates);

            List<Candidate> pageOfCandidates = candidatesPage.getContent();
            for (Candidate candidate : pageOfCandidates) {
                assertTrue(candidates.contains(candidate));
            }
        }
    }
}