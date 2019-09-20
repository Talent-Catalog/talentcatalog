package org.tbbtalent.server.service;

import org.tbbtalent.server.model.WorkExperience;
import org.tbbtalent.server.request.work.experience.CreateWorkExperienceRequest;

public interface WorkExperienceService {

    WorkExperience createWorkExperience(CreateWorkExperienceRequest request);

    void deleteWorkExperience(Long id);
}
