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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateSavedList;
import org.tbbtalent.server.model.db.Nationality;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CandidateSavedListRepository;
import org.tbbtalent.server.repository.db.GetSavedListCandidatesQuery;
import org.tbbtalent.server.repository.db.NationalityRepository;
import org.tbbtalent.server.repository.db.SavedListRepository;
import org.tbbtalent.server.repository.db.UserRepository;
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
    private NationalityRepository nationalityRepository;
    
    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateSavedListRepository candidateSavedListRepository;
    
    @Autowired
    private SavedListRepository savedListRepository;

    @Autowired
    UserRepository userRepository;

    private User owningUser;
    private SavedList savedList;
    private Nationality nationality1;
    
    @BeforeEach
    void setUpListAndOwningUser() {
        assertNotNull(savedListRepository);
        assertNotNull(userRepository);
        
        //Set up a nationality.
        nationality1 = new Nationality();
        nationality1.setName("Syrian");
        nationalityRepository.save(nationality1);

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

    private static boolean flip = true;

    private Candidate createTestCandidate(String username, String firstName, String lastName, String email) {
        User candidateUser = createTestUser(username, firstName, lastName, email);
        Candidate candidate = new Candidate();
        candidate.setUser(candidateUser);
        if (flip) {
            candidate.setNationality(nationality1);
        }
        flip = !flip;
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

    @Transactional
    @Test
    void getCandidateList() {

        int totalCandidates = 10;
        Set<Candidate> candidates = createNCandidates(totalCandidates);
        for (Candidate candidate : candidates) {
            final CandidateSavedList csl = new CandidateSavedList(candidate, savedList);
            candidateSavedListRepository.save(csl);
        }
        savedListRepository.save(savedList);

        SavedListGetRequest request;
        request = new SavedListGetRequest();
        request.setSortFields(new String[] {"nationality.name"});
        request.setSortDirection(Sort.Direction.DESC);
        
        request.setPageSize(4);
        
        int expectedNPages = 3; 

        for (int pageNumber = 0; pageNumber < expectedNPages; pageNumber++) {
            request.setPageNumber(pageNumber);

            PageRequest pageRequest = request.getPageRequestWithoutSort();
            Page<Candidate> candidatesPage = candidateRepository.findAll(
                    new GetSavedListCandidatesQuery(savedList.getId(), request), pageRequest);

            assertNotNull(candidatesPage);
            assertEquals(totalCandidates, candidatesPage.getTotalElements());

            List<Candidate> pageOfCandidates = candidatesPage.getContent();
            for (Candidate candidate : pageOfCandidates) {
                assertTrue(candidates.contains(candidate));
            }
        }
    }
}