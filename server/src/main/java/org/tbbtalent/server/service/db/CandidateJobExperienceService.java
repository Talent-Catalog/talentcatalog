/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.model.db.CandidateJobExperience;
import org.tbbtalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.SearchJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.UpdateJobExperienceRequest;

public interface CandidateJobExperienceService {

    Page<CandidateJobExperience> searchCandidateJobExperience(SearchJobExperienceRequest request);

    CandidateJobExperience createCandidateJobExperience(CreateJobExperienceRequest request);

    CandidateJobExperience updateCandidateJobExperience(UpdateJobExperienceRequest request);

    CandidateJobExperience updateCandidateJobExperience(Long candidateId, UpdateJobExperienceRequest request);

    void deleteCandidateJobExperience(Long id);
}
