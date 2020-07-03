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
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.list.SearchSavedListRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        User candidateUser = createTestUser(username, firstName, lastName, email);
        Candidate candidate = new Candidate();
        candidate.setUser(candidateUser);
        return candidateRepository.save(candidate);
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
    void testDelete() {
      
        Long id = savedList.getId();

        //Retrieve list from database
        SavedList listFromId = savedListRepository.findById(id)
                .orElse(null);
        assertNotNull(listFromId);
        
        //Delete list
        savedListRepository.delete(savedList);
        
        //Shouldn't be able to fetch it.
        //Retrieve list from database
        listFromId = savedListRepository.findById(id)
                .orElse(null);
        assertNull(listFromId);
        
    }

    @Test
    void testSharedUsers() {
        //Set up a couple of users
        User user1 = createTestUser(
                "c1username", "c1first", "c1last",
                "c1email@test.com");
        User user2 = createTestUser(
                "c2username", "c2first", "c2last",
                "c2email@test.com");
        User user3 = createTestUser(
                "c3username", "c3first", "c3last",
                "c3email@test.com");
        
        savedList.addUser(user1);
        savedList.addUser(user2);
        
        assertTrue(savedList.getUsers().contains(user1));
        assertTrue(user1.getSharedLists().contains(savedList));
        assertTrue(savedList.getUsers().contains(user2));
        assertTrue(user2.getSharedLists().contains(savedList));
        
        savedList.removeUser(user1);
        assertFalse(savedList.getUsers().contains(user1));
        assertFalse(user1.getSharedLists().contains(savedList));
        assertTrue(savedList.getUsers().contains(user2));
        assertTrue(user2.getSharedLists().contains(savedList));

        savedList.setUsers(null);
        assertFalse(savedList.getUsers().contains(user1));
        assertFalse(user1.getSharedLists().contains(savedList));
        assertFalse(savedList.getUsers().contains(user2));
        assertFalse(user2.getSharedLists().contains(savedList));
        
    }

    @Test
    void testListSavedLists() {

        SearchSavedListRequest request = new SearchSavedListRequest();
        request.setOwned(true);
        request.setShared(true);
        
        GetSavedListsQuery getSavedListsQuery;
        Sort sort;
        List<SavedList> lists;
        
        getSavedListsQuery = new GetSavedListsQuery(request, owningUser);
        sort = Sort.by(Sort.Direction.ASC, "name");
        lists = savedListRepository.findAll(getSavedListsQuery, sort);

        assertNotNull(lists);
        assertEquals(1, lists.size());

        //Create a sharing user
        User sharingUser = createTestUser(
                "c1username", "c1first", "c1last",
                "c1email@test.com");

        getSavedListsQuery = new GetSavedListsQuery(request, sharingUser);
        sort = Sort.by(Sort.Direction.ASC, "name");
        lists = savedListRepository.findAll(getSavedListsQuery, sort);
        assertNotNull(lists);
        assertEquals(0, lists.size());
        
        savedList.addUser(sharingUser);
        savedListRepository.save(savedList);
        
        getSavedListsQuery = new GetSavedListsQuery(request, sharingUser);
        sort = Sort.by(Sort.Direction.ASC, "name");
        lists = savedListRepository.findAll(getSavedListsQuery, sort);
        assertNotNull(lists);
        assertEquals(1, lists.size());
        
    }

    @Test
    void testSearchSavedLists() {

        SearchSavedListRequest request = new SearchSavedListRequest();
        request.setOwned(true);
        request.setShared(true);
        request.setFixed(false);
        request.setPageNumber(0);
        request.setPageSize(4);
        request.setSortDirection(Sort.Direction.ASC);
        request.setSortFields(new String[] {"name"});
        
        GetSavedListsQuery getSavedListsQuery;
        PageRequest pageRequest;
        Page<SavedList> lists;

        pageRequest = request.getPageRequest();
        getSavedListsQuery = new GetSavedListsQuery(request, owningUser);
        lists = savedListRepository.findAll(getSavedListsQuery, pageRequest);

        assertNotNull(lists);
        assertEquals(1, lists.getContent().size());

        //Create a sharing user
        User sharingUser = createTestUser(
                "c1username", "c1first", "c1last",
                "c1email@test.com");

        pageRequest = request.getPageRequest();
        getSavedListsQuery = new GetSavedListsQuery(request, sharingUser);
        lists = savedListRepository.findAll(getSavedListsQuery, pageRequest);
        assertNotNull(lists);
        assertEquals(0, lists.getContent().size());
        
        savedList.addUser(sharingUser);
        savedListRepository.save(savedList);

        pageRequest = request.getPageRequest();
        getSavedListsQuery = new GetSavedListsQuery(request, sharingUser);
        lists = savedListRepository.findAll(getSavedListsQuery, pageRequest);
        assertNotNull(lists);
        assertEquals(1, lists.getContent().size());
        
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
        Candidate candidate3 = createTestCandidate(
                "c3username", "c3first", "c3last",
                "c3email@test.com");
        Set<Candidate> candidates;

        //Retrieve list from database
        SavedList listFromId = savedListRepository.findByIdLoadCandidates(savedList.getId())
                .orElse(null);
        assertNotNull(listFromId);
        assertNotNull(listFromId.getCandidates());

        //Check that list of candidates is currently empty.
        assertEquals(0, listFromId.getCandidates().size());

        //Add some candidates to the list
        candidates = new HashSet<>();
        candidates.add(candidate1);
        candidates.add(candidate2);
        savedList.addCandidates(candidates);
        //Retrieve list from database
        listFromId = savedListRepository.findByIdLoadCandidates(savedList.getId())
                .orElse(null);
                
        assertNotNull(listFromId);
        assertNotNull(listFromId.getCandidates());
        //Check both candidates are in there.
        assertEquals(2, listFromId.getCandidates().size());
        
        assertTrue(listFromId.getCandidates().contains(candidate1));
        assertTrue(candidate1.getSavedLists().contains(listFromId));
        
        assertTrue(listFromId.getCandidates().contains(candidate2));
        assertTrue(candidate2.getSavedLists().contains(listFromId));
        
        assertFalse(listFromId.getCandidates().contains(candidate3));
        assertFalse(candidate3.getSavedLists().contains(listFromId));
        
        //Set candidates in the list - replacing any existing content
        candidates = new HashSet<>();
        candidates.add(candidate2);
        candidates.add(candidate3);
        savedList.setCandidates(candidates);

        //Retrieve list from database
        listFromId = savedListRepository.findByIdLoadCandidates(savedList.getId())
                .orElse(null);
                
        assertNotNull(listFromId);
        assertNotNull(listFromId.getCandidates());
        //Check only 2 candidates are in there. Previous contents replaced.
        assertEquals(2, listFromId.getCandidates().size());
        
        assertFalse(listFromId.getCandidates().contains(candidate1));
        assertFalse(candidate1.getSavedLists().contains(listFromId));
        
        assertTrue(listFromId.getCandidates().contains(candidate2));
        assertTrue(candidate2.getSavedLists().contains(listFromId));
        
        assertTrue(listFromId.getCandidates().contains(candidate3));
        assertTrue(candidate3.getSavedLists().contains(listFromId));
        
        //Check that candidate entities have list (or not) in its savedLists
        //Retrieve candidates from database
        Candidate candidate1FromID = candidateRepository.findByIdLoadSavedLists(candidate1.getId());
        assertNotNull(candidate1FromID);
        assertNotNull(candidate1FromID.getSavedLists());
        Candidate candidate2FromID = candidateRepository.findByIdLoadSavedLists(candidate2.getId());
        assertNotNull(candidate2FromID);
        assertNotNull(candidate2FromID.getSavedLists());
        Candidate candidate3FromID = candidateRepository.findByIdLoadSavedLists(candidate3.getId());
        assertNotNull(candidate3FromID);
        assertNotNull(candidate3FromID.getSavedLists());

        //Check that candidates have list recorded
        assertFalse(candidate1FromID.getSavedLists().contains(savedList));
        assertTrue(candidate2FromID.getSavedLists().contains(savedList));
        assertTrue(candidate3FromID.getSavedLists().contains(savedList));

        //Add candidate again - shouldn't make any difference.
        savedList.addCandidate(candidate2);
        listFromId = savedListRepository.findByIdLoadCandidates(savedList.getId())
                .orElse(null);
                
        assertNotNull(listFromId);
        assertNotNull(listFromId.getCandidates());
        //Check both candidates are in there.
        assertEquals(2, listFromId.getCandidates().size());
        
        
        //Remove a candidate from the list
        savedList.removeCandidate(candidate3);
        listFromId = savedListRepository.findByIdLoadCandidates(savedList.getId())
                .orElse(null);
        assertNotNull(listFromId);
        assertNotNull(listFromId.getCandidates());
        //Check candidate2 is the only one there.
        assertEquals(1, listFromId.getCandidates().size());
        assertFalse(listFromId.getCandidates().contains(candidate3));
        assertTrue(listFromId.getCandidates().contains(candidate2));

        //List has just candidate2 in there.
        //Test clearing the list by passing null into setCandidates
        savedList.setCandidates(null);
        assertTrue(savedList.getCandidates().isEmpty());
        assertFalse(candidate2.getSavedLists().contains(savedList));
        
        
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
        SavedList listFromName = savedListRepository.findByNameIgnoreCase("testlist", owningUser.getId())
                .orElse(null);
                
        assertNotNull(listFromName);
        //So far it is not shared with anyone
        assertNotNull(listFromName.getUsers());
        assertTrue(listFromName.getUsers().isEmpty());

        //Look up the list by its id - should look the same
        SavedList listFromId = 
                savedListRepository.findByIdLoadUsers(listFromName.getId())
                .orElse(null);
                
        assertNotNull(listFromId);
        assertNotNull(listFromId.getUsers());
        assertEquals(0, listFromName.getUsers().size());
        
        //Now share with user.
        listFromId.addUser(sharedUser);
        
        //Look up list again from id
        listFromId = savedListRepository.findByIdLoadUsers(
                listFromName.getId()).orElse(null);
        //Now the list should record that it is shared with the sharedUser
        assertNotNull(listFromId);
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