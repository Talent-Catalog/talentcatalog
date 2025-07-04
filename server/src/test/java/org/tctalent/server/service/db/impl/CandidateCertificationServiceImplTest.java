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
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.UserTestData.getAdminUser;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateCertificationRepository;
import org.tctalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tctalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;

@ExtendWith(MockitoExtension.class)
class CandidateCertificationServiceImplTest {

    private CandidateCertification certification;
    private List<CandidateCertification> certificationList;
    private CreateCandidateCertificationRequest createRequest;
    private Candidate candidate;
    private UpdateCandidateCertificationRequest updateRequest;
    private long candidateId;

    private static final long CERT_ID = 123L;
    private static final User ADMIN_USER = getAdminUser();
    private static final String NAME = "name";
    private static final String INSTITUTION = "institution";
    private static final LocalDate DATE_COMPLETED = LocalDate.parse("2024-06-01");

    @Mock private CandidateCertificationRepository candidateCertificationRepository;
    @Mock private AuthService authService;
    @Mock private CandidateService candidateService;

    @InjectMocks
    CandidateCertificationServiceImpl candidateCertificationService;

    @BeforeEach
    void setUp() {
        certification = new CandidateCertification();
        certification.setId(CERT_ID);
        candidate = getCandidate();
        candidateId = candidate.getId();
        certification.setCandidate(candidate);
        certificationList = List.of(certification, certification);
        createRequest = new CreateCandidateCertificationRequest();
        createRequest.setCandidateId(candidateId);
        createRequest.setName(NAME);
        createRequest.setInstitution(INSTITUTION);
        createRequest.setDateCompleted(DATE_COMPLETED);
        updateRequest = new UpdateCandidateCertificationRequest();
        updateRequest.setName(NAME);
        updateRequest.setInstitution(INSTITUTION);
        updateRequest.setDateCompleted(DATE_COMPLETED);
        updateRequest.setId(CERT_ID);
    }

    @Test
    @DisplayName("should return list of certifications")
    void list_shouldReturnListOfCertifications() {
        given(candidateCertificationRepository.findByCandidateId(candidateId))
            .willReturn(certificationList);

        assertEquals(certificationList, candidateCertificationService.list(candidateId));
    }

    @Test
    @DisplayName("should return empty list when none found")
    void list_shouldReturnEmptyList_whenNoneFound() {
        given(candidateCertificationRepository.findByCandidateId(candidateId))
            .willReturn(Collections.emptyList());

        assertTrue(candidateCertificationService.list(candidateId).isEmpty());
    }

    @Test
    @DisplayName("should throw when user not logged in")
    void createCandidateCertification_shouldThrow_whenUserNotLoggedIn() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateCertificationService.createCandidateCertification(createRequest));
    }

    @Test
    @DisplayName("should create and save candidate certification")
    void createCandidateCertification_shouldCreateAndSaveCandidateCertification() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateService.getCandidateFromRequest(createRequest.getCandidateId()))
            .willReturn(candidate);
        given(candidateCertificationRepository.save(any(CandidateCertification.class)))
            .will(returnsFirstArg());

        CandidateCertification result =
            candidateCertificationService.createCandidateCertification(createRequest);

        assertEquals(candidate, result.getCandidate());
        verifyCandidateCertification(result);
    }

    @Test
    @DisplayName("should throw when user not logged in")
    void updateCandidateCertification_shouldThrow_whenUserNotLoggedIn() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateCertificationService.updateCandidateCertification(updateRequest));
    }

    @Test
    @DisplayName("should throw when certification not found")
    void updateCandidateCertification_shouldThrow_whenCertificationNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateCertificationRepository.findByIdLoadCandidate(updateRequest.getId()))
            .willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateCertificationService.updateCandidateCertification(updateRequest));
    }

    @Test
    @DisplayName("should update and save candidate certification")
    void updateCandidateCertification_shouldUpdateAndSaveCandidateCertification() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateCertificationRepository.findByIdLoadCandidate(updateRequest.getId()))
            .willReturn(Optional.ofNullable(certification));
        given(candidateCertificationRepository.save(any(CandidateCertification.class)))
            .will(returnsFirstArg());

        candidateCertificationService.updateCandidateCertification(updateRequest);

        verifyCandidateCertification(certification);
    }

    private void verifyCandidateCertification(CandidateCertification certification) {
        verify(candidateCertificationRepository).save(any(CandidateCertification.class));
        verify(candidateService).save(candidate, true);
        assertEquals(NAME, certification.getName());
        assertEquals(INSTITUTION, certification.getInstitution());
        assertEquals(DATE_COMPLETED, certification.getDateCompleted());
        assertEquals(candidate.getUpdatedBy(), ADMIN_USER);
    }

    @Test
    @DisplayName("should throw when user not logged in")
    void deleteCandidateCertification_shouldThrow_whenUserNotLoggedIn() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateCertificationService.deleteCandidateCertification(CERT_ID));
    }

    @Test
    @DisplayName("should throw when certification not found")
    void deleteCandidateCertification_shouldThrow_whenCertificationNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateCertificationRepository.findByIdLoadCandidate(updateRequest.getId()))
            .willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateCertificationService.deleteCandidateCertification(CERT_ID));
    }

    @Test
    @DisplayName("should delete candidate certification")
    void deleteCandidateCertification_shouldDeleteCandidateCertification() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateCertificationRepository.findByIdLoadCandidate(updateRequest.getId()))
            .willReturn(Optional.ofNullable(certification));

        candidateCertificationService.deleteCandidateCertification(CERT_ID);

        verify(candidateCertificationRepository).delete(certification);
        assertEquals(candidate.getUpdatedBy(), ADMIN_USER);
        verify(candidateService).save(candidate, true);
    }

}
