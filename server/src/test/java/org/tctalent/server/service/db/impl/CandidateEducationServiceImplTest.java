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
import static org.tctalent.server.data.CountryTestData.LEBANON;
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
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.EducationType;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateEducationRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.EducationMajorRepository;
import org.tctalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tctalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;

@ExtendWith(MockitoExtension.class)
class CandidateEducationServiceImplTest {

    private CreateCandidateEducationRequest createRequest;
    private CandidateEducation education;
    private List<CandidateEducation> educationList;
    private UpdateCandidateEducationRequest updateRequest;
    private Candidate candidate;

    private static final long CANDIDATE_ID = getCandidate().getId();
    private static final long EDUCATION_ID = 33L;
    private static final long COUNTRY_ID = 66L;
    private static final long MAJOR_ID = 1L;
    private static final User ADMIN_USER = getAdminUser();
    private static final EducationType TYPE = EducationType.Bachelor;
    private static final Country COUNTRY = LEBANON;
    private static final EducationMajor MAJOR = new EducationMajor("Zoology", Status.active);
    private static final Integer LENGTH = 4;
    private static final String INSTITUTION = "Lebanese University";
    private static final String COURSE_NAME = "Zoology & Microbiology";
    private static final Integer YEAR_COMPLETED = 2022;
    private static final boolean INCOMPLETE = false;

    @Mock private CandidateEducationRepository candidateEducationRepository;
    @Mock private CandidateService candidateService;
    @Mock private CountryRepository countryRepository;
    @Mock private EducationMajorRepository educationMajorRepository;
    @Mock private AuthService authService;

    @Captor private ArgumentCaptor<CandidateEducation> educationCaptor;

    @InjectMocks
    CandidateEducationServiceImpl candidateEducationService;

    @BeforeEach
    void setUp() {
        candidate = getCandidate();
        createRequest = new CreateCandidateEducationRequest();
        createRequest.setCountryId(COUNTRY_ID);
        createRequest.setEducationMajorId(MAJOR_ID);
        education = new CandidateEducation();
        education.setId(EDUCATION_ID);
        education.setCandidate(candidate);
        educationList = List.of(education, education);
        updateRequest = new UpdateCandidateEducationRequest();
        updateRequest.setCountryId(COUNTRY_ID);
        updateRequest.setId(EDUCATION_ID);
        updateRequest.setMajorId(MAJOR_ID);
    }

    @Test
    @DisplayName("should return list of educations when found")
    void list_shouldReturnListOfEducations_whenFound() {
        given(candidateEducationRepository.findByCandidateId(CANDIDATE_ID))
            .willReturn(educationList);

        assertEquals(educationList, candidateEducationService.list(CANDIDATE_ID));
    }

