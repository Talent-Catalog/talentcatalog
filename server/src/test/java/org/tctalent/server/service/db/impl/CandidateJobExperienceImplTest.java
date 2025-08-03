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
import static org.tctalent.server.data.CandidateTestData.getCandidate2;
import static org.tctalent.server.data.CountryTestData.JORDAN;
import static org.tctalent.server.data.UserTestData.getAdminUser;

import java.time.LocalDate;
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
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateJobExperienceRepository;
import org.tctalent.server.repository.db.CandidateOccupationRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tctalent.server.request.work.experience.SearchJobExperienceRequest;
import org.tctalent.server.request.work.experience.UpdateJobExperienceRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;

@ExtendWith(MockitoExtension.class)
class CandidateJobExperienceImplTest {

    private CreateJobExperienceRequest createRequest;
    private CandidateJobExperience experience;
    private Page<CandidateJobExperience> experiencePage;
    private UpdateJobExperienceRequest updateRequest;
    private Candidate candidate;
    private SearchJobExperienceRequest searchRequest;
    private long candidateId;
    private User candidateUser;

    private static final long EXPERIENCE_ID = 33L;
    private static final long COUNTRY_ID = 66L;
    private static final long OCCUPATION_ID = 77L;
    private static final User ADMIN_USER = getAdminUser();
    private static final Country COUNTRY = JORDAN;
    private static final String COMPANY_NAME = "Company";
    private static final String ROLE = "Role";
    private static final LocalDate START_DATE = LocalDate.of(2020, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2022, 2, 1);
    private static final boolean FULL_TIME = true;
    private static final boolean PAID = true;
    private static final String DESCRIPTION = "Description";
    private static final CandidateOccupation OCCUPATION = new CandidateOccupation();
    private static final long ALT_OCCUPATION_ID = 999L;
    private static final CandidateOccupation ALT_OCCUPATION = new CandidateOccupation();

    @Mock private CandidateJobExperienceRepository jobExperienceRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private CandidateRepository candidateRepository;
    @Mock private CandidateService candidateService;
    @Mock private CandidateOccupationRepository occupationRepository;
    @Mock private AuthService authService;

    @Captor private ArgumentCaptor<CandidateJobExperience> jobExperienceCaptor;

    @InjectMocks
    CandidateJobExperienceImpl jobExperienceService;

    @BeforeEach
    void setUp() {
        candidate = getCandidate();
        candidateId = candidate.getId();
        candidateUser = getAdminUser();
        createRequest = new CreateJobExperienceRequest();
        createRequest.setCountryId(COUNTRY_ID);
        createRequest.setCandidateOccupationId(OCCUPATION_ID);
        experience = new CandidateJobExperience();
        experience.setId(EXPERIENCE_ID);
        experience.setCandidate(candidate);
        List<CandidateJobExperience> experienceList = List.of(experience, experience);
        experiencePage = new PageImpl<>(experienceList);
        updateRequest = new UpdateJobExperienceRequest();
        updateRequest.setId(EXPERIENCE_ID);
        updateRequest.setCountryId(COUNTRY_ID);
        updateRequest.setCandidateOccupationId(ALT_OCCUPATION_ID);
        searchRequest = new SearchJobExperienceRequest();
    }

    @Test
    @DisplayName("should return page of experiences when candidate id provided")
    void searchCandidateJobExperience_shouldReturnPageOfExperiences_whenCandidateIdProvided() {
        searchRequest.setCandidateId(candidateId);

        given(jobExperienceRepository.findByCandidateId(
            searchRequest.getCandidateId(),
            searchRequest.getPageRequest()
        )).willReturn(experiencePage);

        assertEquals(experiencePage, jobExperienceService.searchCandidateJobExperience(searchRequest));
    }

