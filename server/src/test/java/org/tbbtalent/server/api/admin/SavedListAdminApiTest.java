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

package org.tbbtalent.server.api.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

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
        when(userContext.getLoggedInUser().orElse(null)).thenReturn(testUser);
        
        SavedListService savedListService = new SavedListServiceImpl(
                candidateRepository, savedListRepository, null, null, userRepository, 
                userContext);
        savedListAdminApi = new SavedListAdminApi(savedListService, null, null);
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