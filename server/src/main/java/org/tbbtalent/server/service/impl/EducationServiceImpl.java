package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.model.Country;
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
    public CandidateEducation createEducation(CreateEducationRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        // Get ENUM for education type
        EducationType educationType = EducationType.valueOf(request.getEducationType());

        // Create a new candidateOccupation object to insert into the database
        CandidateEducation candidateEducation = new CandidateEducation();
        candidateEducation.setCandidate(candidate);
        candidateEducation.setEducationType(educationType);
        candidateEducation.setCountry(country);
        candidateEducation.setLengthOfCourseYears(request.getLengthOfCourseYears());
        candidateEducation.setInstitution(request.getInstitution());
        candidateEducation.setCourseName(request.getCourseName());
        candidateEducation.setYearCompleted(request.getYearCompleted());

        // Save the candidateOccupation
        return educationRepository.save(candidateEducation);
    }

    @Override
    public CandidateEducation updateEducation(CreateEducationRequest request) {
        // Get ENUM for education type
        EducationType educationType = EducationType.valueOf(request.getEducationType());

        Candidate candidate = userContext.getLoggedInCandidate();

        CandidateEducation candidateEducation = educationRepository.findByIdLoadEducationType(educationType);

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));


        // Update education object to insert into the database
        candidateEducation.setCandidate(candidate);
        candidateEducation.setEducationType(educationType);
        candidateEducation.setCountry(country);
        candidateEducation.setLengthOfCourseYears(request.getLengthOfCourseYears());
        candidateEducation.setInstitution(request.getInstitution());
        candidateEducation.setCourseName(request.getCourseName());
        candidateEducation.setYearCompleted(request.getYearCompleted());

        // Save the candidateOccupation
        return educationRepository.save(candidateEducation);
    }

}
