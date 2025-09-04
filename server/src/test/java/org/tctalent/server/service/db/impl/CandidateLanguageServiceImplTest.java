/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.LanguageTestData.getLanguage;
import static org.tctalent.server.data.UserTestData.getAdminUser;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateLanguageRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.LanguageLevelRepository;
import org.tctalent.server.repository.db.LanguageRepository;
import org.tctalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;

@ExtendWith(MockitoExtension.class)
class CandidateLanguageServiceImplTest {

    private static final User ADMIN_USER = getAdminUser();
    private static final long CANDIDATE_ID = 1L;
    private static final long LANGUAGE_ID = 2L;
    private static final long WRITTEN_LEVEL_ID = 3L;
    private static final long SPOKEN_LEVEL_ID = 4L;
    private static final Language LANGUAGE = getLanguage();

    private CreateCandidateLanguageRequest createRequest;
    private Candidate candidate;

    private @Mock CandidateLanguageRepository candidateLanguageRepository;
    private @Mock LanguageRepository languageRepository;
    private @Mock CandidateRepository candidateRepository;
    private @Mock CandidateService candidateService;
    private @Mock LanguageLevelRepository languageLevelRepository;
    private @Mock AuthService authService;

    @InjectMocks
    CandidateLanguageServiceImpl candidateLanguageService;

    @BeforeEach
    void setUp() {
        createRequest = new CreateCandidateLanguageRequest();
        createRequest.setCandidateId(CANDIDATE_ID);
        createRequest.setLanguageId(LANGUAGE_ID);
        createRequest.setWrittenLevelId(WRITTEN_LEVEL_ID);
        createRequest.setSpokenLevelId(SPOKEN_LEVEL_ID);
        candidate = getCandidate();
    }

    @Test
    @DisplayName("should throw when user not logged in")
    void createCandidateLanguage_shouldThrow_whenUserNotLoggedIn() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateLanguageService.createCandidateLanguage(createRequest));
    }

    @Test
    @DisplayName("should throw when language not found")
    void createCandidateLanguage_shouldThrow_whenLanguageNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateService.getCandidateFromRequest(anyLong())).willReturn(candidate);
        given(languageRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateLanguageService.createCandidateLanguage(createRequest));
    }

    @Test
    @DisplayName("should create candidate language")
    void createCandidateLanguage_shouldCreate() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateService.getCandidateFromRequest(anyLong())).willReturn(candidate);
        given(languageRepository.findById(anyLong())).willReturn(Optional.of(LANGUAGE));
    }

}
