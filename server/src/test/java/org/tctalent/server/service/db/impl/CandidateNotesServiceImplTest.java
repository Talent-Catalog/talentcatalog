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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.UserTestData.getAdminUser;
import static org.tctalent.server.data.UserTestData.getSystemAdminUser;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateNote;
import org.tctalent.server.model.db.NoteType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateNoteRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.note.CreateCandidateNoteRequest;
import org.tctalent.server.request.note.SearchCandidateNotesRequest;
import org.tctalent.server.request.note.UpdateCandidateNoteRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.UserService;

@ExtendWith(MockitoExtension.class)
class CandidateNotesServiceImplTest {

    private CandidateNote candidateNote;
    private CreateCandidateNoteRequest createRequest;
    private UpdateCandidateNoteRequest updateRequest;

    private static final User ADMIN_USER = getAdminUser();
    private static final long CANDIDATE_ID = 1L;
    private static final Candidate CANDIDATE = getCandidate();
    private static final String TITLE = "title";
    private static final String COMMENT = "comment";
    private static final User SYSTEM_ADMIN = getSystemAdminUser();

    private @Mock CandidateNoteRepository candidateNoteRepository;
    private @Mock UserService userService;
    private @Mock CandidateRepository candidateRepository;
    private @Mock AuthService authService;

    private @Captor ArgumentCaptor<CandidateNote> noteCaptor;

    @InjectMocks
    CandidateNotesServiceImpl candidateNotesService;

    @BeforeEach
    void setUp() {
        candidateNote = new CandidateNote();
        createRequest = new CreateCandidateNoteRequest();
        createRequest.setCandidateId(CANDIDATE_ID);
        createRequest.setTitle(TITLE);
        createRequest.setComment(COMMENT);
        updateRequest = new UpdateCandidateNoteRequest();
        updateRequest.setTitle(TITLE);
        updateRequest.setComment(COMMENT);
    }

    @Test
    @DisplayName("should return populated page when found")
    void searchCandidateNotes_shouldReturnPopulatedPage_whenFound() {
        final Page<CandidateNote> notePage = new PageImpl<>(List.of(candidateNote, candidateNote));
        final long candidateId = 1L;
        final SearchCandidateNotesRequest request = new SearchCandidateNotesRequest();
        request.setCandidateId(candidateId);
        given(candidateNoteRepository.findByCandidateId(candidateId, request.getPageRequest()))
            .willReturn(notePage);

        assertEquals(notePage, candidateNotesService.searchCandidateNotes(request));
    }

    @Test
    @DisplayName("should return empty page when none found")
    void searchCandidateNotes_shouldReturnEmptyPage_whenNoneFound() {
        final Page<CandidateNote> notePage = new PageImpl<>(Collections.emptyList());
        final SearchCandidateNotesRequest request = new SearchCandidateNotesRequest();
        request.setCandidateId(CANDIDATE_ID);
        given(candidateNoteRepository.findByCandidateId(CANDIDATE_ID, request.getPageRequest()))
            .willReturn(notePage);

        assertTrue(candidateNotesService.searchCandidateNotes(request).isEmpty());
    }

    @Test
    @DisplayName("should create and save note as expected")
    void createCandidateNote_shouldCreateAndSaveNote() {
        given(userService.getLoggedInUser()).willReturn(ADMIN_USER);
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));

        candidateNotesService.createCandidateNote(createRequest);

        verify(candidateNoteRepository).save(noteCaptor.capture());
        CandidateNote note = noteCaptor.getValue();
        assertEquals(TITLE, note.getTitle());
        assertEquals(COMMENT, note.getComment());
        assertEquals(ADMIN_USER, note.getCreatedBy());
        assertEquals(CANDIDATE, note.getCandidate());
        assertEquals(NoteType.admin, note.getNoteType());
    }

    @Test
    @DisplayName("should create note of type candidate when creating user is candidate")
    void createCandidateNote_shouldCreateCandidateNoteOfTypeCandidate_whenCreatingUserIsCandidate() {
        CANDIDATE.getUser().setId(11L);
        given(userService.getLoggedInUser()).willReturn(CANDIDATE.getUser());
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));

        candidateNotesService.createCandidateNote(createRequest);

        verify(candidateNoteRepository).save(noteCaptor.capture());
        assertEquals(NoteType.candidate, noteCaptor.getValue().getNoteType());
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void createCandidateNote_shouldThrow_whenCandidateNotFound() {
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class, () -> candidateNotesService.createCandidateNote(createRequest));
    }

    @Test
    @DisplayName("should use system admin when no logged in user found")
    void createCandidateNote_shouldUseSystemAdmin_whenNoLoggedInUserFound() {
        given(userService.getLoggedInUser()).willReturn(null);
        given(userService.getSystemAdminUser()).willReturn(SYSTEM_ADMIN);
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));

        candidateNotesService.createCandidateNote(createRequest);

        verify(candidateNoteRepository).save(noteCaptor.capture());
        assertEquals(SYSTEM_ADMIN, noteCaptor.getValue().getCreatedBy());
    }

    @Test
    @DisplayName("should update candidate note as expected")
    void updateCandidateNote_shouldUpdateCandidateNote() {
        final long noteId = 1L;
        given(candidateNoteRepository.findById(noteId)).willReturn(Optional.of(candidateNote));
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));

        candidateNotesService.updateCandidateNote(noteId, updateRequest);

        verify(candidateNoteRepository).save(noteCaptor.capture());
        CandidateNote note = noteCaptor.getValue();
        assertEquals(TITLE, note.getTitle());
        assertEquals(COMMENT, note.getComment());
        assertEquals(ADMIN_USER, note.getUpdatedBy());
    }

}
