/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class SavedListRepositoryTest {
    
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
        User candidateUser = new User(
                username, firstName, lastName,
                email, Role.user);
        candidateUser.setPasswordEnc("xxxx");
        userRepository.save(candidateUser);
        Candidate candidate = new Candidate();
        candidate.setUser(candidateUser);
        return candidateRepository.save(candidate);
    }
    
    @Test
    void testCandidateAdditionRemoval() {
        //Set up a couple of candidates
        Candidate candidate1 = createTestCandidate(
                "c1username", "c1first", "c1last",
                "c1email@test.com");
        Candidate candidate2 = createTestCandidate(
                "c2username", "c2first", "c2last",
                "c2email@test.com");

        //Retrieve list from database
        SavedList listFromId = savedListRepository.findByIdLoadCandidates(savedList.getId());
        assertNotNull(listFromId);
        assertNotNull(listFromId.getCandidates());

        //Check that list of candidates is currently empty.
        assertEquals(0, listFromId.getCandidates().size());

        //Add candidates to the list
        Set<Candidate> candidates = new HashSet<>();
        candidates.add(candidate1);
        candidates.add(candidate2);
        savedList.setCandidates(candidates);

        //Retrieve list from database
        listFromId = savedListRepository.findByIdLoadCandidates(savedList.getId());
        assertNotNull(listFromId);
        assertNotNull(listFromId.getCandidates());
        //Check both candidates are in there.
        assertEquals(2, listFromId.getCandidates().size());
        assertTrue(listFromId.getCandidates().contains(candidate1));
        assertTrue(listFromId.getCandidates().contains(candidate2));
        
        //Check that candidate entity has list in its savedLists
        //Retrieve candidates from database
        Candidate candidate1FromID = candidateRepository.findByIdLoadSavedLists(candidate1.getId());
        assertNotNull(candidate1FromID);
        assertNotNull(candidate1FromID.getSavedLists());
        Candidate candidate2FromID = candidateRepository.findByIdLoadSavedLists(candidate2.getId());
        assertNotNull(candidate2FromID);
        assertNotNull(candidate2FromID.getSavedLists());

        //Check that candidates have list recorded
        assertTrue(candidate1FromID.getSavedLists().contains(savedList));
        assertTrue(candidate2FromID.getSavedLists().contains(savedList));

        //Add candidate again - shouldn't make any difference.
        savedList.addCandidate(candidate1);
        listFromId = savedListRepository.findByIdLoadCandidates(savedList.getId());
        assertNotNull(listFromId);
        assertNotNull(listFromId.getCandidates());
        //Check both candidates are in there.
        assertEquals(2, listFromId.getCandidates().size());
        
        
        //Remove a candidate from the list
        savedList.removeCandidate(candidate1);
        listFromId = savedListRepository.findByIdLoadCandidates(savedList.getId());
        assertNotNull(listFromId);
        assertNotNull(listFromId.getCandidates());
        //Check candidate2 is the only one there.
        assertEquals(1, listFromId.getCandidates().size());
        assertFalse(listFromId.getCandidates().contains(candidate1));
        assertTrue(listFromId.getCandidates().contains(candidate2));

        //Remove last candidate from the list
        savedList.removeCandidate(candidate2);
        
        
        //Check adding and removing through candidate methods
        candidate1.addSavedList(savedList);
        candidate2.addSavedList(savedList);
        assertTrue(savedList.getCandidates().contains(candidate1));
        assertTrue(savedList.getCandidates().contains(candidate2));
        
        candidate1.removeSavedList(savedList);
        candidate2.removeSavedList(savedList);
        assertTrue(savedList.getCandidates().isEmpty());
    }

    @Test
    void testListWatching() {
        //Set up watching user
        User watchingUser = new User(
                "sharedusername", "sharedfirst", "sharedlast",
                "sharedemail@test.com", Role.admin);
        watchingUser.setPasswordEnc("xxxx");
        userRepository.save(watchingUser);
        assertNotNull(watchingUser);
        
        //Remove non existent watcher
        savedList.removeWatcher(watchingUser.getId());

        //Add watcher
        savedList.addWatcher(watchingUser.getId());

        //Retrieve from database
        SavedList listFromId = savedListRepository.findById(savedList.getId())
                .orElse(null);
        assertNotNull(listFromId);
        assertNotNull(listFromId.getWatcherUserIds());
        assertTrue(listFromId.getWatcherUserIds().contains(watchingUser.getId()));

        //Add watcher again
        savedList.addWatcher(watchingUser.getId());

        //Retrieve from database
        listFromId = savedListRepository.findById(savedList.getId())
                .orElse(null);
        assertNotNull(listFromId);
        assertNotNull(listFromId.getWatcherUserIds());
        assertTrue(listFromId.getWatcherUserIds().contains(watchingUser.getId()));
        //Should only be in there once
        assertEquals(1, listFromId.getWatcherUserIds().size());


        //Remove watcher
        savedList.removeWatcher(watchingUser.getId());

        //Retrieve from database
        listFromId = savedListRepository.findById(savedList.getId())
                .orElse(null);
        assertNotNull(listFromId);
        assertNotNull(listFromId.getWatcherUserIds());
        assertTrue(listFromId.getWatcherUserIds().isEmpty());
        
        //Add crazy watcher.
        savedList.addWatcher(-1L);
        
    }

    @Test
    void testListSharing() {
        //Set up user to share with
        User sharedUser = new User(
                "sharedusername", "sharedfirst", "sharedlast", 
                "sharedemail@test.com", Role.admin);
        sharedUser.setPasswordEnc("xxxx");
        userRepository.save(sharedUser);
        assertNotNull(sharedUser);
        
        //Retrieve the list by its name        
        SavedList listFromName = savedListRepository.findByNameIgnoreCase("testlist");
        assertNotNull(listFromName);
        //So far it is not shared with anyone
        assertNotNull(listFromName.getUsers());
        assertTrue(listFromName.getUsers().isEmpty());

        //Look up the list by its id - should look the same
        SavedList listFromId = savedListRepository.findByIdLoadUsers(
                listFromName.getId());
        assertNotNull(listFromId);
        assertNotNull(listFromId.getUsers());
        assertEquals(0, listFromName.getUsers().size());
        
        //Now share with user.
        listFromId.addUser(sharedUser);
        
        //Look up list again from id
        listFromId = savedListRepository.findByIdLoadUsers(
                listFromName.getId());
        //Now the list should record that it is shared with the sharedUser
        assertNotNull(listFromId.getUsers());
        assertEquals(1, listFromName.getUsers().size());
        assertTrue(listFromName.getUsers().contains(sharedUser));
        
        //Look up shared used on database.
        User sharedUserById = userRepository.findByIdLoadSharedLists(sharedUser.getId());
        //Shared user should show that it is sharing the list.
        assertNotNull(sharedUserById.getSharedLists());
        assertEquals(1, sharedUserById.getSharedLists().size());
        assertTrue(sharedUserById.getSharedLists().contains(listFromId));
    }
}