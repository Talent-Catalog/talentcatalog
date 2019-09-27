package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateJobExperience;
import org.tbbtalent.server.request.work.experience.CreateJobExperienceRequest;

public interface CandidateJobExperienceService {

    CandidateJobExperience createJobExperience(CreateJobExperienceRequest request);

    void deleteJobExperience(Long id);
}
