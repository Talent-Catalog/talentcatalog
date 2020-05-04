/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class SavedListRepositoryTest {
    
    @Autowired
    private SavedListRepository savedListRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void injectedComponentsAreNotNull() {
        assertNotNull(savedListRepository);
        assertNotNull(userRepository);
    }

    @Test
    void createListAndShareIt() {
        //Set up owning user and a user to share with
        User owningUser = new User(
                "username", "first", "last", 
                "email@test.com", Role.admin);
        owningUser.setPasswordEnc("xxxx");
        userRepository.save(owningUser);
        assertNotNull(owningUser);

        User sharedUser = new User(
                "sharedusername", "sharedfirst", "sharedlast", 
                "sharedemail@test.com", Role.admin);
        sharedUser.setPasswordEnc("xxxx");
        userRepository.save(sharedUser);
        assertNotNull(sharedUser);
        
        //Create a new list
        SavedList savedList = new SavedList();
        savedList.setName("TestList");
        savedList.setAuditFields(owningUser);
        
        savedListRepository.save(savedList);
        assertNotNull(savedList);
        
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