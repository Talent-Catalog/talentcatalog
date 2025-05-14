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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
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
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.util.PersistenceContextHelper;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.request.candidate.UpdateCandidatePersonalRequest;
import org.tctalent.server.request.candidate.citizenship.CreateCandidateCitizenshipRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateCitizenshipService;
import org.tctalent.server.service.db.PartnerService;

@ExtendWith(MockitoExtension.class)
class CandidateServiceImplTest {
    private User user;
    private Candidate candidate;
    private Page<Candidate> candidatePage;
    private PartnerImpl partner;
    private PartnerImpl partner2;
    private Country testCountry;
    private UpdateCandidatePersonalRequest updateCandidatePersonalRequest;
    private PartnerImpl autoAssignPartner;

    @Mock private PersistenceContextHelper persistenceContextHelper;
    @Mock private Candidate mockCandidate;
    @Mock private Page<Candidate> mockCandidatePage;
    @Mock private CandidateRepository candidateRepository;
    @Mock private PartnerService partnerService;
    @Mock private CountryRepository countryRepository;
    @Mock private AuthService authService;
    @Mock private UserRepository userRepository;
    @Mock private User mockUser;
    @Mock private CandidateCitizenshipService candidateCitizenshipService;
    @Mock private PartnerImpl mockDefaultSourcePartnerImpl;

    @Spy
    @InjectMocks
    private CandidateServiceImpl candidateService;

    @BeforeEach
    void setUp() {
        partner = new PartnerImpl();
        partner.setId(1L);
        partner.setName("Test Partner");
        partner2 = new PartnerImpl();
        partner2.setId(2L);
        partner2.setName("Test Partner 2");
        user = new User();
        user.setPartner(partner);
        candidate = new Candidate();
        candidate.setId(1L);
        candidate.setUser(user);
        User user2 = new User();
        user2.setPartner(partner);
        Candidate candidate2 = new Candidate();
        candidate2.setId(2L);
        candidate2.setUser(user2);
        candidatePage = new PageImpl<>(List.of(candidate, candidate2));
        updateCandidatePersonalRequest = new UpdateCandidatePersonalRequest();
        testCountry = new Country();
        testCountry.setId(1L);
        autoAssignPartner = new PartnerImpl();
        autoAssignPartner.setId(123L);
        autoAssignPartner.setAutoAssignable(true);
        autoAssignPartner.setSourceCountries(Set.of(testCountry));
    }

    @Test
    @DisplayName("reassign candidates on page succeeds with valid partner and page")
    void reassignCandidatesOnPageSucceeds() {
        doReturn(mockCandidate).when(candidateService).save(any(Candidate.class), eq(true));

        candidateService.reassignCandidatesOnPage(candidatePage, partner2);

        assertEquals(partner2, user.getPartner()); // Verify partner assignment
        verify(candidateService, times(2))
            .save(any(Candidate.class), eq(true)); // Verify save called
        verify(persistenceContextHelper).flushAndClearEntityManager(); // Ensure flush and clear
    }

    @Test
    @DisplayName("reassign candidates fails with invalid implementation of partner")
    void reassignCandidatesOnPageFailsWithInvalidPartner() {
        Partner invalidPartner = mock(Partner.class);

        assertThrows(
            IllegalArgumentException.class, () ->
            candidateService.reassignCandidatesOnPage(candidatePage, invalidPartner)
        );
        // Shouldn't happen:
        verify(candidateService, never()).save(any(), anyBoolean());
        verify(persistenceContextHelper, never()).flushAndClearEntityManager();
    }

    @Test
    @DisplayName("reassign candidates handles null partner")
    void reassignCandidatesHandlesNullPartner() {
        assertThrows(
            IllegalArgumentException.class, () ->
                candidateService.reassignCandidatesOnPage(candidatePage, null)
        );
        // Shouldn't happen:
        verify(candidateService, never()).save(any(), anyBoolean());
        verify(persistenceContextHelper, never()).flushAndClearEntityManager();
    }

    @Test
    @DisplayName("cleanUpResolvedDuplicates clears potentialDuplicate on resolved candidates")
    void cleanUpResolvedDuplicatesWhenIdsDiffer() {
        long resolvedCandidateId = 42L;

        // Given: candidate ID was previously marked but is not in the new list
        given(candidateRepository.findIdsOfPotentialDuplicateCandidates(null))
            .willReturn(List.of()); // newCandidateIds (empty)
        given(candidateRepository.findIdsOfCandidatesMarkedPotentialDuplicates())
            .willReturn(List.of(resolvedCandidateId)); // previousCandidateIds (1)

        // doReturn() works better for spies than given() - avoids method call altogether.
        doReturn(mockCandidate).when(candidateService).getCandidate(resolvedCandidateId);

        candidateService.cleanUpResolvedDuplicates(); // Act

        // Check that candidate has flag cleared and is saved:
        verify(mockCandidate).setPotentialDuplicate(false);
        verify(candidateService).save(mockCandidate, false);
    }

    @Test
    @DisplayName("cleanUpResolvedDuplicates - no action if no duplicates resolved")
    void cleanUpResolvedDuplicatesNoActionIfNoneResolved() {
        long candidateId1 = 101L;
        long candidateId2 = 102L;

        List<Long> currentDuplicates = List.of(candidateId1, candidateId2);
        List<Long> previouslyMarkedDuplicates = List.of(candidateId1, candidateId2);

        given(candidateRepository.findIdsOfPotentialDuplicateCandidates(null))
            .willReturn(currentDuplicates);
        given(candidateRepository.findIdsOfCandidatesMarkedPotentialDuplicates())
            .willReturn(previouslyMarkedDuplicates);

        candidateService.cleanUpResolvedDuplicates(); // Act

        verify(candidateService, never()).getCandidate(anyLong());
        verify(candidateService, never()).save(any(), anyBoolean());
    }

