/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.api.admin;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.SavedListRepository;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.request.list.CreateSavedListRequest;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.impl.SavedListServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
class SavedListAdminApiTest {

    @Autowired
    CandidateRepository candidateRepository;
    
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
        
        SavedListService savedListService = new SavedListServiceImpl(
                candidateRepository, savedListRepository, null, userRepository, 
                userContext);
        savedListAdminApi = new SavedListAdminApi(savedListService);
    }

    @Transactional
//    @Test
    void createNewSavedList() {
        SearchSavedListRequest searchReq = new SearchSavedListRequest();
        searchReq.setKeyword("testlist");
        List<Map<String, Object>> lists = savedListAdminApi.search(searchReq);
        if (lists.size() > 0) {
            Long id = (Long) lists.get(0).get("id");
            savedListAdminApi.delete(id);
        }
        
        CreateSavedListRequest request = new CreateSavedListRequest();
        request.setName("TestList");
        request.setFixed(false);

        Map<String, Object> result = savedListAdminApi.create(request);
        assertNotNull(result);
        Object name = result.get("name");
        assertEquals("TestList", name);
    }
}