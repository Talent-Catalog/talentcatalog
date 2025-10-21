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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.logging.LogBuilder;
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
import org.tctalent.server.service.db.CandidateAttachmentService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.aws.S3ResourceHelper;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;
import org.tctalent.server.util.textExtract.TextExtractHelper;

@Service
@Slf4j
public class CandidateAttachmentsServiceImpl implements CandidateAttachmentService {

    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final CandidateAttachmentRepository candidateAttachmentRepository;
    private final FileSystemService fileSystemService;
    private final AuthService authService;
    private final S3ResourceHelper s3ResourceHelper;

    @Autowired
    public CandidateAttachmentsServiceImpl(CandidateRepository candidateRepository,
                                           CandidateService candidateService,
                                           CandidateAttachmentRepository candidateAttachmentRepository,
                                           FileSystemService fileSystemService, S3ResourceHelper s3ResourceHelper,
                                           AuthService authService) {
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.candidateAttachmentRepository = candidateAttachmentRepository;
        this.fileSystemService = fileSystemService;
        this.s3ResourceHelper = s3ResourceHelper;
        this.authService = authService;
    }

    @Override
    public Page<CandidateAttachment> searchCandidateAttachments(SearchCandidateAttachmentsRequest request) {
        return candidateAttachmentRepository.findByCandidateId(request.getCandidateId(), request.getPageRequest());
    }

    @Override
    public Page<CandidateAttachment> searchCandidateAttachmentsForLoggedInCandidate(
        PagedSearchRequest request) {
        Long candidateId = authService.getLoggedInCandidateId();
        if (candidateId == null) {
            throw new InvalidSessionException("Not logged in");
        }
        return candidateAttachmentRepository
                .findByCandidateId(candidateId, request.getPageRequest());
    }

    @Override
    public List<CandidateAttachment> listCandidateAttachmentsByType(ListByUploadTypeRequest request) {
        return candidateAttachmentRepository.findByCandidateIdAndType(request.getCandidateId(), request.getUploadType());
    }

    @Override
    public List<CandidateAttachment> listCandidateAttachmentsForLoggedInCandidate() {
        Long candidateId = authService.getLoggedInCandidateId();
        if (candidateId == null) {
            throw new InvalidSessionException("Not logged in");
        }
        return candidateAttachmentRepository.findByCandidateIdLoadAudit(candidateId);
    }

