package org.tbbtalent.server.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.EducationMajor;
import org.tbbtalent.server.model.db.EducationType;
import org.tbbtalent.server.repository.db.CandidateEducationRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.repository.db.EducationMajorRepository;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateEducationService;

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
        CandidateEducation education = createCandidateEducation(candidate, request);

        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);

        return education;
    }

    @Override
    public CandidateEducation updateCandidateEducation(Long id, UpdateCandidateEducationRequest request) {
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
        candidateEducation.setIncomplete(request.getIncomplete());

        // Save the candidateOccupation
        return candidateEducationRepository.save(candidateEducation);
    }

    @Override
    public CandidateEducation updateCandidateEducation(UpdateCandidateEducationRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        CandidateEducation candidateEducation = updateCandidateEducation(candidate.getId(), request);
        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);
        return candidateEducation;
    }

    @Override
    public List<CandidateEducation> list(long id) {
        return candidateEducationRepository.findByCandidateId(id);
    }

    @Override
    public void deleteCandidateEducation(Long id) {
        Candidate candidate = userContext.getLoggedInCandidate();
        CandidateEducation candidateEducation = candidateEducationRepository.findByIdAndCandidateId(id, candidate.getId());
        if (candidateEducation == null) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }
        candidateEducationRepository.delete(candidateEducation);

        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);
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

        EducationMajor educationMajor = null;
        if (request.getEducationMajorId() != null) {
            educationMajor = educationMajorRepository.findById(request.getEducationMajorId())
                    .orElseThrow(() -> new NoSuchObjectException(EducationMajor.class, request.getEducationMajorId()));
        }

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
        candidateEducation.setIncomplete(request.getIncomplete());
        // Save the candidate education
        candidateEducation = candidateEducationRepository.save(candidateEducation);
        // Add the major back to the saved object
        candidateEducation.setEducationMajor(educationMajor);
        // Save the candidateOccupation
        return candidateEducation;
    }



}
