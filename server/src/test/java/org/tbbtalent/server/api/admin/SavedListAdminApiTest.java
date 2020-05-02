/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.api.admin;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.repository.SavedListRepository;
import org.tbbtalent.server.repository.UserRepository;
import org.tbbtalent.server.request.list.UpdateSavedListRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.SavedListService;
import org.tbbtalent.server.service.impl.SavedListServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
class SavedListAdminApiTest {
    
    @Autowired
    SavedListRepository savedListRepository;
    
    @Autowired
    UserRepository userRepository;
    
    private SavedListAdminApi savedListAdminApi;

    @BeforeEach
    void initUseCase() {
        User testUser = userRepository.findByUsernameIgnoreCase("camerojo"); 
        
        UserContext userContext = Mockito.mock(UserContext.class);
        when(userContext.getLoggedInUser()).thenReturn(testUser);
        
        SavedListService savedListService = new SavedListServiceImpl(savedListRepository, userContext);
        savedListAdminApi = new SavedListAdminApi(savedListService);
    }

    @Test
    void createNewSavedList() {
        UpdateSavedListRequest request = new UpdateSavedListRequest();
        request.setName("TestList");
        request.setFixed(false);

        Map<String, Object> result = savedListAdminApi.create(request);
        assertNotNull(result);
        Object name = result.get("name");
        assertEquals("TestList", name);
    }
}