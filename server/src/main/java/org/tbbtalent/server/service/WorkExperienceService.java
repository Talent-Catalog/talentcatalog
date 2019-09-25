package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateJobExperience;
import org.tbbtalent.server.request.work.experience.CreateWorkExperienceRequest;

public interface WorkExperienceService {

    CandidateJobExperience createWorkExperience(CreateWorkExperienceRequest request);

    void deleteWorkExperience(Long id);
}
