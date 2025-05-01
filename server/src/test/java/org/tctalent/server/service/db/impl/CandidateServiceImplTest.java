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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.util.PersistenceContextHelper;

@ExtendWith(MockitoExtension.class)
class CandidateServiceImplTest {
    User user;
    User user2;
    Candidate candidate;
    Candidate candidate2;
    Page<Candidate> candidatePage;
    PartnerImpl partner;

    @Mock PersistenceContextHelper persistenceContextHelper;
    @Mock Candidate mockCandidate;
    @Mock CandidateRepository candidateRepository;

    @Spy
    @InjectMocks
    CandidateServiceImpl candidateService;

    @BeforeEach
    void setUp() {
        user = new User();
        candidate = new Candidate();
        candidate.setId(1L);
        candidate.setUser(user);
        user2 = new User();
        candidate2 = new Candidate();
        candidate2.setId(2L);
        candidate2.setUser(user2);
        candidatePage = new PageImpl<>(List.of(candidate, candidate2));
        partner = new PartnerImpl();
        partner.setId(1L);
        partner.setName("Test Partner");
    }

    @Test
    @DisplayName("reassign candidates on page succeeds with valid partner and page")
    void reassignCandidatesOnPageSucceeds() {
        doReturn(mockCandidate).when(candidateService).save(any(Candidate.class), eq(true));

        candidateService.reassignCandidatesOnPage(candidatePage, partner);

        assertEquals(partner, user.getPartner()); // Verify partner assignment
        verify(candidateService, times(2)).save(any(Candidate.class), eq(true)); // Verify save called
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
    @DisplayName("clears potentialDuplicate on resolved candidates")
    void cleansUpResolvedDuplicatesWhenIdsDiffer() {
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
    @DisplayName("no action if all marked duplicates are still valid")
    void cleanUpResolvedDuplicatesNoActionIfNoneResolved() {
        long candidateId1 = 101L;
        long candidateId2 = 102L;

        List<Long> currentDuplicates = List.of(candidateId1, candidateId2);
        List<Long> previouslyMarkedDuplicates = List.of(candidateId1, candidateId2);

        doReturn(currentDuplicates)
            .when(candidateRepository)
            .findIdsOfPotentialDuplicateCandidates(null);

        doReturn(previouslyMarkedDuplicates)
            .when(candidateRepository)
            .findIdsOfCandidatesMarkedPotentialDuplicates();

        candidateService.cleanUpResolvedDuplicates(); // Act

        verify(candidateService, never()).getCandidate(anyLong());
        verify(candidateService, never()).save(any(), anyBoolean());
    }

}
