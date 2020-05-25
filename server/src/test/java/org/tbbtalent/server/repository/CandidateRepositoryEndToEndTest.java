/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.candidate.SavedListGetRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CandidateRepositoryEndToEndTest {
    private static final Logger log = LoggerFactory.getLogger(CandidateRepositoryEndToEndTest.class);
    
    @Autowired
    private NationalityRepository nationalityRepository;
    
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

        User owningUser = userRepository.findByUsernameIgnoreCase("camerojox");
        if (owningUser == null) {
            //Create test user if it doesn't already exist.
            //Usually it won't because transactional tests are automatically
            //rolled back.
            owningUser = new User(
                    "camerojox", "TestFirst", "TestLast",
                    "Test@x.com", Role.user);
            owningUser.setPasswordEnc("xxxx");
            owningUser.setReadOnly(false);
            userRepository.save(owningUser);
        }

        //Create a test list if it is not already there
        String listName = "TestList";
        savedList = savedListRepository.findByNameIgnoreCase(listName)
                .orElse(null);

        if (savedList == null) {
            savedList = new SavedList();
            savedList.setName("TestList");
            savedList.setAuditFields(owningUser);

            savedListRepository.save(savedList);
        } else {
            savedList = savedListRepository.findByIdLoadCandidates(savedList.getId())
            .orElse(null);
        }
        assertNotNull(savedList);
    }

    //@Transactional
    //@Test
    void testCandidateListSort() {
        Set<Candidate> candidates = fetchTestCandidates(20);
        assertThat(candidates).isNotNull();
        int totalCandidates = candidates.size();
        
        savedList.setCandidates(candidates);
        
        savedListRepository.save(savedList);

        SavedListGetRequest request;
        request = new SavedListGetRequest();
        request.setSortFields(new String[] {"nationality.name"});
        request.setSortDirection(Sort.Direction.ASC);
        
        request.setSavedListId(savedList.getId());
        request.setPageSize(10);
        request.setPageNumber(0);

        PageRequest pageRequest = request.getPageRequestWithoutSort();
        Page<Candidate> candidatesPage = candidateRepository.findAll(
                new CandidateListGetQuery(request), pageRequest);

        assertNotNull(candidatesPage);
        assertEquals(totalCandidates, candidatesPage.getTotalElements());

        List<Candidate> pageOfCandidates = candidatesPage.getContent();
        assertThat(pageOfCandidates).isNotNull();
        
        for (Candidate candidate : pageOfCandidates) {
            User user = candidate.getUser();
            log.info((user == null ? "?" : (user.getFirstName() + " " + user.getLastName())) + " " 
                    + candidate.getNationality().getName() + " " + candidate.getCountry().getName());
        }
    }

    private Set<Candidate> fetchTestCandidates(int n) {
        List<Candidate> candidates = candidateRepository.findAll();
        Set<Candidate> candidateSet = new HashSet<>();
        for (int i = 0; i < n; i++) {
            Candidate candidate;
            candidate = candidates.get(i);
            
            candidate = candidateRepository.findByIdLoadSavedLists(candidate.getId());
            candidateSet.add(candidate);
        }
        return candidateSet;
    }
}