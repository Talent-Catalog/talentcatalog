package org.tbbtalent.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.ServiceException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateAttachment;
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
        return candidateAttachmentRepository.findByCandidateId(candidate.getId());
    }

    @Override
    public CandidateAttachment createCandidateAttachment(CreateCandidateAttachmentRequest request, Boolean adminOnly) {
        Candidate candidate;
        /* Handle requests coming from the admin portal */
        if (request.getCandidateId() != null) {
            candidate = candidateRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));
        } else {
            candidate = userContext.getLoggedInCandidate();
        }

        /* Prepend the filename with a UUID to ensure it's an existing file doesn't get overwritten on S3 */
        String uniqueFilename = UUID.randomUUID() + "_" + request.getName();
        /* Copy the file into the candidate's folder on S3 */
        try {
            this.s3ResourceHelper.copyObject(
                    "temp/" + request.getFolder() + "/" + request.getName(),
                    "candidate/" + candidate.getCandidateNumber() + "/" + uniqueFilename
            );
        } catch (Exception e) {
            log.error("An error occurred while transferring a file on S3", e);
            throw new ServiceException("s3_file_transfer_exception", "An error occurred while uploading your file");
        }

        /* Create a record of the attachment */
        CandidateAttachment attachment = new CandidateAttachment();
        attachment.setCandidate(candidate);
        attachment.setName(uniqueFilename);
        // The location is set to the filename because we can derive it's location from the candidate data
        attachment.setLocation(uniqueFilename);
        attachment.setFileType(request.getFileType());
        /* TODO: Full path or partial S3 path? */
        attachment.setMigrated(false);
        attachment.setAdminOnly(adminOnly);
        return candidateAttachmentRepository.save(attachment);
    }

}
