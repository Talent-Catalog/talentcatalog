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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.UserTestData.getAdminUser;
import static org.tctalent.server.data.UserTestData.getCandidateUser;

import java.io.File;
import java.io.IOException;
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
import org.tctalent.server.exception.InvalidCredentialsException;
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
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.aws.S3ResourceHelper;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;

@ExtendWith(MockitoExtension.class)
public class CandidateAttachmentsServiceImplTest {

    private CandidateAttachment attachment;
    private Page<CandidateAttachment> attachmentPage;
    private List<CandidateAttachment> attachmentList;
    private CreateCandidateAttachmentRequest createRequest;
    private Candidate candidate2;

    private final static Candidate CANDIDATE = getCandidate();
    private final static long CANDIDATE_ID = CANDIDATE.getId();
    private final static User ADMIN_USER = getAdminUser();
    private final static String NAME = "name";
    private final static String LOCATION = "www.link.com";
    private final static String FILE_TYPE = "pdf";
    private final static String TEXT_EXTRACT = "text extract";
    private final static long ATTACHMENT_ID = 1L;
    private final static User CANDIDATE_USER = getCandidateUser();

    @Mock private CandidateAttachmentRepository candidateAttachmentRepository;
    @Mock private AuthService authService;
    @Mock private CandidateRepository candidateRepository;
    @Mock private CandidateService candidateService;
    @Mock private S3ResourceHelper s3ResourceHelper;
    @Mock private FileSystemService fileSystemService;

    @Captor private ArgumentCaptor<CandidateAttachment> attachmentCaptor;

    @InjectMocks
    CandidateAttachmentsServiceImpl candidateAttachmentsService;

    @BeforeEach
    void setUp() {
        attachment = new CandidateAttachment();
        attachmentPage = new PageImpl<>(List.of(attachment, attachment));
        attachmentList = List.of(attachment, attachment);
        createRequest = new CreateCandidateAttachmentRequest();
        createRequest.setCandidateId(CANDIDATE_ID);
        createRequest.setLocation(LOCATION);
        createRequest.setName(NAME);
        candidate2 = new Candidate();
        candidate2.setId(123L);
        candidate2.setUser(getCandidateUser());
    }

    @Test
    @DisplayName("should return page of attachments when found")
    void searchCandidateAttachments_shouldReturnAttachment_whenFound() {
        final SearchCandidateAttachmentsRequest request = new SearchCandidateAttachmentsRequest();
        request.setCandidateId(CANDIDATE_ID);

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
        request.setCandidateId(CANDIDATE_ID);
        request.setUploadType(UploadType.idCard);

        given(candidateAttachmentRepository.findByCandidateIdAndType(CANDIDATE_ID, UploadType.idCard))
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
        given(authService.getLoggedInCandidateId()).willReturn(CANDIDATE_ID);
        given(candidateAttachmentRepository.findByCandidateIdLoadAudit(CANDIDATE_ID))
            .willReturn(attachmentList);

        assertEquals(attachmentList,
            candidateAttachmentsService.listCandidateAttachmentsForLoggedInCandidate());
    }

