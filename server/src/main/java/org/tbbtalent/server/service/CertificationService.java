package org.tbbtalent.server.service;

import org.tbbtalent.server.model.Certification;
import org.tbbtalent.server.request.certification.CreateCertificationRequest;

public interface CertificationService {

    Certification createCertification(CreateCertificationRequest request);

    void deleteCertification(Long id);

}
