/*
 * Copyright (c) 2024 Talent Catalog.
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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
import org.tctalent.server.util.PersistenceContextHelper;

@ExtendWith(MockitoExtension.class)
class CandidateServiceImplTest {
    User user;
    Candidate candidate;
    Page<Candidate> candidatePage;
    PartnerImpl partner;

    @Mock PersistenceContextHelper persistenceContextHelper;
    @Mock Candidate mockCandidate;

    @Spy
    @InjectMocks
    CandidateServiceImpl candidateService;

    @BeforeEach
    void setUp() {
        Long candidateId = 1L;
        user = new User();
        candidate = new Candidate();
        candidate.setId(candidateId);
        candidate.setUser(user);
        candidatePage = new PageImpl<>(List.of(candidate));

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
        verify(candidateService).save(any(Candidate.class), eq(true)); // Verify save called
        verify(persistenceContextHelper).flushAndClearEntityManager(); // Ensure flush and clear
    }

}
