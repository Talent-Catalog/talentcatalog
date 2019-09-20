package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Certification;
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
    public Certification createCertification(CreateCertificationRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Create a new profession object to insert into the database
        Certification certification = new Certification();
        certification.setCandidate(candidate);
        certification.setName(request.getName());
        certification.setInstitution(request.getInstitution());
        certification.setDateCompleted(request.getDateCompleted());

        // Save the profession
        return certificationRepository.save(certification);
    }

    @Override
    public void deleteCertification(Long id) {
        Candidate candidate = userContext.getLoggedInCandidate();
        Certification certification = certificationRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(Certification.class, id));

        // Check that the user is deleting their own profession
        if (!candidate.getId().equals(certification.getCandidate().getId())) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }

        certificationRepository.delete(certification);
    }
}