    @Test
    @DisplayName("should return empty page when candidate id provided")
    void searchCandidateJobExperience_shouldReturnEmptyPage_whenCandidateIdProvided() {
        searchRequest.setCandidateId(candidateId);
        experiencePage = Page.empty();

        given(jobExperienceRepository.findByCandidateId(
            searchRequest.getCandidateId(),
            searchRequest.getPageRequest()
        )).willReturn(experiencePage);

        assertEquals(experiencePage, jobExperienceService.searchCandidateJobExperience(searchRequest));
    }

    @Test
    @DisplayName("should return page of experiences when candidate id not provided")
    void searchCandidateJobExperience_shouldReturnPageOfExperiences_whenCandidateIdNotProvided() {
        given(jobExperienceRepository.findByCandidateOccupationId(
            searchRequest.getCandidateOccupationId(),
            searchRequest.getPageRequest()
        )).willReturn(experiencePage);

        assertEquals(experiencePage, jobExperienceService.searchCandidateJobExperience(searchRequest));
    }

    @Test
    @DisplayName("should return empty page when candidate id not provided")
    void searchCandidateJobExperience_shouldReturnEmptyPage_whenCandidateIdNotProvided() {
        experiencePage = Page.empty();

        given(jobExperienceRepository.findByCandidateOccupationId(
            searchRequest.getCandidateOccupationId(),
            searchRequest.getPageRequest()
        )).willReturn(experiencePage);

        assertEquals(experiencePage, jobExperienceService.searchCandidateJobExperience(searchRequest));
    }

