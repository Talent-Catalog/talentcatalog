package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.model.CandidateJobExperience;
import org.tbbtalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.SearchJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.UpdateJobExperienceRequest;

public interface CandidateJobExperienceService {

    Page<CandidateJobExperience> searchCandidateJobExperience(SearchJobExperienceRequest request);

    CandidateJobExperience createCandidateJobExperience(CreateJobExperienceRequest request);

    CandidateJobExperience createCandidateJobExperience(Long id, CreateJobExperienceRequest request);

    CandidateJobExperience updateCandidateJobExperience(Long id, UpdateJobExperienceRequest request);

    void deleteCandidateJobExperience(Long id);
}
