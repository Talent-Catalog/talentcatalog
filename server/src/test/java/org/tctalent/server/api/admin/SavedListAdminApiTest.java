/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.api.admin;

import static org.mockito.Mockito.when;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.security.AuthService;

//@SpringBootTest
class SavedListAdminApiTest {

    @Autowired
    CandidateRepository candidateRepository;

    @Autowired
    SavedListRepository savedListRepository;

    @Autowired
    UserRepository userRepository;

    private SavedListAdminApi savedListAdminApi;

//    @BeforeEach
    void initUseCase() {
        User testUser = userRepository.findByUsernameIgnoreCase("camerojo");

        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.getLoggedInUser().orElse(null)).thenReturn(testUser);

//        SavedListService savedListService = new SavedListServiceImpl(
//                candidateRepository, null, savedListRepository, null, null, null,
//            null, null, userRepository,
//                authService);
//        savedListAdminApi = new SavedListAdminApi(savedListService, null, null);
    }

}
