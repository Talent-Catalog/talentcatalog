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

import java.util.Collections;
import java.util.List;
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
import org.tctalent.server.model.db.CandidateStatus;
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
import org.tctalent.server.util.PersistenceContextHelper;

@ExtendWith(MockitoExtension.class)
class CandidateServiceImplTest {
    private User user;
    private User testUser2;
    private Candidate candidate;
    private Page<Candidate> candidatePage;
    private PartnerImpl partner;
    private PartnerImpl partner2;
    private PartnerImpl partner3;
    private Country testCountry;
    private UpdateCandidatePersonalRequest updateCandidatePersonalRequest;
    private PartnerImpl autoAssignPartner;

    @Mock private PersistenceContextHelper persistenceContextHelper;

    //TODO JC I don't think we need a mockCandidate. Just use a normal candidate object.
    @Mock private Candidate mockCandidate;
    @Mock private Page<Candidate> mockCandidatePage;
    @Mock private CandidateRepository candidateRepository;
    @Mock private PartnerService partnerService;
    @Mock private CountryRepository countryRepository;
    @Mock private AuthService authService;
    @Mock private UserRepository userRepository;
    @Mock private CandidateCitizenshipService candidateCitizenshipService;

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
        partner3 = new PartnerImpl();
        partner3.setId(3L);
        partner3.setSourcePartner(true);
        partner3.setName("Test Partner 3");

