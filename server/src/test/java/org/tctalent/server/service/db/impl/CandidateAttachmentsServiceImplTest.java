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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.UserTestData.getAdminUser;
import static org.tctalent.server.data.UserTestData.getCandidateUser;
import static org.tctalent.server.data.UserTestData.getLimitedUser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
import org.springframework.mock.web.MockMultipartFile;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.task.UploadType;
import org.tctalent.server.repository.db.CandidateAttachmentRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.PagedSearchRequest;
import org.tctalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tctalent.server.request.attachment.ListByUploadTypeRequest;
import org.tctalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tctalent.server.request.attachment.UpdateCandidateAttachmentRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.aws.S3ResourceHelper;
import org.tctalent.server.util.filesystem.GoogleFileSystemBaseEntity;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

@ExtendWith(MockitoExtension.class)
public class CandidateAttachmentsServiceImplTest {

    private CandidateAttachment attachment;
    private Page<CandidateAttachment> attachmentPage;
    private List<CandidateAttachment> attachmentList;
    private CreateCandidateAttachmentRequest createRequest;
    private Candidate candidate2;
    private User user2;
    private UpdateCandidateAttachmentRequest updateRequest;
    private Candidate candidate;

    private final static long CANDIDATE_ID = getCandidate().getId();
    private final static User ADMIN_USER = getAdminUser();
    private final static String NAME = "name";
    private final static String LOCATION = "www.link.com";
    private final static String FILE_TYPE = "pdf";
    private final static String TEXT_EXTRACT = "text extract";
    private final static long ATTACHMENT_ID = 1L;
    private final static User CANDIDATE_USER = getCandidateUser();
    private final static String ORIGINAL_FILE_NAME = "test.pdf";
    private final static MockMultipartFile file = new MockMultipartFile("file",
        ORIGINAL_FILE_NAME,"application/pdf", "This is content".getBytes());

    @Mock private CandidateAttachmentRepository candidateAttachmentRepository;
    @Mock private AuthService authService;
    @Mock private CandidateRepository candidateRepository;
    @Mock private CandidateService candidateService;
    @Mock private S3ResourceHelper s3ResourceHelper;
    @Mock private FileSystemService fileSystemService;
    @Mock private OutputStream outputStream;

    @Captor private ArgumentCaptor<CandidateAttachment> attachmentCaptor;

    @InjectMocks
    CandidateAttachmentsServiceImpl candidateAttachmentsService;

