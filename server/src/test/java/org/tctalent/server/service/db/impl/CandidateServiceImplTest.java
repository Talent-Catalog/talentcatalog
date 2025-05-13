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
    private UpdateCandidatePersonalRequest updateCandidatePersonalRequest;

    @Mock private PartnerService partnerService;
    @Mock private CountryRepository countryRepository;
    @Mock private AuthService authService;
    @Mock private UserRepository userRepository;
    @Mock private CandidateRepository candidateRepository;
    @Mock private User mockUser;
    @Mock private CandidateCitizenshipService candidateCitizenshipService;
    @Mock private PartnerImpl defaultSourcePartnerImpl;

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
    }

    @Test
    @DisplayName("should reassign when current partner not operational in given country")
    void checkForChangedPartnerShouldReassignWhenCurrentPartnerNotOperationalInGivenCountry() {

        stubUpdatePersonalToReachCheckForChangedPartner();

        // Set candidate location to country in which current partner isn't operational, and return.
        Country candidateCountryLocation = new Country();
        candidateCountryLocation.setId(1L);
        given(countryRepository.findById(1L)).willReturn(Optional.of(candidateCountryLocation));

        candidateService.updatePersonal(updateCandidatePersonalRequest);

        verify(userRepository).save(testUser);
        Assertions.assertEquals(testUser.getPartner(), defaultSourcePartnerImpl);
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
        Country partnerSourceCountry = new Country();
        partnerSourceCountry.setId(2L);
        testPartner.setSourceCountries(Set.of(partnerSourceCountry));
        testUser.setPartner((PartnerImpl) testPartner); // Reassign to modified version

        Country stubbedNationality = new Country();
        stubbedNationality.setId(2L);
        given(countryRepository.findById(2L)).willReturn(Optional.of(stubbedNationality));

        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));

        // Gives us our modified testCandidate for the setter block's checkForChangedPartner() call:
        given(userRepository.save(mockUser)).willReturn(testCandidate.getUser());
        given(candidateRepository.findByUserId(null)).willReturn(testCandidate);

        given(partnerService.getDefaultSourcePartner()).willReturn(defaultSourcePartnerImpl);

        // Handles updateCitizenships() call
        testCandidate.setCandidateCitizenships(Collections.emptyList());
        given(candidateCitizenshipService.createCitizenship(anyLong(), any(
            CreateCandidateCitizenshipRequest.class))).willReturn(null);

        // Handles save() after setter block
        doReturn(testCandidate).when(candidateService).save(any(Candidate.class), eq(true));
    }

    // TODO
    //  - not new registration
    //  - current partner does handle candidate country location
    //  - the other update method passes false for isNewRegistration
    //  - verify other desired behaviour of checkForChangedPartner()

}
