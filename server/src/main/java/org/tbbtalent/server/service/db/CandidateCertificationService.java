/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.tbbtalent.server.model.db.CandidateCertification;
import org.tbbtalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tbbtalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;

public interface CandidateCertificationService {

    CandidateCertification createCandidateCertification(CreateCandidateCertificationRequest request);

    void deleteCandidateCertification(Long id);

    CandidateCertification createCandidateCertificationAdmin(long id, CreateCandidateCertificationRequest request);

    CandidateCertification updateCandidateCertificationAdmin(long id, UpdateCandidateCertificationRequest request);

    List<CandidateCertification> list(long id);

}
