package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateCertification;
import org.tbbtalent.server.request.certification.CreateCertificationRequest;

public interface CertificationService {

    CandidateCertification createCertification(CreateCertificationRequest request);

    void deleteCertification(Long id);

}