    @Test
    @DisplayName("should throw when id provided and candidate not found")
    void createCandidateJobExperience_shouldThrow_whenIdProvidedAndCandidateNotFound() {
        createRequest.setCandidateId(candidateId);

        given(candidateRepository.findById(candidateId)).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> jobExperienceService.createCandidateJobExperience(createRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(candidateId)));
    }

    @Test
    @DisplayName("should throw when id not provided and candidate not logged in")
    void createCandidateJobExperience_shouldThrow_whenIdNotProvidedAndCandidateNotLoggedIn() {
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.empty());

        Exception ex = assertThrows(InvalidSessionException.class,
            () -> jobExperienceService.createCandidateJobExperience(createRequest));

        assertTrue(ex.getMessage().contains("Not logged in"));
    }

    @Test
    @DisplayName("should throw when country not found")
    void createCandidateJobExperience_shouldThrow_whenCountryNotFound() {
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(candidate));
        given(countryRepository.findById(createRequest.getCountryId()))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> jobExperienceService.createCandidateJobExperience(createRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(COUNTRY_ID)));
    }

    @Test
    @DisplayName("should throw when occupation not found")
    void createCandidateJobExperience_shouldThrow_whenOccupationNotFound() {
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(candidate));
        given(countryRepository.findById(createRequest.getCountryId()))
            .willReturn(Optional.of(COUNTRY));
        given(occupationRepository.findById(createRequest.getCandidateOccupationId()))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> jobExperienceService.createCandidateJobExperience(createRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(OCCUPATION_ID)));
    }

    @Test
    @DisplayName("should create experience as expected")
    void createCandidateJobExperience() {
        createRequest.setCompanyName(COMPANY_NAME);
        createRequest.setRole(ROLE);
        createRequest.setStartDate(START_DATE);
        createRequest.setEndDate(END_DATE);
        createRequest.setFullTime(FULL_TIME);
        createRequest.setPaid(PAID);
        createRequest.setDescription(DESCRIPTION);

        given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(candidate));
        given(countryRepository.findById(createRequest.getCountryId()))
            .willReturn(Optional.of(COUNTRY));
        given(occupationRepository.findById(createRequest.getCandidateOccupationId()))
            .willReturn(Optional.of(OCCUPATION));
        given(authService.getLoggedInUser()).willReturn(Optional.of(candidateUser));

        jobExperienceService.createCandidateJobExperience(createRequest);

        verify(jobExperienceRepository).save(jobExperienceCaptor.capture());
        CandidateJobExperience result = jobExperienceCaptor.getValue();
        assertEquals(candidate, result.getCandidate());
        verifyExperience(result, OCCUPATION);

        verify(candidateService).save(candidate, true, true);
        assertEquals(candidateUser, candidate.getUpdatedBy());
    }

    @Test
    @DisplayName("should throw when id not provided and candidate not logged in")
    void updateCandidateJobExperience_shouldThrow_whenIdNotProvidedAndCandidateNotLoggedIn() {
        given(authService.getLoggedInCandidate()).willReturn(null);

        assertThrows(InvalidSessionException.class,
            () -> jobExperienceService.updateCandidateJobExperience(updateRequest));
    }

    @Test
    @DisplayName("should throw when experience not found")
    void updateCandidateJobExperience_shouldThrow_whenExperienceNotFound() {
        given(authService.getLoggedInCandidate()).willReturn(candidate);
        given(jobExperienceRepository.findByIdLoadCandidateOccupation(
            updateRequest.getId())).willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> jobExperienceService.updateCandidateJobExperience(updateRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(EXPERIENCE_ID)));
    }

    @Test
    @DisplayName("should throw when country not found")
    void updateCandidateJobExperience_shouldThrow_whenCountryNotFound() {
        given(authService.getLoggedInCandidate()).willReturn(candidate);
        given(jobExperienceRepository.findByIdLoadCandidateOccupation(
            updateRequest.getId())).willReturn(Optional.of(experience));
        given(countryRepository.findById(updateRequest.getCountryId()))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> jobExperienceService.updateCandidateJobExperience(updateRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(COUNTRY_ID)));
    }

    @Test
    @DisplayName("should throw when occupation not found")
    void updateCandidateJobExperience_shouldThrow_whenOccupationNotFound() {
        given(authService.getLoggedInCandidate()).willReturn(candidate);
        given(jobExperienceRepository.findByIdLoadCandidateOccupation(
            updateRequest.getId())).willReturn(Optional.of(experience));
        given(countryRepository.findById(updateRequest.getCountryId()))
            .willReturn(Optional.of(COUNTRY));
        given(occupationRepository.findById(updateRequest.getCandidateOccupationId()))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> jobExperienceService.updateCandidateJobExperience(updateRequest));

        assertTrue(ex.getMessage().contains(String.valueOf(updateRequest.getCandidateOccupationId())));
    }

    @Test
    @DisplayName("should update as expected")
    void updateCandidateJobExperience() {
        updateRequest.setCompanyName(COMPANY_NAME);
        updateRequest.setRole(ROLE);
        updateRequest.setStartDate(START_DATE);
        updateRequest.setEndDate(END_DATE);
        updateRequest.setFullTime(FULL_TIME);
        updateRequest.setPaid(PAID);
        updateRequest.setDescription(DESCRIPTION);

        given(authService.getLoggedInCandidate()).willReturn(candidate);
        given(jobExperienceRepository.findByIdLoadCandidateOccupation(
            updateRequest.getId())).willReturn(Optional.of(experience));
        given(countryRepository.findById(updateRequest.getCountryId()))
            .willReturn(Optional.of(COUNTRY));
        given(occupationRepository.findById(updateRequest.getCandidateOccupationId()))
            .willReturn(Optional.of(ALT_OCCUPATION));
        given(authService.getLoggedInUser()).willReturn(Optional.of(candidateUser));
        given(jobExperienceRepository.save(experience)).willReturn(experience);

        jobExperienceService.updateCandidateJobExperience(updateRequest);

        verify(jobExperienceRepository).save(experience);
        verifyExperience(experience, ALT_OCCUPATION);

        verify(candidateService).save(candidate, true, true);
        assertEquals(candidateUser, candidate.getUpdatedBy());
    }

    private static void verifyExperience(
        CandidateJobExperience result,
        CandidateOccupation expectedOccupation
    ) {
        assertEquals(COUNTRY, result.getCountry());
        assertEquals(expectedOccupation, result.getCandidateOccupation());
        assertEquals(COMPANY_NAME, result.getCompanyName());
        assertEquals(ROLE, result.getRole());
        assertEquals(START_DATE, result.getStartDate());
        assertEquals(END_DATE, result.getEndDate());
        assertEquals(FULL_TIME, result.getFullTime());
        assertEquals(PAID, result.getPaid());
        assertEquals(DESCRIPTION, result.getDescription());
    }

    @Test
    @DisplayName("should throw when user not logged in")
    void deleteCandidateJobExperience_shouldThrow_whenUserNotLoggedIn() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> jobExperienceService.deleteCandidateJobExperience(EXPERIENCE_ID));
    }

    @Test
    @DisplayName("should throw when experience not found")
    void deleteCandidateJobExperience_shouldThrow_whenExperienceNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(jobExperienceRepository.findByIdLoadCandidate(EXPERIENCE_ID))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> jobExperienceService.deleteCandidateJobExperience(EXPERIENCE_ID));

        assertTrue(ex.getMessage().contains(String.valueOf(EXPERIENCE_ID)));
    }

    @Test
    @DisplayName("should throw when request from admin portal and candidate not found")
    void deleteCandidateJobExperience_shouldThrow_whenRequestFromAdminPortalAndCandidateNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(jobExperienceRepository.findByIdLoadCandidate(EXPERIENCE_ID))
            .willReturn(Optional.of(experience));
        given(authService.hasAdminPrivileges(ADMIN_USER.getRole())).willReturn(true);
        given(candidateRepository.findById(experience.getCandidate().getId()))
            .willReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchObjectException.class,
            () -> jobExperienceService.deleteCandidateJobExperience(EXPERIENCE_ID));

        assertTrue(ex.getMessage().contains(String.valueOf(experience.getCandidate().getId())));
    }

    @Test
    @DisplayName("should throw when request from candidate who is not owner of experience")
    void deleteCandidateJobExperience_shouldThrow_whenRequestFromCandidateWhoIsNotOwnerOfExperience() {
        Candidate altCandidate = getCandidate2();
        given(authService.getLoggedInUser()).willReturn(Optional.of(altCandidate.getUser()));
        given(jobExperienceRepository.findByIdLoadCandidate(EXPERIENCE_ID))
            .willReturn(Optional.of(experience));
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(altCandidate));

        Exception ex = assertThrows(InvalidCredentialsException.class,
            () -> jobExperienceService.deleteCandidateJobExperience(EXPERIENCE_ID));

        assertEquals("You do not have permission to perform that action", ex.getMessage());
    }

    @Test
    @DisplayName("should delete as expected for admin user")
    void deleteCandidateJobExperience_whenDeletingUserIsAdmin() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(jobExperienceRepository.findByIdLoadCandidate(EXPERIENCE_ID))
            .willReturn(Optional.of(experience));
        given(authService.hasAdminPrivileges(ADMIN_USER.getRole())).willReturn(true);
        given(candidateRepository.findById(experience.getCandidate().getId()))
            .willReturn(Optional.of(candidate));

        jobExperienceService.deleteCandidateJobExperience(EXPERIENCE_ID);

        verify(jobExperienceRepository).delete(experience);
        assertEquals(ADMIN_USER, candidate.getUpdatedBy());
        verify(candidateService).save(candidate, true, true);
    }

    @Test
    @DisplayName("should delete as expected for candidate user")
    void deleteCandidateJobExperience_whenDeletingUserIsCandidate() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(candidateUser));
        given(jobExperienceRepository.findByIdLoadCandidate(EXPERIENCE_ID))
            .willReturn(Optional.of(experience));
        given(authService.hasAdminPrivileges(candidateUser.getRole())).willReturn(false);
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(candidate));

        jobExperienceService.deleteCandidateJobExperience(EXPERIENCE_ID);

        verify(jobExperienceRepository).delete(experience);
        assertEquals(candidateUser, candidate.getUpdatedBy());
        verify(candidateService).save(candidate, true, true);
    }

}
