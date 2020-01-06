package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateCertification;
import org.tbbtalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tbbtalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;

import java.util.List;

public interface CandidateCertificationService {

    CandidateCertification createCandidateCertification(CreateCandidateCertificationRequest request);

    void deleteCandidateCertification(Long id);

    CandidateCertification createCandidateCertificationAdmin(long id, CreateCandidateCertificationRequest request);

    CandidateCertification updateCandidateCertificationAdmin(long id, UpdateCandidateCertificationRequest request);

    List<CandidateCertification> list(long id);

}
