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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.UserTestData.getAdminUser;

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
import org.springframework.data.domain.PageRequest;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.task.UploadType;
import org.tctalent.server.repository.db.CandidateAttachmentRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.PagedSearchRequest;
import org.tctalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tctalent.server.request.attachment.ListByUploadTypeRequest;
import org.tctalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;

@ExtendWith(MockitoExtension.class)
public class CandidateAttachmentsServiceImplTest {

    private CandidateAttachment attachment;
    private Page<CandidateAttachment> attachmentPage;
    private List<CandidateAttachment> attachmentList;
    private CreateCandidateAttachmentRequest createRequest;

    private final static long candidateId = 99L;
    private final static Candidate candidate = getCandidate();
    private final static User adminUser = getAdminUser();
    private final static String name = "name";
    private final static String location = "www.link.com";

    private @Mock CandidateAttachmentRepository candidateAttachmentRepository;
    private @Mock AuthService authService;
    private @Mock CandidateRepository candidateRepository;
    private @Mock CandidateService candidateService;

    private @Captor ArgumentCaptor<CandidateAttachment> attachmentCaptor;

    @InjectMocks
    CandidateAttachmentsServiceImpl candidateAttachmentsService;

    @BeforeEach
    void setUp() {
        attachment = new CandidateAttachment();
        attachmentPage = new PageImpl<>(List.of(attachment, attachment));
        attachmentList = List.of(attachment, attachment);
        createRequest = new CreateCandidateAttachmentRequest();
        createRequest.setCandidateId(candidateId);
        createRequest.setLocation(location);
        createRequest.setName(name);
    }

    @Test
    @DisplayName("should return page of attachments when found")
    void searchCandidateAttachments_shouldReturnAttachment_whenFound() {
        final SearchCandidateAttachmentsRequest request = new SearchCandidateAttachmentsRequest();
        request.setCandidateId(candidateId);

        given(candidateAttachmentRepository.findByCandidateId(anyLong(), any(PageRequest.class)))
            .willReturn(attachmentPage);

        assertEquals(attachmentPage, candidateAttachmentsService.searchCandidateAttachments(request));
    }

    @Test
    @DisplayName("should return page of attachments when candidate logged in")
    void searchCandidateAttachmentsForLoggedInCandidate_shouldReturnPageOfAttachments() {
        given(authService.getLoggedInCandidateId()).willReturn(1L);
        given(candidateAttachmentRepository.findByCandidateId(anyLong(), any(PageRequest.class)))
            .willReturn(attachmentPage);

        assertEquals(attachmentPage,
            candidateAttachmentsService.searchCandidateAttachmentsForLoggedInCandidate(
                new PagedSearchRequest()));
    }

    @Test
    @DisplayName("should throw when candidate not logged in")
    void searchCandidateAttachmentsForLoggedInCandidate_shouldThrow_whenCandidateNotLoggedIn() {
        given(authService.getLoggedInCandidateId()).willReturn(null);

        assertThrows(InvalidSessionException.class,
            () -> candidateAttachmentsService.searchCandidateAttachmentsForLoggedInCandidate(
                new PagedSearchRequest()));
    }

    @Test
    @DisplayName("should return list of attachments when found")
    void listCandidateAttachmentsByType_shouldReturnListOfAttachments_whenFound() {
        ListByUploadTypeRequest request = new ListByUploadTypeRequest();
        request.setCandidateId(candidateId);
        request.setUploadType(UploadType.idCard);

        given(candidateAttachmentRepository.findByCandidateIdAndType(candidateId, UploadType.idCard))
            .willReturn(attachmentList);

        assertEquals(attachmentList,
            candidateAttachmentsService.listCandidateAttachmentsByType(request));
    }

    @Test
    @DisplayName("should throw when candidate not logged in")
    void listCandidateAttachmentsForLoggedInCandidate_shouldThrow_whenCandidateNotLoggedIn() {
        given(authService.getLoggedInCandidateId()).willReturn(null);

        assertThrows(InvalidSessionException.class,
            () -> candidateAttachmentsService.listCandidateAttachmentsForLoggedInCandidate());
    }