    @Override
    public List<CandidateAttachment> listCandidateCvs(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));
        return candidateAttachmentRepository.findByCandidateIdAndCv(candidate.getId(), true);
    }

    @Override
    public List<CandidateAttachment> listCandidateAttachments(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));
        return candidateAttachmentRepository.findByCandidateId(candidate.getId());
    }

    @Override
    public CandidateAttachment createCandidateAttachment(CreateCandidateAttachmentRequest request) {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Candidate candidate;
        String textExtract;

        if (request.getCandidateId() != null) {
            candidate = candidateRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));
        } else {
            throw new InvalidRequestException("Missing candidate ID");
        }

        // Create a record of the attachment
        CandidateAttachment attachment = new CandidateAttachment();

        attachment.setCandidate(candidate);
        attachment.setMigrated(false);
        attachment.setAuditFields(user);
        attachment.setUploadType(request.getUploadType());

        if (request.getType().equals(AttachmentType.link)) {

            attachment.setType(AttachmentType.link);
            attachment.setLocation(request.getLocation());
            attachment.setName(request.getName());

        } else if (request.getType().equals(AttachmentType.googlefile)) {
            attachment.setLocation(request.getLocation());
            attachment.setName(request.getName());
            attachment.setType(AttachmentType.googlefile);
            attachment.setFileType(request.getFileType());
            attachment.setCv(request.getCv());
            attachment.setTextExtract(request.getTextExtract());

        } else if (request.getType().equals(AttachmentType.file)) {
            // Prepend the filename with a UUID to ensure an existing file doesn't get overwritten on S3
            String uniqueFilename = UUID.randomUUID() + "_" + request.getName();
            // Source is temporary folder where the file got uploaded, destination is the candidates unique folder
            String source = "temp/" + request.getFolder() + "/" + request.getName();
            String destination = "candidate/" + candidate.getCandidateNumber() + "/" + uniqueFilename;
            // Download the file from the S3 temporary file before it is copying over
            File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), source);
            // Copy the file from temp folder in s3 into the candidate's folder on S3
            this.s3ResourceHelper.copyObject(source, destination);

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("CreateCandidateAttachment")
                .message("[S3] Transferred candidate attachment from source [" + source + "] to destination [" + destination + "]")
                .logInfo();

            // The location is set to the filename because we can derive it's location from the candidate number
            attachment.setLocation(uniqueFilename);
            attachment.setName(request.getName());
            attachment.setType(AttachmentType.file);
            attachment.setFileType(request.getFileType());

            // Extract text from the file
            if(request.getCv()) {
                try {
                    textExtract = TextExtractHelper.getTextExtractFromFile(srcFile, request.getFileType());
                    if(StringUtils.isNotBlank(textExtract)) {
                        attachment.setTextExtract(textExtract);
                        candidateAttachmentRepository.save(attachment);
                    }
                } catch (Exception e) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("CreateCandidateAttachment")
                        .message("Could not extract text from uploaded cv file")
                        .logError(e);

                    attachment.setTextExtract(null);
                }
                attachment.setCv(request.getCv());
            }

        }


        // Update candidate audit fields
        candidate.setAuditFields(user);
        boolean updateCandidateText = request.getCv() != null ? request.getCv() : false;
        candidateService.save(candidate, true, updateCandidateText);

        return candidateAttachmentRepository.save(attachment);
    }

    // Removed @Transactional to fix logged error ObjectDeletedException. There is a risk that now deleting from
    // repository but not from S3 bucket.
    @Override
    public void deleteCandidateAttachment(Long id) {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        CandidateAttachment candidateAttachment = candidateAttachmentRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateAttachment.class, id));

        Candidate candidate;

        // If coming from candidate portal check delete logic
        if (user.getRole().equals(Role.user)) {
            candidate = candidateService.getLoggedInCandidate()
                    .orElseThrow(() -> new InvalidSessionException("Not logged in"));
            // Check that the candidate is deleting an attachment related to themselves
            if (!candidate.getId().equals(candidateAttachment.getCandidate().getId())) {
                throw new InvalidCredentialsException("You do not have permission to perform that action");
            }
            // Check that candidate is only deleting their own uploads
            if (!candidate.getUser().getId().equals(candidateAttachment.getCreatedBy().getId())) {
                throw new InvalidRequestException("You can only delete your own uploads.");
            }
            // Try to delete the record from the database, but throw error if foreign key constraint and the attachment is used as a shareable doc.
            try {
                candidateAttachmentRepository.delete(candidateAttachment);
            } catch (Exception e) {
                throw new InvalidRequestException("Attachment cannot be deleted.");
            }
        } else {
            candidate = candidateRepository.findById(candidateAttachment.getCandidate().getId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateAttachment.getCandidate().getId()));
            // Try to delete the record from the database, but throw error if foreign key constraint and the attachment is used as a shareable doc.
            try {
                candidateAttachmentRepository.delete(candidateAttachment);
            } catch (Exception e) {
                throw new InvalidRequestException("This attachment is selected as a shareable attachment (either below, or within a list) and can't be deleted until deselected.");
            }
        }

        // Update the candidate audit fields
        candidate.setAuditFields(user);
        candidateService.save(candidate, true);

        //Try and delete associated file on file system
        AttachmentType attachmentType = candidateAttachment.getType();
        if (attachmentType != null) {
            switch (attachmentType) {
                case file:
                    String folder = BooleanUtils.isTrue(
                            candidateAttachment.isMigrated()) ? "migrated"
                            : candidate.getCandidateNumber();
                    s3ResourceHelper.deleteFile("candidate/" + folder + "/" + candidateAttachment.getLocation());
                    break;
                case googlefile:
                    GoogleFileSystemFile fsf = new GoogleFileSystemFile(candidateAttachment.getLocation());
                    if (!authService.hasAdminPrivileges(user.getRole())) {
                        fsf.setName("RemovedByCandidate_" + candidateAttachment.getName());
                        try {
                            fileSystemService.renameFile(fsf);
                        } catch (IOException e) {
                            LogBuilder.builder(log)
                                .user(authService.getLoggedInUser())
                                .action("DeleteCandidateAttachment")
                                .message("Could not rename attachment in Google Drive: " + candidateAttachment.getName())
                                .logError(e);
                        }
                    } else {
                        try {
                            fileSystemService.deleteFile(fsf);
                        } catch (IOException e) {
                            LogBuilder.builder(log)
                                .user(authService.getLoggedInUser())
                                .action("DeleteCandidateAttachment")
                                .message("Could not delete attachment from Google Drive: " + fsf)
                                .logError(e);
                        }

                    }
                    break;
            }
        }
    }

    @Override
    public void downloadCandidateAttachment(Long id, OutputStream out)
            throws IOException, NoSuchObjectException {
        CandidateAttachment attachment = getCandidateAttachment(id);
        downloadCandidateAttachment(attachment, out);
    }

    @Override
    public void downloadCandidateAttachment(
            CandidateAttachment attachment, OutputStream out) throws IOException {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        boolean creator = attachment.getCreatedBy().equals(user);
        boolean mine = attachment.getCandidate().equals(user.getCandidate());

        // If from candidate validate they can't access if they aren't the creator, and it's not about them.
        if (user.getRole().equals(Role.user)) {
            if (!creator && !mine) {
                throw new InvalidRequestException("You don't have permission to download this attachment.");
            } else {
                GoogleFileSystemFile file = new GoogleFileSystemFile(attachment.getLocation());
                fileSystemService.downloadFile(file, out);
            }
        } else if (user.getRole().equals(Role.limited) || user.getRole().equals(Role.semilimited)) {
            throw new InvalidRequestException("You don't have permission to download this attachment.");
        } else {
            //We only handle Google attachments for now because that is all
            //we need.
            //We can access link and AWS attachments simply using their urls.
            //We can't do that with Google attachments because of security
            //restrictions with the Google Shared Drive.
            //To get around that, we actually download a copy of the Google
            //file and return that copy to the user's browser.
            if (attachment.getType() == AttachmentType.googlefile) {
                GoogleFileSystemFile file = new GoogleFileSystemFile(attachment.getLocation());
                fileSystemService.downloadFile(file, out);
            }
        }
    }

    @Override
    public CandidateAttachment getCandidateAttachment(Long id)
            throws NoSuchObjectException {
        CandidateAttachment candidateAttachment =
                candidateAttachmentRepository.findByIdLoadCandidate(id)
                .orElseThrow(() ->
                        new NoSuchObjectException(CandidateAttachment.class, id));
        return candidateAttachment;
    }

    @Override
    public CandidateAttachment updateCandidateAttachment(Long id,
            UpdateCandidateAttachmentRequest request) throws IOException, UnauthorisedActionException {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        CandidateAttachment candidateAttachment = getCandidateAttachment(id);
        final AttachmentType attachmentType = candidateAttachment.getType();

        if (authService.authoriseLoggedInUser(candidateAttachment.getCandidate())) {
            // Update the name
            if (!candidateAttachment.getName().equals(request.getName())) {
                candidateAttachment.setName(request.getName());
                if (attachmentType == AttachmentType.googlefile) {
                    //For Google files we also rename the uploaded file
                    GoogleFileSystemFile fsf = new GoogleFileSystemFile(candidateAttachment.getLocation());
                    fsf.setName(request.getName());
                    fileSystemService.renameFile(fsf);
                }
            }

            //Only AWS/S3 files support this CV to not CV and vice versa
            //for CV to non CV and vice versa only applies to AWS/S3 files
            if (attachmentType == AttachmentType.file) {
                // Run text extraction if attachment changed from not CV to a CV or remove if changed from CV to not CV.
                if (request.getCv() && !candidateAttachment.isCv()) {
                    try {
                        String uniqueFilename = candidateAttachment.getLocation();
                        String destination;
                        if (candidateAttachment.isMigrated()) {
                            destination = "candidate/migrated/" + uniqueFilename;
                        } else {
                            destination =
                                "candidate/" + candidateAttachment.getCandidate().getCandidateNumber()
                                    + "/" + uniqueFilename;
                        }
                        File srcFile = this.s3ResourceHelper.downloadFile(
                            this.s3ResourceHelper.getS3Bucket(), destination);
                        String extractedText = TextExtractHelper.getTextExtractFromFile(srcFile,
                            candidateAttachment.getFileType());
                        if (StringUtils.isNotBlank(extractedText)) {
                            candidateAttachment.setTextExtract(extractedText);
                            candidateAttachmentRepository.save(candidateAttachment);
                        }
                    } catch (Exception e) {
                        LogBuilder.builder(log)
                            .user(authService.getLoggedInUser())
                            .action("UpdateCandidateAttachment")
                            .message("Unable to extract text from file " + candidateAttachment.getLocation())
                            .logError(e);

                        candidateAttachment.setTextExtract(null);
                    }
                }
            }
            // UPDATE THE URL LOCATION (IF LINK)
            if (candidateAttachment.getType().equals(AttachmentType.link)) {
                candidateAttachment.setLocation(request.getLocation());
            }
            // UPDATE THE CANDIDATE AUDIT FIELDS
            Candidate candidate = candidateAttachment.getCandidate();
            candidate.setAuditFields(user);
            candidateService.save(candidate, true);
            candidateAttachment.setAuditFields(user);
            candidateAttachmentRepository.save(candidateAttachment);
        } else {
            throw new UnauthorisedActionException("update");
        }

        return candidateAttachment;
    }

    public CandidateAttachment uploadAttachment(@NonNull Candidate candidate,
        String uploadedFileName, @Nullable String subfolderName, MultipartFile file,
        UploadType uploadType) throws IOException, NoSuchObjectException {

        //Save to a temporary file
        File tempFile = File.createTempFile("talent", ".tmp");
        try (FileOutputStream outputStream = new FileOutputStream(tempFile);
            InputStream inputStream = file.getInputStream()) {
            inputStream.transferTo(outputStream);
        }

        //Get link to candidate folder, creating one (plus subfolders) if needed.
        candidate = candidateService.createCandidateFolder(candidate.getId());
        String folderLink = candidate.getFolderlink();

        //Create a folder object for the candidate folder (where the attachment
        //file will be uploaded to)
        GoogleFileSystemFolder parentFolder = new GoogleFileSystemFolder(folderLink);

        //Use the drive associated with the candidate folder.
        GoogleFileSystemDrive candidateDataDrive = fileSystemService.getDriveFromEntity(parentFolder);

        if (subfolderName != null) {
            //Create folder if it does not exist
            GoogleFileSystemFolder subfolder = fileSystemService.findAFolder(
                candidateDataDrive, parentFolder, subfolderName);
            if (subfolder == null) {
                subfolder = fileSystemService.createFolder(
                    candidateDataDrive, parentFolder, subfolderName);

                //We make upload subfolders viewable by anyone with link so that they and their
                //contents can be shared externally (eg with migration agents).
                fileSystemService.publishFolder(subfolder);
            }

            //Set parentFolder to subfolder
            parentFolder = subfolder;
        }

        //Upload the file to its folder, with the correct name (not the temp
        //file name).
        GoogleFileSystemFile uploadedFile = fileSystemService.uploadFile(
            candidateDataDrive, parentFolder, uploadedFileName, tempFile);

        final String fileType = getFileExtension(uploadedFileName);

        //Do text extraction if CV - otherwise leave as null.
        String textExtract = null;
        if(uploadType == UploadType.cv) {
            try {
                textExtract = TextExtractHelper.getTextExtractFromFile(tempFile, fileType);
            } catch (Exception e) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("UploadAttachment")
                    .message("Could not extract text from uploaded file")
                    .logError(e);
            }
        }

        //Delete tempfile
        if (!tempFile.delete()) {
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("UploadAttachment")
                .message("Failed to delete temporary file " + tempFile)
                .logError();
        }

        //Now create corresponding CandidateAttachment record.
        CreateCandidateAttachmentRequest req = new CreateCandidateAttachmentRequest();
        req.setCandidateId(candidate.getId());
        req.setType(AttachmentType.googlefile);
        req.setName(uploadedFileName);
        req.setFileType(fileType);
        req.setLocation(uploadedFile.getUrl());
        req.setUploadType(uploadType);
        req.setCv(uploadType == UploadType.cv);
        if(StringUtils.isNotBlank(textExtract)) {
            // Remove any null bytes to avoid PSQLException: ERROR: invalid byte sequence for encoding "UTF8"
            textExtract = Pattern.compile("\\x00").matcher(textExtract).replaceAll("?");
            req.setTextExtract(textExtract);
        }

        CandidateAttachment attachment = createCandidateAttachment(req);

        return attachment;

    }

    @Override
    @NonNull
    public CandidateAttachment uploadAttachment(
            @NonNull Long candidateId, Boolean cv, MultipartFile file )
            throws IOException, NoSuchObjectException {

        Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));

        //Name of file being uploaded (this is the name it had on the
        //originating computer).
        String fileName = file.getOriginalFilename();

        UploadType uploadType = cv ? UploadType.cv : UploadType.other;
        return uploadAttachment(candidate, fileName, null, file, uploadType);
    }

    @Override
    @NonNull
    public CandidateAttachment uploadAttachment(Boolean cv, MultipartFile file)
            throws IOException, InvalidSessionException {
        Long candidateId = authService.getLoggedInCandidateId();
        if (candidateId == null) {
            throw new InvalidSessionException("Not logged in");
        }

        return uploadAttachment(candidateId, cv, file);
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return filename.substring(lastIndexOf+1);
    }
}
