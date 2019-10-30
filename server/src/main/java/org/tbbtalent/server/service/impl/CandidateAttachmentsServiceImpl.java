package org.tbbtalent.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.AttachmentType;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.repository.CandidateAttachmentRepository;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.request.SearchRequest;
import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tbbtalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateAttachmentService;
import org.tbbtalent.server.service.aws.S3ResourceHelper;

import java.util.List;
import java.util.UUID;

@Service
public class CandidateAttachmentsServiceImpl implements CandidateAttachmentService {

    private static final Logger log = LoggerFactory.getLogger(CandidateAttachmentsServiceImpl.class);

    private final CandidateRepository candidateRepository;
    private final CandidateAttachmentRepository candidateAttachmentRepository;
    private final UserContext userContext;
    private final S3ResourceHelper s3ResourceHelper;

    @Value("{aws.s3.files-bucket}")
    String s3Bucket;

    @Autowired
    public CandidateAttachmentsServiceImpl(CandidateRepository candidateRepository,
                                           CandidateAttachmentRepository candidateAttachmentRepository,
                                           S3ResourceHelper s3ResourceHelper,
                                           UserContext userContext) {
        this.candidateRepository = candidateRepository;
        this.candidateAttachmentRepository = candidateAttachmentRepository;
        this.s3ResourceHelper = s3ResourceHelper;
        this.userContext = userContext;
    }

    @Override
    public Page<CandidateAttachment> searchCandidateAttachments(SearchCandidateAttachmentsRequest request) {
        return candidateAttachmentRepository.findByCandidateId(request.getCandidateId(), request.getPageRequest());
    }

    @Override
    public Page<CandidateAttachment> searchCandidateAttachmentsForLoggedInCandidate(SearchRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        return candidateAttachmentRepository.findByCandidateId(candidate.getId(), request.getPageRequest());
    }

    @Override
    public List<CandidateAttachment> listCandidateAttachmentsForLoggedInCandidate() {
        Candidate candidate = userContext.getLoggedInCandidate();
        return candidateAttachmentRepository.findByCandidateIdLoadAudit(candidate.getId());
    }

    @Override
    public CandidateAttachment createCandidateAttachment(CreateCandidateAttachmentRequest request, Boolean adminOnly) {
        User user = userContext.getLoggedInUser();
        Candidate candidate;

        // Handle requests coming from the admin portal
        if (request.getCandidateId() != null) {
            candidate = candidateRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));
        } else {
            candidate = userContext.getLoggedInCandidate();
        }

        // Create a record of the attachment
        CandidateAttachment attachment = new CandidateAttachment();

        if (request.getType().equals(AttachmentType.link)) {

            attachment.setType(AttachmentType.link);
            attachment.setLocation(request.getLocation());
            attachment.setName(request.getName());

        } else if (request.getType().equals(AttachmentType.file)) {
            // Upload the file to AWS S3

            // Prepend the filename with a UUID to ensure an existing file doesn't get overwritten on S3
            String uniqueFilename = UUID.randomUUID() + "_" + request.getName();
            // Copy the file into the candidate's folder on S3
            String source = "temp/" + request.getFolder() + "/" + request.getName();
            String destination = "candidate/" + candidate.getCandidateNumber() + "/" + uniqueFilename;
            this.s3ResourceHelper.copyObject(source, destination);

            log.info("[S3] Transferred candidate attachment from source [" + source + "] to destination [" + destination + "]");

            // The location is set to the filename because we can derive it's location from the candidate number
            attachment.setLocation(uniqueFilename);
            attachment.setName(uniqueFilename);
            attachment.setType(AttachmentType.file);
            attachment.setFileType(request.getFileType());
        }

        attachment.setCandidate(candidate);
        attachment.setMigrated(false);
        attachment.setAdminOnly(adminOnly);
        attachment.setAuditFields(user);

        // Update candidate audit fields
        candidate.setAuditFields(user);
        candidateRepository.save(candidate);

        return candidateAttachmentRepository.save(attachment);
    }

    @Override
    @Transactional
    public void deleteCandidateAttachment(Long id) {
        Candidate candidate = userContext.getLoggedInCandidate();
        CandidateAttachment candidateAttachment = candidateAttachmentRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateAttachment.class, id));

        // Check that the user is deleting their own attachment
        if (!candidate.getId().equals(candidateAttachment.getCandidate().getId())) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }

        // Delete the record from the database
        candidateAttachmentRepository.delete(candidateAttachment);

        // Delete the object on S3
        s3ResourceHelper.deleteFile("candidate/" + candidate.getCandidateNumber() + "/" + candidateAttachment.getName());

        // Update the candidate audit fields
        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);
    }

}
