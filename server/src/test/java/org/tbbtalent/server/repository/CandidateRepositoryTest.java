/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.repository.db.*;
import org.tbbtalent.server.request.candidate.SavedListGetRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class CandidateRepositoryTest {

    @Autowired
    private CountryRepository countryRepository;
    
    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private SavedListRepository savedListRepository;

    @Autowired
    UserRepository userRepository;

    private User owningUser;
    private SavedList savedList;
    private Country nationality1;
    
    @BeforeEach
    void setUpListAndOwningUser() {
        assertNotNull(savedListRepository);
        assertNotNull(userRepository);
        
        //Set up a nationality.
        nationality1 = new Country();
        nationality1.setName("Syria");
        countryRepository.save(nationality1);

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
        savedList.addCandidates(candidates);
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
