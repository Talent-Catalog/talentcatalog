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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.api.admin.AdminApiTestUtil;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.request.candidate.UpdateCandidatePersonalRequest;
import org.tctalent.server.request.candidate.citizenship.CreateCandidateCitizenshipRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateCitizenshipService;
import org.tctalent.server.service.db.PartnerService;

@ExtendWith(MockitoExtension.class)
class CandidateServiceImplTest {
    private Partner testPartner;
    private Candidate testCandidate;
    private User testUser;
    private Country testCountry;
    private UpdateCandidatePersonalRequest updateCandidatePersonalRequest;
    private PartnerImpl autoAssignPartner;

    @Mock private PartnerService partnerService;
    @Mock private CountryRepository countryRepository;
    @Mock private AuthService authService;
    @Mock private UserRepository userRepository;
    @Mock private CandidateRepository candidateRepository;
    @Mock private User mockUser;
    @Mock private CandidateCitizenshipService candidateCitizenshipService;
    @Mock private PartnerImpl mockDefaultSourcePartnerImpl;

    @InjectMocks
    @Spy
    CandidateServiceImpl candidateService;

    @BeforeEach
    void setUp() {
        testPartner = new PartnerImpl();
        testPartner = AdminApiTestUtil.getPartner();
        testCandidate = new Candidate();
        testCandidate = AdminApiTestUtil.getCandidate();
        testUser = new User();
        testUser = AdminApiTestUtil.getFullUser();
        testCandidate.setUser(testUser);
        updateCandidatePersonalRequest = new UpdateCandidatePersonalRequest();
        testCountry = new Country();
        testCountry.setId(1L);
        autoAssignPartner = new PartnerImpl();
        autoAssignPartner.setId(123L);
        autoAssignPartner.setAutoAssignable(true);
        autoAssignPartner.setSourceCountries(Set.of(testCountry));
    }

    @Test
    @DisplayName("should reassign new registrant when partner not operational in given country")
    void checkForChangedPartnerShouldReassignWhenCurrentPartnerNotOperationalInGivenCountry() {

        stubUpdatePersonalToReachCheckForChangedPartner();

        // Return candidate location country in which current partner isn't operational.
        Country invalidCountry = new Country();
        invalidCountry.setId(99L);
        given(countryRepository.findById(1L)).willReturn(Optional.of(invalidCountry));

        given(partnerService.getDefaultSourcePartner()).willReturn(mockDefaultSourcePartnerImpl);

        candidateService.updatePersonal(updateCandidatePersonalRequest);

        verify(userRepository).save(testUser);
        Assertions.assertEquals(testUser.getPartner(), mockDefaultSourcePartnerImpl);
    }

    @Test
    @DisplayName("should not reassign new registrant when partner operational in given country")
    void checkForChangedPartnerShouldNotReassignWhenCurrentPartnerOperationalInGivenCountry() {

        stubUpdatePersonalToReachCheckForChangedPartner();

        // Return candidate location country in which current partner is operational.
        given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));

        candidateService.updatePersonal(updateCandidatePersonalRequest);

        verify(userRepository, never()).save(testUser);
        Assertions.assertEquals(testUser.getPartner(), testPartner);
    }

    @Test
    @DisplayName("should not reassign existing candidate (not a new registration) because current "
        + "partner is not operational in given country")
    void checkForChangedPartnerShouldNotReassignExistingCandidate() {

        stubUpdatePersonalToReachCheckForChangedPartner();

        // Return candidate location country in which current partner isn't operational.
        Country invalidCountry = new Country();
        invalidCountry.setId(99L);
        given(countryRepository.findById(1L)).willReturn(Optional.of(invalidCountry));

        updateCandidatePersonalRequest.setIsRegistration(false);
        candidateService.updatePersonal(updateCandidatePersonalRequest);

        verify(userRepository, never()).save(testUser);
        Assertions.assertEquals(testUser.getPartner(), testPartner);
    }

    @Test
    @DisplayName("should reassign existing candidate if there is an auto-assign partner in their "
        + "new country location and they are currently assigned to the default source partner")
    void checkForChangedPartnerShouldReassignFromAutoAssignPartnerToDefaultForExistingCandidate() {

        stubUpdatePersonalToReachCheckForChangedPartner();

        given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));

        // Assign test candidate to default source partner
        testUser.setPartner(mockDefaultSourcePartnerImpl);
        given(mockDefaultSourcePartnerImpl.isDefaultSourcePartner()).willReturn(true);

        given(partnerService.getAutoAssignablePartnerByCountry(testCountry))
            .willReturn(autoAssignPartner);

        updateCandidatePersonalRequest.setIsRegistration(false);
        candidateService.updatePersonal(updateCandidatePersonalRequest);

        verify(userRepository).save(testUser);
        Assertions.assertEquals(testUser.getPartner(), autoAssignPartner);
    }

    @Test
    @DisplayName("should reassign new registrant if there is an auto-assign partner in their "
        + "country location and they are currently assigned to the default source partner")
    void checkForChangedPartnerShouldReassignFromAutoAssignPartnerToDefaultForNewRegistrant() {

        stubUpdatePersonalToReachCheckForChangedPartner();

        given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));

        // Assign test candidate to default source partner
        testUser.setPartner(mockDefaultSourcePartnerImpl);
        given(mockDefaultSourcePartnerImpl.canManageCandidatesInCountry(testCountry))
            .willReturn(true);
        given(mockDefaultSourcePartnerImpl.isDefaultSourcePartner()).willReturn(true);

        given(partnerService.getAutoAssignablePartnerByCountry(testCountry))
            .willReturn(autoAssignPartner);

        updateCandidatePersonalRequest.setIsRegistration(true);
        candidateService.updatePersonal(updateCandidatePersonalRequest);

        verify(userRepository).save(testUser);
        Assertions.assertEquals(testUser.getPartner(), autoAssignPartner);
    }

    /**
     * Factors out stubbing needed to reach + test checkForChangedPartner() within updatePersonal().
     * In order of execution at time of writing.
     */
    private void stubUpdatePersonalToReachCheckForChangedPartner() {
        updateCandidatePersonalRequest.setIsRegistration(true);
        updateCandidatePersonalRequest.setCountryId(1L);
        updateCandidatePersonalRequest.setNationalityId(2L);
        updateCandidatePersonalRequest.setOtherNationalityIds(new Long[0]);

        // Set current partner source country
        testPartner.setSourceCountries(Set.of(testCountry));
        testUser.setPartner((PartnerImpl) testPartner);

        Country stubbedNationality = new Country();
        stubbedNationality.setId(2L);
        given(countryRepository.findById(2L)).willReturn(Optional.of(stubbedNationality));

        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));

        // Gives us our modified testCandidate for the setter block's checkForChangedPartner() call:
        given(userRepository.save(mockUser)).willReturn(testCandidate.getUser());
        given(candidateRepository.findByUserId(null)).willReturn(testCandidate);

        // Handles updateCitizenships() call
        testCandidate.setCandidateCitizenships(Collections.emptyList());
        given(candidateCitizenshipService.createCitizenship(anyLong(), any(
            CreateCandidateCitizenshipRequest.class))).willReturn(null);

        // Handles save() after setter block
        doReturn(testCandidate).when(candidateService).save(any(Candidate.class), eq(true));
    }

}
