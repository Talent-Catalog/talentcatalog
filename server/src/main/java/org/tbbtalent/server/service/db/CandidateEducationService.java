/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;

public interface CandidateEducationService {

    CandidateEducation createCandidateEducation(CreateCandidateEducationRequest request);

    CandidateEducation updateCandidateEducation(UpdateCandidateEducationRequest request);
    
    CandidateEducation updateCandidateEducation(Long id, UpdateCandidateEducationRequest request);

    CandidateEducation createCandidateEducation(long id, CreateCandidateEducationRequest request);

    List<CandidateEducation> list(long id);

    void deleteCandidateEducation(Long id);
}
