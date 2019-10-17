package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.repository.CandidateEducationRepository;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.CountryRepository;
import org.tbbtalent.server.repository.EducationMajorRepository;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateEducationService;

import java.util.List;

@Service
public class CandidateEducationServiceImpl implements CandidateEducationService {

    private final CandidateEducationRepository candidateEducationRepository;
    private final CountryRepository countryRepository;
    private final EducationMajorRepository educationMajorRepository;
    private final CandidateRepository candidateRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateEducationServiceImpl(CandidateEducationRepository candidateEducationRepository,
                                         CountryRepository countryRepository,
                                         EducationMajorRepository educationMajorRepository, CandidateRepository candidateRepository,
                                         UserContext userContext) {
        this.candidateEducationRepository = candidateEducationRepository;
        this.countryRepository = countryRepository;
        this.educationMajorRepository = educationMajorRepository;
        this.candidateRepository = candidateRepository;
        this.userContext = userContext;
    }

    @Override
    public CandidateEducation createCandidateEducation(CreateCandidateEducationRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        return createCandidateEducation(candidate, request);
    }

    @Override
    public CandidateEducation updateCandidateEducation(UpdateCandidateEducationRequest request) {
        // Get ENUM for education type
        EducationType educationType = request.getEducationType();

        CandidateEducation candidateEducation = candidateEducationRepository.findById(request.getId())
                .orElseThrow(() -> new NoSuchObjectException(CandidateEducation.class, request.getId()));

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        EducationMajor educationMajor = educationMajorRepository.findById(request.getMajorId())
                .orElseThrow(() -> new NoSuchObjectException(EducationMajor.class, request.getMajorId()));

        // Update education object to insert into the database
        candidateEducation.setEducationType(educationType);
        candidateEducation.setCountry(country);
        candidateEducation.setEducationMajor(educationMajor);
        candidateEducation.setLengthOfCourseYears(request.getLengthOfCourseYears());
        candidateEducation.setInstitution(request.getInstitution());
        candidateEducation.setCourseName(request.getCourseName());
        candidateEducation.setYearCompleted(request.getYearCompleted());

        // Save the candidateOccupation
        return candidateEducationRepository.save(candidateEducation);
    }

    @Override
    public List<CandidateEducation> list(long id) {
        return candidateEducationRepository.findByCandidateId(id);
    }

    @Override
    public CandidateEducation createCandidateEducation(long candidateId, CreateCandidateEducationRequest request) {
        Candidate candidate = this.candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));
        return createCandidateEducation(candidate, request);
    }


    private CandidateEducation createCandidateEducation(Candidate candidate, CreateCandidateEducationRequest request) {
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        EducationMajor educationMajor = educationMajorRepository.findById(request.getEducationMajorId())
                .orElseThrow(() -> new NoSuchObjectException(EducationMajor.class, request.getEducationMajorId()));

        // Get ENUM for education type
        EducationType educationType = request.getEducationType();

        // Create a new candidateOccupation object to insert into the database
        CandidateEducation candidateEducation = new CandidateEducation();
        candidateEducation.setCandidate(candidate);
        candidateEducation.setEducationType(educationType);
        candidateEducation.setCountry(country);
        candidateEducation.setEducationMajor(educationMajor);
        candidateEducation.setLengthOfCourseYears(request.getLengthOfCourseYears());
        candidateEducation.setInstitution(request.getInstitution());
        candidateEducation.setCourseName(request.getCourseName());
        candidateEducation.setYearCompleted(request.getYearCompleted());

        // Save the candidateOccupation
        return candidateEducationRepository.save(candidateEducation);
    }



}
