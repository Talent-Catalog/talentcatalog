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

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.SavedListRepository;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.impl.SavedListServiceImpl;

//@SpringBootTest
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
        
        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.getLoggedInUser().orElse(null)).thenReturn(testUser);
        
        SavedListService savedListService = new SavedListServiceImpl(
                candidateRepository, savedListRepository, null, null, null,
            null, null, userRepository,
                authService);
        savedListAdminApi = new SavedListAdminApi(savedListService, null, null);
    }

}