    @Test
    @DisplayName("should return list of cvs when candidate found")
    void listCandidateCvs_shouldReturnListOfCvs_whenCandidateFound() {
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));
        given(candidateAttachmentRepository.findByCandidateIdAndCv(anyLong(), eq(true)))
            .willReturn(attachmentList);

        assertEquals(attachmentList, candidateAttachmentsService.listCandidateCvs(CANDIDATE_ID));
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void listCandidateCvs_shouldThrow_whenCandidateNotFound() {
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateAttachmentsService.listCandidateCvs(CANDIDATE_ID));
    }

    @Test
    @DisplayName("should return list of attachments when candidate found")
    void listCandidateAttachments_shouldReturnListOfAttachments_whenCandidateFound() {
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));
        given(candidateAttachmentRepository.findByCandidateId(anyLong())).willReturn(attachmentList);

        assertEquals(attachmentList, candidateAttachmentsService.listCandidateAttachments(
            CANDIDATE_ID));
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void listCandidateAttachments_shouldThrow_whenCandidateNotFound() {
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateAttachmentsService.listCandidateAttachments(CANDIDATE_ID));
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
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateAttachmentsService.createCandidateAttachment(createRequest));
    }

    @Test
    @DisplayName("should throw when no candidate id in request")
    void createCandidateAttachment_shouldThrow_whenNoCandidateIdInRequest() {
        createRequest.setCandidateId(null);
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));

        assertThrows(InvalidRequestException.class,
            () -> candidateAttachmentsService.createCandidateAttachment(createRequest));
    }

    @Test
    @DisplayName("should create candidate attachment for link")
    void createCandidateAttachment_shouldCreateCandidateAttachmentForLink() {
        createRequest.setType(AttachmentType.link);

        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));

        candidateAttachmentsService.createCandidateAttachment(createRequest);

        verify(candidateService).save(CANDIDATE, true);
        verify(candidateAttachmentRepository).save(attachmentCaptor.capture());
        CandidateAttachment attachment = attachmentCaptor.getValue();
        assertEquals(NAME, attachment.getName());
        assertEquals(LOCATION, attachment.getLocation());
        assertEquals(AttachmentType.link, attachment.getType());
        assertEquals(ADMIN_USER, attachment.getCreatedBy());
    }

    @Test
    @DisplayName("should create candidate attachment for google file")
    void createCandidateAttachment_shouldCreateCandidateAttachmentForGoogleFile() {
        createRequest.setType(AttachmentType.googlefile);
        createRequest.setCv(true);
        createRequest.setFileType(FILE_TYPE);
        createRequest.setTextExtract(TEXT_EXTRACT);

        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));

        candidateAttachmentsService.createCandidateAttachment(createRequest);

        verify(candidateService).save(CANDIDATE, true);
        verify(candidateAttachmentRepository).save(attachmentCaptor.capture());
        CandidateAttachment attachment = attachmentCaptor.getValue();
        assertEquals(NAME, attachment.getName());
        assertEquals(LOCATION, attachment.getLocation());
        assertEquals(AttachmentType.googlefile, attachment.getType());
        assertTrue(attachment.isCv());
        assertEquals(TEXT_EXTRACT, attachment.getTextExtract());
        assertEquals(FILE_TYPE, attachment.getFileType());
        assertEquals(ADMIN_USER, attachment.getCreatedBy());
    }

    @Test
    @DisplayName("should create candidate attachment for file")
    void createCandidateAttachment_shouldCreateCandidateAttachmentForFile() {
        createRequest.setType(AttachmentType.file);
        createRequest.setCv(false); // Because of the way that TextExtractHelper is injected, it
        // can't be mocked - so we're not able to test the CV path.
        createRequest.setFileType(FILE_TYPE);
        createRequest.setTextExtract(TEXT_EXTRACT);

        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));
        given(s3ResourceHelper.getS3Bucket()).willReturn("bucket");
        given(s3ResourceHelper.downloadFile(anyString(), anyString())).willReturn(mock(File.class));

        candidateAttachmentsService.createCandidateAttachment(createRequest);

        verify(s3ResourceHelper).downloadFile(anyString(), anyString());
        verify(s3ResourceHelper).copyObject(anyString(), anyString());
        verify(candidateService).save(CANDIDATE, true);
        verify(candidateAttachmentRepository).save(attachmentCaptor.capture());
        CandidateAttachment attachment = attachmentCaptor.getValue();
        assertTrue(attachment.getLocation().contains(NAME));
        assertEquals(AttachmentType.file, attachment.getType());
        assertEquals(NAME, attachment.getName());
        assertEquals(FILE_TYPE, attachment.getFileType());
        assertFalse(attachment.isCv());
        assertEquals(ADMIN_USER, attachment.getCreatedBy());
    }

    @Test
    @DisplayName("should throw when logged in user not found")
    void deleteCandidateAttachment_shouldThrow_whenUserNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateAttachmentsService.deleteCandidateAttachment(ATTACHMENT_ID));
    }

    @Test
    @DisplayName("should throw when attachment not found")
    void deleteCandidateAttachment_shouldThrow_whenAttachmentNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateAttachmentsService.deleteCandidateAttachment(ATTACHMENT_ID));
    }

    @Test
    @DisplayName("should throw when logged in candidate not found")
    void deleteCandidateAttachment_shouldThrow_whenCandidateNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(CANDIDATE_USER));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateAttachmentsService.deleteCandidateAttachment(ATTACHMENT_ID));
    }

    @Test
    @DisplayName("should throw when candidate tries to delete unrelated attachment")
    void deleteCandidateAttachment_shouldThrow_whenCandidateTriesToDeleteUnrelatedAttachment() {
        attachment.setCandidate(candidate2);
        given(authService.getLoggedInUser()).willReturn(Optional.of(CANDIDATE_USER));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(CANDIDATE));

        Exception ex = assertThrows(InvalidCredentialsException.class,
            () -> candidateAttachmentsService.deleteCandidateAttachment(ATTACHMENT_ID));
        assertEquals(ex.getMessage(), "You do not have permission to perform that action");
    }

    @Test
    @DisplayName("should throw when attachment not uploaded by deleting candidate")
    void deleteCandidateAttachment_shouldThrow_whenAttachmentNotUploadedByDeletingCandidate() {
        attachment.setCandidate(candidate2);
        attachment.setCreatedBy(getAdminUser());

        given(authService.getLoggedInUser()).willReturn(Optional.of(CANDIDATE_USER));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(candidate2));

        Exception ex = assertThrows(InvalidRequestException.class,
            () -> candidateAttachmentsService.deleteCandidateAttachment(ATTACHMENT_ID));
        assertEquals(ex.getMessage(), "You can only delete your own uploads.");
    }

    @Test
    @DisplayName("should delete when attachment related to and uploaded by candidate")
    void deleteCandidateAttachment_shouldDelete_whenAttachmentRelatedToAndUploadedByCandidate() {
        attachment.setCandidate(candidate2);
        attachment.setCreatedBy(candidate2.getUser());
        attachment.setType(AttachmentType.file);

        given(authService.getLoggedInUser()).willReturn(Optional.of(CANDIDATE_USER));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(candidate2));

        candidateAttachmentsService.deleteCandidateAttachment(ATTACHMENT_ID);

        verify(candidateAttachmentRepository).delete(attachment);
        assertEquals(candidate2.getUpdatedBy(), candidate2.getUser());
        verify(candidateService).save(candidate2, true);
        verify(s3ResourceHelper).deleteFile(anyString()); // Attempted delete of associated file
    }

    @Test
    @DisplayName("should delete for admin user")
    void deleteCandidateAttachment_shouldDeleteForAdminUser() throws IOException {
        attachment.setCandidate(CANDIDATE);
        attachment.setType(AttachmentType.googlefile);

        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(CANDIDATE));
        given(authService.hasAdminPrivileges(ADMIN_USER.getRole())).willReturn(true);

        candidateAttachmentsService.deleteCandidateAttachment(ATTACHMENT_ID);

        verify(candidateAttachmentRepository).delete(attachment);
        assertEquals(CANDIDATE.getUpdatedBy(), ADMIN_USER);
        verify(candidateService).save(CANDIDATE, true);
        verify(fileSystemService).deleteFile(any(GoogleFileSystemFile.class));
    }

}