    @BeforeEach
    void setUp() {
        attachment = new CandidateAttachment();
        attachment.setName("an old name");
        attachment.setLocation("www.apreviouslocation.com");
        attachmentPage = new PageImpl<>(List.of(attachment, attachment));
        attachmentList = List.of(attachment, attachment);
        createRequest = new CreateCandidateAttachmentRequest();
        createRequest.setCandidateId(CANDIDATE_ID);
        createRequest.setLocation(LOCATION);
        createRequest.setName(NAME);
        candidate = getCandidate();
        candidate2 = new Candidate();
        candidate2.setId(123L);
        user2 = new User();
        user2.setId(456L);
        user2.setRole(Role.user);
        user2.setCandidate(candidate2);
        candidate2.setUser(user2);
        updateRequest = new UpdateCandidateAttachmentRequest();
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
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(candidate));
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
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(candidate));
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
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(candidate));

        candidateAttachmentsService.createCandidateAttachment(createRequest);

        verify(candidateService).save(candidate, true);
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
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(candidate));

        candidateAttachmentsService.createCandidateAttachment(createRequest);

        verify(candidateService).save(candidate, true);
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
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(candidate));
        given(s3ResourceHelper.getS3Bucket()).willReturn("bucket");
        given(s3ResourceHelper.downloadFile(anyString(), anyString())).willReturn(mock(File.class));

        candidateAttachmentsService.createCandidateAttachment(createRequest);

        verify(s3ResourceHelper).downloadFile(anyString(), anyString());
        verify(s3ResourceHelper).copyObject(anyString(), anyString());
        verify(candidateService).save(candidate, true);
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
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(candidate));

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
    @DisplayName("should delete file when attachment related to and uploaded by candidate")
    void deleteCandidateAttachment_shouldDeleteFile_whenAttachmentRelatedToAndUploadedByCandidate() {
        attachment.setCandidate(candidate2);
        attachment.setCreatedBy(candidate2.getUser());
        attachment.setType(AttachmentType.file);

        given(authService.getLoggedInUser()).willReturn(Optional.of(candidate2.getUser()));
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
    @DisplayName("should catch when attempt to delete file throws exception")
    void deleteCandidateAttachment_shouldCatch_whenAttemptToDeleteFileThrowsException()
        throws IOException {
        attachment.setCandidate(candidate2);
        attachment.setCreatedBy(candidate2.getUser());
        attachment.setType(AttachmentType.googlefile);

        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(candidateRepository.findById(candidate2.getId())).willReturn(Optional.of(candidate2));
        given(authService.hasAdminPrivileges(any(Role.class))).willReturn(true);

        doThrow(IOException.class).when(fileSystemService).deleteFile(any(GoogleFileSystemFile.class));
        assertDoesNotThrow(() -> candidateAttachmentsService.deleteCandidateAttachment(ATTACHMENT_ID));

        verify(candidateAttachmentRepository).delete(attachment);
        assertEquals(candidate2.getUpdatedBy(), ADMIN_USER);
        verify(candidateService).save(candidate2, true);
    }

    @Test
    @DisplayName("should rename google file when related to and uploaded by candidate")
    void deleteCandidateAttachment_shouldRenameGoogleFile_whenRelatedToAndUploadedByCandidate()
        throws IOException {
        attachment.setCandidate(candidate2);
        attachment.setCreatedBy(candidate2.getUser());
        attachment.setType(AttachmentType.googlefile);

        given(authService.getLoggedInUser()).willReturn(Optional.of(candidate2.getUser()));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(candidate2));
        given(authService.hasAdminPrivileges(any(Role.class))).willReturn(false);

        candidateAttachmentsService.deleteCandidateAttachment(ATTACHMENT_ID);

        verify(candidateAttachmentRepository).delete(attachment);
        assertEquals(candidate2.getUpdatedBy(), candidate2.getUser());
        verify(candidateService).save(candidate2, true);
        verify(fileSystemService).renameFile(any(GoogleFileSystemFile.class));
        verify(fileSystemService, never()).deleteFile(any(GoogleFileSystemFile.class));
    }

    @Test
    @DisplayName("should catch when attempt to rename throws exception")
    void deleteCandidateAttachment_shouldCatch_whenAttemptToRenameThrowsException() throws IOException {
        attachment.setCandidate(candidate2);
        attachment.setCreatedBy(candidate2.getUser());
        attachment.setType(AttachmentType.googlefile);

        given(authService.getLoggedInUser()).willReturn(Optional.of(candidate2.getUser()));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(candidateService.getLoggedInCandidate()).willReturn(Optional.of(candidate2));
        given(authService.hasAdminPrivileges(any(Role.class))).willReturn(false);

        doThrow(IOException.class).when(fileSystemService).renameFile(any(GoogleFileSystemFile.class));
        assertDoesNotThrow(() -> candidateAttachmentsService.deleteCandidateAttachment(ATTACHMENT_ID));

        verify(candidateAttachmentRepository).delete(attachment);
        assertEquals(candidate2.getUpdatedBy(), candidate2.getUser());
        verify(candidateService).save(candidate2, true);
        verify(fileSystemService).renameFile(any(GoogleFileSystemFile.class));
        verify(fileSystemService, never()).deleteFile(any(GoogleFileSystemFile.class));
    }

    @Test
    @DisplayName("should delete for admin user")
    void deleteCandidateAttachment_shouldDeleteForAdminUser() throws IOException {
        attachment.setCandidate(candidate);
        attachment.setType(AttachmentType.googlefile);

        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(candidate));
        given(authService.hasAdminPrivileges(ADMIN_USER.getRole())).willReturn(true);

        candidateAttachmentsService.deleteCandidateAttachment(ATTACHMENT_ID);

        verify(candidateAttachmentRepository).delete(attachment);
        assertEquals(candidate.getUpdatedBy(), ADMIN_USER);
        verify(candidateService).save(candidate, true);
        verify(fileSystemService).deleteFile(any(GoogleFileSystemFile.class));
    }

    @Test
    @DisplayName("should throw when attachment not found")
    void downloadCandidateAttachment_shouldThrow_whenAttachmentNotFound() {
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateAttachmentsService.downloadCandidateAttachment(ATTACHMENT_ID, outputStream));
    }

    @Test
    @DisplayName("should throw when user not logged in")
    void downloadCandidateAttachment_shouldThrow_whenUserNotLoggedIn() {
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.ofNullable(attachment));
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateAttachmentsService.downloadCandidateAttachment(ATTACHMENT_ID, outputStream));
    }

    @Test
    @DisplayName("should throw when candidate user not related or creator")
    void downloadCandidateAttachment_shouldThrow_whenCandidateUserNotRelatedOrCreator() {
        attachment.setCandidate(candidate);
        attachment.setCreatedBy(ADMIN_USER);
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.ofNullable(attachment));
        given(authService.getLoggedInUser()).willReturn(Optional.of(candidate2.getUser()));

        assertThrows(InvalidRequestException.class,
            () -> candidateAttachmentsService.downloadCandidateAttachment(ATTACHMENT_ID, outputStream));
    }

    @Test
    @DisplayName("should download when candidate user is related")
    void downloadCandidateAttachment_shouldDownload_whenCandidateUserRelated() throws IOException {
        attachment.setCandidate(candidate2); // Related
        attachment.setCreatedBy(ADMIN_USER); // Not created by
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.ofNullable(attachment));
        given(authService.getLoggedInUser()).willReturn(Optional.of(candidate2.getUser()));

        candidateAttachmentsService.downloadCandidateAttachment(ATTACHMENT_ID, outputStream);

        verify(fileSystemService).downloadFile(any(GoogleFileSystemFile.class),
            any(OutputStream.class));
    }

    @Test
    @DisplayName("should download when created by candidate user")
    void downloadCandidateAttachment_shouldDownload_whenCreatedByCandidateUser() throws IOException {
        attachment.setCandidate(candidate); // Not related
        attachment.setCreatedBy(user2); // Created by
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.ofNullable(attachment));
        given(authService.getLoggedInUser()).willReturn(Optional.of(candidate2.getUser()));

        candidateAttachmentsService.downloadCandidateAttachment(ATTACHMENT_ID, outputStream);

        verify(fileSystemService).downloadFile(any(GoogleFileSystemFile.class),
            any(OutputStream.class));
    }

    @Test
    @DisplayName("should download for admin user")
    void downloadCandidateAttachment_shouldDownloadForAdminUser() throws IOException {
        // Not related nor created by - doesn't matter for admin
        attachment.setCandidate(candidate2);
        attachment.setCreatedBy(user2);
        attachment.setType(AttachmentType.googlefile);

        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.ofNullable(attachment));
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));

        candidateAttachmentsService.downloadCandidateAttachment(ATTACHMENT_ID, outputStream);

        verify(fileSystemService).downloadFile(any(GoogleFileSystemFile.class),
            any(OutputStream.class));
    }

    @Test
    @DisplayName("should throw for limited admin user")
    void downloadCandidateAttachment_shouldThrowForLimitedAdminUser() {
        // Not related nor created by - doesn't matter for admin
        attachment.setCandidate(candidate2);
        attachment.setCreatedBy(user2);
        attachment.setType(AttachmentType.googlefile);

        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.ofNullable(attachment));
        given(authService.getLoggedInUser()).willReturn(Optional.of(getLimitedUser()));

        assertThrows(InvalidRequestException.class,
            () -> candidateAttachmentsService.downloadCandidateAttachment(ATTACHMENT_ID, outputStream));
    }

    @Test
    @DisplayName("should return attachment when found")
    void getCandidateAttachment_shouldReturn_whenFound() {
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.ofNullable(attachment));

        assertEquals(attachment, candidateAttachmentsService.getCandidateAttachment(ATTACHMENT_ID));
    }

    @Test
    @DisplayName("should throw when not found")
    void getCandidateAttachment_shouldThrow_whenNotFound() {
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateAttachmentsService.getCandidateAttachment(ATTACHMENT_ID));
    }

    @Test
    @DisplayName("should throw when user not logged in")
    void updateCandidateAttachment_shouldThrow_whenUserNotLoggedIn() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> candidateAttachmentsService.updateCandidateAttachment(ATTACHMENT_ID, updateRequest));
    }

    @Test
    @DisplayName("should throw when user not authorised")
    void updateCandidateAttachment_shouldThrow_whenUserNotAuthorised() {
        attachment.setType(AttachmentType.link);
        attachment.setCandidate(candidate);
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(authService.authoriseLoggedInUser(candidate)).willReturn(false);

        assertThrows(UnauthorisedActionException.class,
            () -> candidateAttachmentsService.updateCandidateAttachment(ATTACHMENT_ID, updateRequest));
    }

    @Test
    @DisplayName("should update google file as expected")
    void updateCandidateAttachment_shouldUpdateGoogleFile() throws IOException {
        final String newName = "new name";
        updateRequest.setName(newName);
        attachment.setCandidate(candidate);
        attachment.setType(AttachmentType.googlefile);

        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(authService.authoriseLoggedInUser(candidate)).willReturn(true);

        candidateAttachmentsService.updateCandidateAttachment(ATTACHMENT_ID, updateRequest);

        assertEquals(attachment.getName(), newName);
        verify(fileSystemService).renameFile(any(GoogleFileSystemFile.class));
        assertEquals(candidate.getUpdatedBy(), ADMIN_USER);
        verify(candidateService).save(candidate, true);
        assertEquals(attachment.getUpdatedBy(), ADMIN_USER);
        verify(candidateAttachmentRepository).save(attachment);
    }

    @Test
    @DisplayName("should update link as expected")
    void updateCandidateAttachment_shouldUpdateFile() throws IOException {
        final String newName = "new name";
        updateRequest.setName(newName);
        final String newLocation = "new location";
        updateRequest.setLocation(newLocation);
        attachment.setCandidate(candidate);
        attachment.setType(AttachmentType.link);

        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));
        given(candidateAttachmentRepository.findByIdLoadCandidate(ATTACHMENT_ID))
            .willReturn(Optional.of(attachment));
        given(authService.authoriseLoggedInUser(candidate)).willReturn(true);

        candidateAttachmentsService.updateCandidateAttachment(ATTACHMENT_ID, updateRequest);

        assertEquals(attachment.getName(), newName);
        assertEquals(attachment.getLocation(), newLocation);
        assertEquals(candidate.getUpdatedBy(), ADMIN_USER);
        verify(candidateService).save(candidate, true);
        assertEquals(attachment.getUpdatedBy(), ADMIN_USER);
        verify(candidateAttachmentRepository).save(attachment);
    }

    @Test
    @DisplayName("should throw when logged in candidate id not found")
    void uploadAttachment_shouldThrow_whenCandidateIdNotFound() {
        given(authService.getLoggedInCandidateId()).willReturn(null);

        assertThrows(InvalidSessionException.class,
            () -> candidateAttachmentsService.uploadAttachment(true, file));
    }

    @Test
    @DisplayName("should throw when candidate not found")
    void uploadAttachment_shouldThrow_whenCandidateNotFound() {
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> candidateAttachmentsService.uploadAttachment(CANDIDATE_ID, true, file));
    }

    @Test
    @DisplayName("should upload file as expected")
    void uploadAttachment_shouldUploadFile() throws IOException {
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(candidate));
        given(candidateService.createCandidateFolder(CANDIDATE_ID)).willReturn(candidate);
        given(fileSystemService.getDriveFromEntity(any(GoogleFileSystemBaseEntity.class)))
            .willReturn(mock(GoogleFileSystemDrive.class));
        given(fileSystemService.uploadFile(any(GoogleFileSystemDrive.class),
            any(GoogleFileSystemFolder.class), anyString(), any(File.class)))
            .willReturn(mock(GoogleFileSystemFile.class));
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));

        candidateAttachmentsService.uploadAttachment(CANDIDATE_ID, false, file);

        assertEquals(candidate.getUpdatedBy(), ADMIN_USER);
        verify(candidateService).save(candidate, true);
        verify(candidateAttachmentRepository).save(attachmentCaptor.capture());
        CandidateAttachment upload = attachmentCaptor.getValue();
        assertEquals(upload.getCandidate(), candidate);
        assertEquals(upload.getType(), AttachmentType.googlefile);
        assertEquals(upload.getName(), ORIGINAL_FILE_NAME);
        assertEquals(upload.getFileType(), "pdf");
        assertEquals(upload.getUpdatedBy(), ADMIN_USER);
    }

    @Test
    @DisplayName("should upload file and create subfolder as expected")
    void uploadAttachment_shouldUploadFileAndCreateSubFolder() throws IOException {
        given(candidateRepository.findById(CANDIDATE_ID)).willReturn(Optional.of(candidate));
        given(candidateService.createCandidateFolder(CANDIDATE_ID)).willReturn(candidate);
        given(fileSystemService.getDriveFromEntity(any(GoogleFileSystemBaseEntity.class)))
            .willReturn(mock(GoogleFileSystemDrive.class));
        given(fileSystemService.uploadFile(any(GoogleFileSystemDrive.class),
            any(GoogleFileSystemFolder.class), anyString(), any(File.class)))
            .willReturn(mock(GoogleFileSystemFile.class));
        given(authService.getLoggedInUser()).willReturn(Optional.of(ADMIN_USER));

        final String subfolderName = "subfolderName";
        given(fileSystemService.findAFolder(any(GoogleFileSystemDrive.class),
            any(GoogleFileSystemFolder.class), eq(subfolderName))).willReturn(null);
        given(fileSystemService.createFolder(any(GoogleFileSystemDrive.class),
            any(GoogleFileSystemFolder.class), eq(subfolderName)))
            .willReturn(mock(GoogleFileSystemFolder.class));

        candidateAttachmentsService.uploadAttachment(candidate, ORIGINAL_FILE_NAME, subfolderName,
            file, UploadType.other);

        verify(fileSystemService).createFolder(any(GoogleFileSystemDrive.class),
            any(GoogleFileSystemFolder.class), eq(subfolderName));
        verify(fileSystemService).publishFolder(any(GoogleFileSystemFolder.class));
    }

}