    @Test
    @DisplayName("should return list of attachments when candidate logged in")
    void listCandidateAttachmentsForLoggedInCandidate_shouldReturnListOfAttachments_whenFound() {
        given(authService.getLoggedInCandidateId()).willReturn(candidateId);
        given(candidateAttachmentRepository.findByCandidateIdLoadAudit(candidateId))
            .willReturn(attachmentList);

        assertEquals(attachmentList,
            candidateAttachmentsService.listCandidateAttachmentsForLoggedInCandidate());
    }

    @Test
    @DisplayName("should return list of cvs when candidate found")
    void listCandidateCvs_shouldReturnListOfCvs_whenCandidateFound() {
        given(candidateRepository.findById(candidateId)).willReturn(Optional.of(candidate));
        given(candidateAttachmentRepository.findByCandidateIdAndCv(anyLong(), eq(true)))
            .willReturn(attachmentList);

        assertEquals(attachmentList, candidateAttachmentsService.listCandidateCvs(candidateId));
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void listCandidateCvs_shouldThrow_whenCandidateNotFound() {
        given(candidateRepository.findById(candidateId)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateAttachmentsService.listCandidateCvs(candidateId));
    }

    @Test
    @DisplayName("should return list of attachments when candidate found")
    void listCandidateAttachments_shouldReturnListOfAttachments_whenCandidateFound() {
        given(candidateRepository.findById(candidateId)).willReturn(Optional.of(candidate));
        given(candidateAttachmentRepository.findByCandidateId(anyLong())).willReturn(attachmentList);

        assertEquals(attachmentList, candidateAttachmentsService.listCandidateAttachments(candidateId));
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void listCandidateAttachments_shouldThrow_whenCandidateNotFound() {
        given(candidateRepository.findById(candidateId)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateAttachmentsService.listCandidateAttachments(candidateId));
    }

    @Test
    @DisplayName("should throw when no logged in user")
    void createCandidateAttachment_shouldThrow_whenNoLoggedInUser() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateAttachmentsService.createCandidateAttachment(createRequest));
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void createCandidateAttachment_shouldThrow_whenCandidateNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(candidateRepository.findById(candidateId)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateAttachmentsService.createCandidateAttachment(createRequest));
    }

    @Test
    @DisplayName("should throw when no candidate id in request")
    void createCandidateAttachment_shouldThrow_whenNoCandidateIdInRequest() {
        createRequest.setCandidateId(null);
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));

        assertThrows(InvalidRequestException.class,
            () -> candidateAttachmentsService.createCandidateAttachment(createRequest));
    }

    @Test
    @DisplayName("should create candidate attachment for link")
    void createCandidateAttachment_shouldCreateCandidateAttachmentForLink() {
        createRequest.setType(AttachmentType.link);

        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(candidateRepository.findById(candidateId)).willReturn(Optional.of(candidate));

        candidateAttachmentsService.createCandidateAttachment(createRequest);

        verify(candidateService).save(candidate, true);
        verify(candidateAttachmentRepository).save(attachmentCaptor.capture());
        CandidateAttachment attachment = attachmentCaptor.getValue();
        assertEquals(name, attachment.getName());
        assertEquals(location, attachment.getLocation());
        assertEquals(AttachmentType.link, attachment.getType());
    }

    @Test
    @DisplayName("should create candidate attachment for google file")
    void createCandidateAttachment_shouldCreateCandidateAttachmentForGoogleFile() {
        createRequest.setType(AttachmentType.googlefile);
        createRequest.setCv(true);

        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(candidateRepository.findById(candidateId)).willReturn(Optional.of(candidate));

        candidateAttachmentsService.createCandidateAttachment(createRequest);

        verify(candidateService).save(candidate, true);
        verify(candidateAttachmentRepository).save(attachmentCaptor.capture());
        CandidateAttachment attachment = attachmentCaptor.getValue();
        assertEquals(name, attachment.getName());
        assertEquals(location, attachment.getLocation());
        assertEquals(AttachmentType.googlefile, attachment.getType());
        assertTrue(attachment.isCv());
    }

}
