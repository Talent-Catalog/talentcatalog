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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
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
import org.tctalent.server.api.admin.AdminApiTestUtil;
import org.tctalent.server.model.db.BrandingInfo;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.model.es.CandidateEs;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.es.CandidateEsRepository;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.PersistenceContextHelper;
import org.tctalent.server.util.html.TextExtracter;

@ExtendWith(MockitoExtension.class)
class CandidateServiceImplTest {
    User user;
    Candidate candidate;
    Page<Candidate> candidatePage;
    PartnerImpl partner;
    CandidateEs candidateEs;

    @Mock PersistenceContextHelper persistenceContextHelper;
    @Mock CandidateRepository candidateRepository;
    @Mock CandidateEsRepository candidateEsRepository;
    @Mock Candidate mockCandidate;
    @Mock CandidateEs mockCandidateEs;
    @Mock TextExtracter textExtracter;

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

        candidateEs = new CandidateEs();
    }

    @Test
    @DisplayName("reassign candidates on page succeeds with valid partner and page")
    void reassignCandidatesOnPageSucceeds() {
        OffsetDateTime testDateTime = OffsetDateTime.now(ZoneOffset.ofHours(10));
        candidate.setUpdatedDate(testDateTime);

        String textSearchId = "text search id";
        candidateEs.setId(textSearchId);


        given(candidateRepository.save(any(Candidate.class))).willReturn(mockCandidate);
        given(mockCandidate.getTextSearchId()).willReturn(textSearchId);
        given(candidateEsRepository.findById(anyString())).willReturn(Optional.of(mockCandidateEs));
        doNothing().when(mockCandidateEs).copy(any(Candidate.class), any(TextExtracter.class));
        given(candidateEsRepository.save(any(CandidateEs.class))).willReturn(mockCandidateEs);
        given(mockCandidateEs.getId()).willReturn(textSearchId);

        candidateService.reassignCandidatesOnPage(candidatePage, partner);

        assertEquals(partner, user.getPartner()); // Verify partner assignment
        verify(candidateService).save(any(Candidate.class), eq(true)); // Verify save called
        verify(persistenceContextHelper).flushAndClearEntityManager(); // Ensure flush
    }

}
