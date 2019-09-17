package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Education;
import org.tbbtalent.server.repository.EducationRepository;
import org.tbbtalent.server.request.education.CreateEducationRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.EducationService;

@Service
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;
    private final UserContext userContext;


    @Autowired
    public EducationServiceImpl(EducationRepository educationRepository,
                                 UserContext userContext) {
        this.educationRepository = educationRepository;
        this.userContext = userContext;
    }


    @Override
    public Education createEducation(CreateEducationRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Create a new profession object to insert into the database
        Education education = new Education();
        education.setCandidate(candidate);
        education.setEducationType(Education.EducationType.valueOf(request.getEducationType()));
        education.setCountryId(request.getCountryId());
        education.setLengthOfCourseYears(request.getLengthOfCourseYears());
        education.setInstitution(request.getInstitution());
        education.setCourseName(request.getCourseName());
        education.setDateCompleted(request.getDateCompleted());

        // Save the profession
        return educationRepository.save(education);
    }

}