    @Test
    @DisplayName("cleanUpResolvedDuplicates handles empty lists")
    void cleanUpResolvedDuplicatesHandlesEmptyLists() {
        List<Long> emptyList = List.of();
        given(candidateRepository.findIdsOfPotentialDuplicateCandidates(null))
            .willReturn(emptyList);
        given(candidateRepository.findIdsOfCandidatesMarkedPotentialDuplicates())
            .willReturn(emptyList);

        assertDoesNotThrow(() -> candidateService.cleanUpResolvedDuplicates()); // Act & Assert
    }

    @Test
    @DisplayName("processPotentialDuplicatePage marks as duplicates all candidates on page")
    void processPotentialDuplicatePageMarksAsDuplicatesAllCandidatesOnPage() {
        Candidate mockCandidate = mock(Candidate.class);

        List<Candidate> mockCandidateList = List.of(mockCandidate, mockCandidate, mockCandidate);

        given(mockCandidatePage.getContent()).willReturn(mockCandidateList);

        candidateService.processPotentialDuplicatePage(mockCandidatePage); // Act

        verify(mockCandidate, times(3)).setPotentialDuplicate(true);
        verify(candidateService, times(3))
            .save(mockCandidate, false);
    }

    @Test
    @DisplayName("processPotentialDuplicatePage skips empty page w no exceptions")
    void processPotentialDuplicatePageSkipsEmptyPage() {
        Page<Candidate> spyCandidatePage = Mockito.spy(new PageImpl<>(List.of()));

        // Act & Assert
        assertDoesNotThrow(() -> candidateService.processPotentialDuplicatePage(spyCandidatePage));

        verify(spyCandidatePage, never()).getContent();
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

      verify(userRepository).save(user);
      Assertions.assertEquals(user.getPartner(), mockDefaultSourcePartnerImpl);
    }

    @Test
    @DisplayName("should not reassign new registrant when partner operational in given country")
    void checkForChangedPartnerShouldNotReassignWhenCurrentPartnerOperationalInGivenCountry() {

      stubUpdatePersonalToReachCheckForChangedPartner();

      // Return candidate location country in which current partner is operational.
      given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));

      candidateService.updatePersonal(updateCandidatePersonalRequest);

      verify(userRepository, never()).save(user);
      Assertions.assertEquals(user.getPartner(), partner);
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

      verify(userRepository, never()).save(user);
      Assertions.assertEquals(user.getPartner(), partner);
    }

    @Test
    @DisplayName("should reassign existing candidate if there is an auto-assign partner in their "
        + "new country location and they are currently assigned to the default source partner")
    void checkForChangedPartnerShouldReassignFromAutoAssignPartnerToDefaultForExistingCandidate() {

      stubUpdatePersonalToReachCheckForChangedPartner();

      given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));

      // Assign test candidate to default source partner
      user.setPartner(mockDefaultSourcePartnerImpl);
      given(mockDefaultSourcePartnerImpl.isDefaultSourcePartner()).willReturn(true);

      given(partnerService.getAutoAssignablePartnerByCountry(testCountry))
          .willReturn(autoAssignPartner);

      updateCandidatePersonalRequest.setIsRegistration(false);
      candidateService.updatePersonal(updateCandidatePersonalRequest);

      verify(userRepository).save(user);
      Assertions.assertEquals(user.getPartner(), autoAssignPartner);
    }

    @Test
    @DisplayName("should reassign new registrant if there is an auto-assign partner in their "
        + "country location and they are currently assigned to the default source partner")
    void checkForChangedPartnerShouldReassignFromAutoAssignPartnerToDefaultForNewRegistrant() {

      stubUpdatePersonalToReachCheckForChangedPartner();

      given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));

      // Assign test candidate to default source partner
      user.setPartner(mockDefaultSourcePartnerImpl);
      given(mockDefaultSourcePartnerImpl.canManageCandidatesInCountry(testCountry))
          .willReturn(true);
      given(mockDefaultSourcePartnerImpl.isDefaultSourcePartner()).willReturn(true);

      given(partnerService.getAutoAssignablePartnerByCountry(testCountry))
          .willReturn(autoAssignPartner);

      updateCandidatePersonalRequest.setIsRegistration(true);
      candidateService.updatePersonal(updateCandidatePersonalRequest);

      verify(userRepository).save(user);
      Assertions.assertEquals(user.getPartner(), autoAssignPartner);
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
      partner.setSourcePartner(true);
      partner.setSourceCountries(Set.of(testCountry));
      user.setPartner(partner);

      Country stubbedNationality = new Country();
      stubbedNationality.setId(2L);
      given(countryRepository.findById(2L)).willReturn(Optional.of(stubbedNationality));

      given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));

      // Gives us our modified testCandidate for the setter block's checkForChangedPartner() call:
      given(userRepository.save(mockUser)).willReturn(candidate.getUser());
      given(candidateRepository.findByUserId(null)).willReturn(candidate);

      // Handles updateCitizenships() call
      candidate.setCandidateCitizenships(Collections.emptyList());
      given(candidateCitizenshipService.createCitizenship(anyLong(), any(
          CreateCandidateCitizenshipRequest.class))).willReturn(null);

      // Handles save() after setter block
      doReturn(candidate).when(candidateService).save(any(Candidate.class), eq(true));
    }

}
