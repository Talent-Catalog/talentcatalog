package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateJobExperience;
import org.tbbtalent.server.request.work.experience.CreateJobExperienceRequest;

public interface CandidateJobExperienceService {

    CandidateJobExperience createCandidateJobExperience(CreateJobExperienceRequest request);

    CandidateJobExperience createCandidateJobExperience(Long id, CreateJobExperienceRequest request);

    void deleteCandidateJobExperience(Long id);
}