        user = new User();
        user.setPartner(partner);
        testUser2 = new User();

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
        autoAssignPartner.setName("Auto Assign Partner");
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
    @DisplayName("should not reassign registered candidate")
    void reassignPartnerIfNeeded_shouldNotReassignExistingCandidate() {
        stubUpdatePersonalToReachReassignPartnerIfNeeded(CandidateStatus.pending); // Existing profile
        given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));

        candidateService.updatePersonal(updateCandidatePersonalRequest); // When

        verify(userRepository, never()).save(user);
        Assertions.assertEquals(user.getPartner(), partner);
    }

    /*
      Candidate partner reassignment tests summary.

      Inputs:
      PActs - current partner acts in new country
      PDefault - current partner is default source partner
      CHasPartner - new country has assigned partner

      * 0. PActs=0 PDefault=0 CHasPartner=0 - Assign default source partner
      * 1. PActs=0 PDefault=0 CHasPartner=1 - Assign country partner
      X 2. PActs=0 PDefault=1 CHasPartner=0 : Not possible. PDefault=1 => PActs=1
      X 3. PActs=0 PDefault=1 CHasPartner=1 : Not possible. PDefault=1 => PActs=1
      * 4. PActs=1 PDefault=0 CHasPartner=0 - No partner change needed
      * 5. PActs=1 PDefault=0 CHasPartner=1 - No partner change needed
      * 6. PActs=1 PDefault=1 CHasPartner=0 - No partner change needed
      * 7. PActs=1 PDefault=1 CHasPartner=1 - Assign country partner

      From the above there are only 6 cases to test
     */

    @Test
    @DisplayName("0-should reassign new registrant to default source partner when there is no "
        + "auto-assign partner and current partner is not operational in their location")
    void reassignPartnerIfNeeded_shouldAssignDefault_whenCurrentPartnerInvalidAndNoAutoAssign() {
        stubUpdatePersonalToReachReassignPartnerIfNeeded(CandidateStatus.draft); // New registrant
        // Current partner invalid - not mocking to also test Partner.canManageCandidatesInCountry():
        Country invalidCountry = new Country();
        invalidCountry.setId(99L);
        given(countryRepository.findById(1L)).willReturn(Optional.of(invalidCountry));

        // No auto-assign partner:
        given(partnerService.getAutoAssignablePartnerByCountry(invalidCountry)).willReturn(null);
        given(partnerService.getDefaultSourcePartner()).willReturn(partner3);

        candidateService.updatePersonal(updateCandidatePersonalRequest); // When

        verify(userRepository).save(user);
        Assertions.assertEquals(user.getPartner(), partner3);
    }

    @Test
    @DisplayName("1-should reassign to auto-assign partner (if exists) when current partner is not "
        + "operational in the given country location")
    void reassignPartnerIfNeeded_shouldAssignAutoAssignPartner_whenCurrentPartnerInvalid() {
        stubUpdatePersonalToReachReassignPartnerIfNeeded(CandidateStatus.draft); // New registrant
        user.setPartner(partner3);
        given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));
        given(partnerService.getAutoAssignablePartnerByCountry(testCountry))
            .willReturn(autoAssignPartner);

        candidateService.updatePersonal(updateCandidatePersonalRequest); // When

        verify(userRepository).save(user);
        Assertions.assertEquals(user.getPartner(), autoAssignPartner);
    }

    @Test
    @DisplayName("4-should not reassign new registrant when current partner is operational in the "
        + "given country location (and is not the default source partner)")
    void reassignPartnerIfNeeded_shouldNotReassign_whenCurrentPartnerIsValidAndNotDefault() {
        stubUpdatePersonalToReachReassignPartnerIfNeeded(CandidateStatus.draft); // New registrant
        given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));

        candidateService.updatePersonal(updateCandidatePersonalRequest); // When

        verify(userRepository, never()).save(user);
        Assertions.assertEquals(user.getPartner(), partner);
    }

    @Test
    @DisplayName("5-should not reassign when current partner can manage country"
        + " and current partner is not default partner")
    void reassignPartnerIfNeeded_shouldNotReassign_whenCurrentPartnerIsAssigned_andCurrentPartnerNotDefault() {
        stubUpdatePersonalToReachReassignPartnerIfNeeded(CandidateStatus.draft); // New registrant
        user.setPartner(partner3);
        partner3.setDefaultSourcePartner(false);
        partner3.setSourceCountries(Set.of(testCountry));
        given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));

        candidateService.updatePersonal(updateCandidatePersonalRequest); // When

        verify(userRepository, never()).save(user);
        Assertions.assertEquals(user.getPartner(), partner3);
    }

    @Test
    @DisplayName("6-should not reassign or unnecessarily write to DB when current partner is default "
        + "and there's no auto-assign partner for the given country location")
    void reassignPartnerIfNeeded_shouldNotReassign_whenNoAutoAssignAndCurrentPartnerIsDefault() {
        stubUpdatePersonalToReachReassignPartnerIfNeeded(CandidateStatus.draft); // New registrant
        user.setPartner(partner3);
        partner3.setDefaultSourcePartner(true);
        partner3.setSourceCountries(Set.of(testCountry));
        given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));

        //Stubbed partnerService getAutoAssignablePartnerByCountry will always return null
        //indicating that there is no auto assign partner for the new country.

        candidateService.updatePersonal(updateCandidatePersonalRequest); // When

        verify(userRepository, never()).save(user);
        Assertions.assertEquals(user.getPartner(), partner3);
    }

    @Test
    @DisplayName("7-should reassign when current partner is default "
        + "and there is an auto-assign partner for the given country location")
    void reassignPartnerIfNeeded_shouldReassign_whenAutoAssignAndCurrentPartnerIsDefault() {
        stubUpdatePersonalToReachReassignPartnerIfNeeded(CandidateStatus.draft); // New registrant
        user.setPartner(partner3);
        partner3.setDefaultSourcePartner(true);

        given(countryRepository.findById(1L)).willReturn(Optional.of(testCountry));
        given(partnerService.getAutoAssignablePartnerByCountry(testCountry))
            .willReturn(autoAssignPartner);

        candidateService.updatePersonal(updateCandidatePersonalRequest); // When

        verify(userRepository).save(user);
        Assertions.assertEquals(user.getPartner(), autoAssignPartner);
    }

    /**
    * Factors out stubbing needed to reach + test reassignPartnerIfNeeded() within updatePersonal().
    * Set up so that user's current partner is operational in their given country location.
    * @param candidateStatus {@code CandidateStatus} can be passed to suit test scenario
    */
    private void stubUpdatePersonalToReachReassignPartnerIfNeeded(CandidateStatus candidateStatus) {
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

        given(authService.getLoggedInUser()).willReturn(Optional.of(testUser2));

        // Gives us our modified testCandidate for the setter block's reassignPartnerIfNeeded() call:
        given(userRepository.save(testUser2)).willReturn(candidate.getUser());
        candidate.setStatus(candidateStatus);
        given(candidateRepository.findByUserId(null)).willReturn(candidate);

        // Handles updateCitizenships() call
        candidate.setCandidateCitizenships(Collections.emptyList());
        given(candidateCitizenshipService.createCitizenship(anyLong(), any(
            CreateCandidateCitizenshipRequest.class))).willReturn(null);

        // Handles save() after setter block
        doReturn(candidate).when(candidateService).save(any(Candidate.class), eq(true));
    }

}
