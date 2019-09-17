package org.tbbtalent.server.service;

import org.tbbtalent.server.model.Education;
import org.tbbtalent.server.request.education.CreateEducationRequest;

public interface EducationService {

    Education createEducation(CreateEducationRequest request);

}
