package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.model.EducationType;
import org.tbbtalent.server.repository.CandidateEducationRepository;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.CountryRepository;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateEducationService;

import java.util.List;

@Service
public class CandidateEducationServiceImpl implements CandidateEducationService {

    private final CandidateEducationRepository candidateEducationRepository;
    private final CountryRepository countryRepository;
    private final CandidateRepository candidateRepository;
    private final UserContext userContext;


    @Autowired
    public CandidateEducationServiceImpl(CandidateEducationRepository candidateEducationRepository,
                                         CountryRepository countryRepository,
                                         CandidateRepository candidateRepository,
                                         UserContext userContext) {
        this.candidateEducationRepository = candidateEducationRepository;
        this.countryRepository = countryRepository;
        this.candidateRepository = candidateRepository;
        this.userContext = userContext;
    }


    @Override
    public CandidateEducation createCandidateEducation(CreateCandidateEducationRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        // Get ENUM for education type
        EducationType educationType = request.getEducationType();

        // Create a new candidateOccupation object to insert into the database
        CandidateEducation candidateEducation = new CandidateEducation();
        candidateEducation.setCandidate(candidate);
        candidateEducation.setEducationType(educationType);
        candidateEducation.setCountry(country);
        candidateEducation.setLengthOfCourseYears(request.getLengthOfCourseYears());
        candidateEducation.setInstitution(request.getInstitution());
        candidateEducation.setCourseName(request.getCourseName());
        candidateEducation.setDateCompleted(request.getDateCompleted());

        // Save the candidateOccupation
        return candidateEducationRepository.save(candidateEducation);
    }

    @Override
    public CandidateEducation updateCandidateEducation(CreateCandidateEducationRequest request) {
        // Get ENUM for education type
        EducationType educationType = request.getEducationType();

        Candidate candidate = userContext.getLoggedInCandidate();

        CandidateEducation candidateEducation = candidateEducationRepository.findByIdLoadEducationType(educationType);

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
        candidateEducation.setDateCompleted(request.getDateCompleted());

        // Save the candidateOccupation
        return candidateEducationRepository.save(candidateEducation);
    }

    @Override
    public List<CandidateEducation> list(long id) {
        return candidateEducationRepository.findByCandidateId(id);
    }

    @Override
    public CandidateEducation createCandidateEducationAdmin(long candidateId, CreateCandidateEducationRequest request) {
        Candidate candidate = this.candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        // Get ENUM for education type
        EducationType educationType = request.getEducationType();

        // Create a new candidateOccupation object to insert into the database
        CandidateEducation candidateEducation = new CandidateEducation();
        candidateEducation.setCandidate(candidate);
        candidateEducation.setEducationType(educationType);
        candidateEducation.setCountry(country);
        candidateEducation.setLengthOfCourseYears(request.getLengthOfCourseYears());
        candidateEducation.setInstitution(request.getInstitution());
        candidateEducation.setCourseName(request.getCourseName());
        candidateEducation.setDateCompleted(request.getDateCompleted());

        // Save the candidateOccupation
        return candidateEducationRepository.save(candidateEducation);
    }

    @Override
    public CandidateEducation updateCandidateEducationAdmin(long id, UpdateCandidateEducationRequest request) {

        CandidateEducation candidateEducation = this.candidateEducationRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateEducation.class, id));

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));


        // Update education object to insert into the database
        candidateEducation.setCountry(country);
        candidateEducation.setLengthOfCourseYears(request.getLengthOfCourseYears());
        candidateEducation.setInstitution(request.getInstitution());
        candidateEducation.setCourseName(request.getCourseName());
        candidateEducation.setDateCompleted(request.getDateCompleted());

        // Save the candidateOccupation
        return candidateEducationRepository.save(candidateEducation);

    }
}
