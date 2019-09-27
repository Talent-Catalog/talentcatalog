package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.model.Education;
import org.tbbtalent.server.model.EducationType;
import org.tbbtalent.server.repository.CountryRepository;
import org.tbbtalent.server.repository.EducationRepository;
import org.tbbtalent.server.request.education.CreateEducationRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.EducationService;

@Service
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;
    private final CountryRepository countryRepository;
    private final UserContext userContext;


    @Autowired
    public EducationServiceImpl(EducationRepository educationRepository,
                                 CountryRepository countryRepository,
                                 UserContext userContext) {
        this.educationRepository = educationRepository;
        this.countryRepository = countryRepository;
        this.userContext = userContext;
    }


    @Override
    public Education createEducation(CreateEducationRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        // Get ENUM for education type
        EducationType educationType = EducationType.valueOf(request.getEducationType());

        // Create a new candidateOccupation object to insert into the database
        Education education = new Education();
        education.setCandidate(candidate);
        education.setEducationType(educationType);
        education.setCountry(country);
        education.setLengthOfCourseYears(request.getLengthOfCourseYears());
        education.setInstitution(request.getInstitution());
        education.setCourseName(request.getCourseName());
        education.setDateCompleted(request.getDateCompleted());

        // Save the candidateOccupation
        return educationRepository.save(education);
    }

    @Override
    public Education updateEducation(CreateEducationRequest request) {
        // Get ENUM for education type
        EducationType educationType = EducationType.valueOf(request.getEducationType());

        Candidate candidate = userContext.getLoggedInCandidate();

        Education education = educationRepository.findByIdLoadEducationType(educationType);

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));


        // Update education object to insert into the database
        education.setCandidate(candidate);
        education.setEducationType(educationType);
        education.setCountry(country);
        education.setLengthOfCourseYears(request.getLengthOfCourseYears());
        education.setInstitution(request.getInstitution());
        education.setCourseName(request.getCourseName());
        education.setDateCompleted(request.getDateCompleted());

        // Save the candidateOccupation
        return educationRepository.save(education);
    }

}
