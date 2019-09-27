package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateCertification;
import org.tbbtalent.server.repository.CertificationRepository;
import org.tbbtalent.server.request.certification.CreateCertificationRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CertificationService;

@Service
public class CertificationServiceImpl implements CertificationService {

    private final CertificationRepository certificationRepository;
    private final UserContext userContext;

    @Autowired
    public CertificationServiceImpl(CertificationRepository certificationRepository,
                                     UserContext userContext) {
        this.certificationRepository = certificationRepository;
        this.userContext = userContext;
    }


    @Override
    public CandidateCertification createCertification(CreateCertificationRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Create a new candidateOccupation object to insert into the database
        CandidateCertification candidateCertification = new CandidateCertification();
        candidateCertification.setCandidate(candidate);
        candidateCertification.setName(request.getName());
        candidateCertification.setInstitution(request.getInstitution());
        candidateCertification.setDateCompleted(request.getDateCompleted());

        // Save the candidateOccupation
        return certificationRepository.save(candidateCertification);
    }

    @Override
    public void deleteCertification(Long id) {
        Candidate candidate = userContext.getLoggedInCandidate();
        CandidateCertification candidateCertification = certificationRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateCertification.class, id));

        // Check that the user is deleting their own candidateOccupation
        if (!candidate.getId().equals(candidateCertification.getCandidate().getId())) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }

        certificationRepository.delete(candidateCertification);
    }
}