    @Test
    @DisplayName("should return empty list when none found")
    void list_shouldReturnEmptyList_whenNoneFound() {
        given(candidateEducationRepository.findByCandidateId(CANDIDATE_ID))
            .willReturn(Collections.emptyList());

        assertTrue(candidateEducationService.list(CANDIDATE_ID).isEmpty());
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void createEducation_shouldThrow_whenCandidateNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateEducationService.createCandidateEducation(createRequest));
    }

    @Test
    @DisplayName("should throw when country not found")
    void createEducation_shouldThrow_whenCountryNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(countryRepository.findById(createRequest.getCountryId()))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateEducationService.createCandidateEducation(createRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(createRequest.getCountryId())));
    }

    @Test
    @DisplayName("should throw when major not found")
    void createEducation_shouldThrow_whenMajorNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(countryRepository.findById(createRequest.getCountryId()))
            .willReturn(Optional.of(COUNTRY));
        given(educationMajorRepository.findById(createRequest.getEducationMajorId()))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateEducationService.createCandidateEducation(createRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(createRequest.getEducationMajorId())));
    }

    @Test
    @DisplayName("should create education as expected")
    void createEducation() {
        createRequest.setCandidateId(CANDIDATE_ID);
        createRequest.setCountryId(COUNTRY_ID);
        createRequest.setEducationType(TYPE);
        createRequest.setLengthOfCourseYears(LENGTH);
        createRequest.setInstitution(INSTITUTION);
        createRequest.setCourseName(COURSE_NAME);
        createRequest.setYearCompleted(YEAR_COMPLETED);
        createRequest.setIncomplete(INCOMPLETE);

        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateService.getCandidateFromRequest(createRequest.getCandidateId()))
            .willReturn(candidate);
        given(countryRepository.findById(createRequest.getCountryId()))
            .willReturn(Optional.of(COUNTRY));
        given(educationMajorRepository.findById(createRequest.getEducationMajorId()))
            .willReturn(Optional.of(MAJOR));

        candidateEducationService.createCandidateEducation(createRequest);

        verify(candidateEducationRepository).save(educationCaptor.capture());
        CandidateEducation result = educationCaptor.getValue();
        assertEquals(candidate, education.getCandidate());
        verifyEducation(result);

        verify(candidateService).save(candidate, true);
        assertEquals(ADMIN_USER, candidate.getUpdatedBy());
    }

    @Test
    @DisplayName("should throw when user not logged in")
    void updateCandidateEducation_shouldThrow_whenUserNotLoggedIn() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateEducationService.updateCandidateEducation(updateRequest));
    }

    @Test
    @DisplayName("should throw when education not found")
    void updateCandidateEducation_shouldThrow_whenEducationNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateEducationRepository.findById(updateRequest.getId()))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateEducationService.updateCandidateEducation(updateRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(updateRequest.getId())));
    }

    @Test
    @DisplayName("should throw when country not found")
    void updateCandidateEducation_shouldThrow_whenCountryNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateEducationRepository.findById(updateRequest.getId()))
            .willReturn(Optional.of(education));
        given(countryRepository.findById(updateRequest.getCountryId())).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateEducationService.updateCandidateEducation(updateRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(updateRequest.getCountryId())));
    }

    @Test
    @DisplayName("should throw when major not found")
    void updateCandidateEducation_shouldThrow_whenMajorNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateEducationRepository.findById(updateRequest.getId()))
            .willReturn(Optional.of(education));
        given(countryRepository.findById(updateRequest.getCountryId()))
            .willReturn(Optional.of(COUNTRY));
        given(educationMajorRepository.findById(updateRequest.getMajorId()))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> candidateEducationService.updateCandidateEducation(updateRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(updateRequest.getMajorId())));
    }

    @Test
    @DisplayName("should update and save education as expected")
    void updateCandidateEducation_shouldUpdateAndSaveEducation() {
        updateRequest.setCountryId(COUNTRY_ID);
        updateRequest.setEducationType(TYPE);
        updateRequest.setLengthOfCourseYears(LENGTH);
        updateRequest.setInstitution(INSTITUTION);
        updateRequest.setCourseName(COURSE_NAME);
        updateRequest.setYearCompleted(YEAR_COMPLETED);
        updateRequest.setIncomplete(INCOMPLETE);

        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateEducationRepository.findById(updateRequest.getId()))
            .willReturn(Optional.of(education));
        given(countryRepository.findById(updateRequest.getCountryId()))
            .willReturn(Optional.of(COUNTRY));
        given(educationMajorRepository.findById(updateRequest.getMajorId()))
            .willReturn(Optional.of(MAJOR));
        given(candidateEducationRepository.save(education)).willReturn(education);

        candidateEducationService.updateCandidateEducation(updateRequest);

        verify(candidateEducationRepository).save(educationCaptor.capture());
        CandidateEducation result = educationCaptor.getValue();
        verifyEducation(result);

        verify(candidateService).save(candidate, true);
        assertEquals(ADMIN_USER, candidate.getUpdatedBy());
    }

    private static void verifyEducation(CandidateEducation education) {
        assertEquals(COUNTRY, education.getCountry());
        assertEquals(TYPE, education.getEducationType());
        assertEquals(LENGTH, education.getLengthOfCourseYears());
        assertEquals(INSTITUTION, education.getInstitution());
        assertEquals(COURSE_NAME, education.getCourseName());
        assertEquals(YEAR_COMPLETED, education.getYearCompleted());
        assertEquals(INCOMPLETE, education.getIncomplete());
    }

    @Test
    @DisplayName("should throw when user not logged in")
    void deleteCandidateEducation_shouldThrow_whenUserNotLoggedIn() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateEducationService.deleteCandidateEducation(EDUCATION_ID));
    }

    @Test
    @DisplayName("should throw when education not found")
    void deleteCandidateEducation_shouldThrow_whenEducationNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateEducationRepository.findByIdLoadCandidate(EDUCATION_ID)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateEducationService.deleteCandidateEducation(EDUCATION_ID));
    }

    @Test
    @DisplayName("should throw when user not authorised")
    void deleteCandidateEducation_shouldThrow_whenUserNotAuthorised() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateEducationRepository.findByIdLoadCandidate(EDUCATION_ID))
            .willReturn(Optional.of(education));
        given(authService.authoriseLoggedInUser(candidate)).willReturn(false);

        assertThrows(UnauthorisedActionException.class,
            () -> candidateEducationService.deleteCandidateEducation(EDUCATION_ID));
    }

    @Test
    @DisplayName("should delete education and save candidate")
    void deleteCandidateEducation_shouldDeleteEducationAndSaveCandidate() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateEducationRepository.findByIdLoadCandidate(EDUCATION_ID))
            .willReturn(Optional.of(education));
        given(authService.authoriseLoggedInUser(candidate)).willReturn(true);

        candidateEducationService.deleteCandidateEducation(EDUCATION_ID);

        verify(candidateEducationRepository).delete(education);

        verify(candidateService).save(candidate, true);
        assertEquals(ADMIN_USER, candidate.getUpdatedBy());
    }

}
