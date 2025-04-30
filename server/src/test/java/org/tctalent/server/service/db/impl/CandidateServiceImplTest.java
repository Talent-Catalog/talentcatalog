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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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
    @Mock private Partner invalidPartner;

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
        assertThrows(
            IllegalArgumentException.class, () ->
            candidateService.reassignCandidatesOnPage(candidatePage, invalidPartner)
        );
        verify(candidateService, never()).save(any(), anyBoolean());
        verify(persistenceContextHelper, never()).flushAndClearEntityManager();
    }

    @Test
    @DisplayName("reassign candidates handles empty page")
    void reassignCandidatesHandlesEmptyPage() {
        Page<Candidate> emptyPage = new PageImpl<>(List.of());

        candidateService.reassignCandidatesOnPage(emptyPage, partner);
    }

    @Test
    @DisplayName("reassign candidates handles null partner")
    void reassignCandidatesHandlesNullPartner() {
        assertThrows(
            IllegalArgumentException.class, () ->
                candidateService.reassignCandidatesOnPage(candidatePage, null)
        );
        verify(candidateService, never()).save(any(), anyBoolean());
        verify(persistenceContextHelper, never()).flushAndClearEntityManager();
    }

}
