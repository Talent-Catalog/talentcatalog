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
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateExamRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tctalent.server.request.candidate.exam.UpdateCandidateExamRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.email.EmailHelper;

@ExtendWith(MockitoExtension.class)
class CandidateExamServiceImplTest {

    private CreateCandidateExamRequest createRequest;
    private CandidateExam exam;
    private List<CandidateExam> examList;
    private UpdateCandidateExamRequest updateRequest;
    private Candidate candidate;
    private long candidateId;

    private static final long EXAM_ID = 33L;
    private static final User ADMIN_USER = getAdminUser();
    private static final Exam EXAM = Exam.IELTSGen;
    private static final String OTHER = "other";
    private static final String SCORE = "7.5";
    private static final Long YEAR = 2020L;
    private static final String NOTES = "notes";

    @Mock private CandidateExamRepository candidateExamRepository;
    @Mock private CandidateRepository candidateRepository;
    @Mock private AuthService authService;
    @Mock private EmailHelper emailHelper;
    @Mock private CandidateService candidateService;

    @Captor private ArgumentCaptor<CandidateExam> examCaptor;

    @InjectMocks
    CandidateExamServiceImpl candidateExamService;

    @BeforeEach
    void setUp() {
        candidate = getCandidate();
        candidateId = candidate.getId();
        exam = new CandidateExam();
        exam.setId(EXAM_ID);
        exam.setCandidate(candidate);
        examList = List.of(exam, exam);
        createRequest = new CreateCandidateExamRequest();
        updateRequest = new UpdateCandidateExamRequest();
        updateRequest.setId(EXAM_ID);
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void createExam_shouldThrow_whenCandidateNotFound() {
        given(candidateRepository.findById(candidateId)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateExamService.createExam(candidateId, createRequest));
    }

    @Test
    @DisplayName("should create exam as expected")
    void createEducation() {
        createRequest.setExam(EXAM);
        createRequest.setOtherExam(OTHER);
        createRequest.setScore(SCORE);
        createRequest.setYear(YEAR);
        createRequest.setNotes(NOTES);

        given(candidateRepository.findById(candidateId)).willReturn(Optional.of(candidate));

        candidateExamService.createExam(candidateId, createRequest);

        verify(candidateExamRepository).save(examCaptor.capture());
        CandidateExam result = examCaptor.getValue();
        assertEquals(candidate, exam.getCandidate());
        verifyExam(result);
    }

    @Test
    @DisplayName("should throw when user not logged in")
    void updateCandidateExam_shouldThrow_whenUserNotLoggedIn() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateExamService.updateCandidateExam(updateRequest));
    }

    @Test
    @DisplayName("should throw when exam not found")
    void updateExam_shouldThrow_whenExamNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateExamRepository.findByIdLoadCandidate(updateRequest.getId()))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateExamService.updateCandidateExam(updateRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(EXAM_ID)));
    }

    @Test
    @DisplayName("should update and save exam as expected")
    void updateCandidateExam_shouldUpdateAndSaveExam() {
        updateRequest.setExam(EXAM);
        updateRequest.setOtherExam(OTHER);
        updateRequest.setScore(SCORE);
        updateRequest.setYear(YEAR);
        updateRequest.setNotes(NOTES);

        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateExamRepository.findByIdLoadCandidate(updateRequest.getId()))
            .willReturn(Optional.of(exam));

        candidateExamService.updateCandidateExam(updateRequest);

        verify(candidateExamRepository).save(examCaptor.capture());
        CandidateExam result = examCaptor.getValue();
        verifyExam(result);

        verify(candidateService).save(candidate, true);
        assertEquals(ADMIN_USER, candidate.getUpdatedBy());
    }

    private static void verifyExam(CandidateExam exam) {
        assertEquals(EXAM, exam.getExam());
        assertEquals(OTHER, exam.getOtherExam());
        assertEquals(SCORE, exam.getScore());
        assertEquals(YEAR, exam.getYear());
        assertEquals(NOTES, exam.getNotes());
    }

    @Test
    @DisplayName("should return list of exams when found")
    void list_shouldReturnListOfExams_whenFound() {
        given(candidateExamRepository.findByCandidateId(candidateId))
            .willReturn(examList);

        assertEquals(examList, candidateExamService.list(candidateId));
    }

    @Test
    @DisplayName("should return empty list when none found")
    void list_shouldReturnEmptyList_whenNoneFound() {
        given(candidateExamRepository.findByCandidateId(candidateId))
            .willReturn(Collections.emptyList());

        assertTrue(candidateExamService.list(candidateId).isEmpty());
    }

}
