package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.request.education.CreateEducationRequest;

public interface EducationService {

    CandidateEducation createEducation(CreateEducationRequest request);

    CandidateEducation updateEducation(CreateEducationRequest request);

}
