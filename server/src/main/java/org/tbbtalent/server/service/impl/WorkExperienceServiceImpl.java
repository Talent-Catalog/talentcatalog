package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.model.CandidateJobExperience;
import org.tbbtalent.server.repository.CountryRepository;
import org.tbbtalent.server.repository.WorkExperienceRepository;
import org.tbbtalent.server.request.work.experience.CreateWorkExperienceRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.WorkExperienceService;

@Service
public class WorkExperienceServiceImpl implements WorkExperienceService {

    private final WorkExperienceRepository workExperienceRepository;
    private final CountryRepository countryRepository;
    private final UserContext userContext;

    @Autowired
    public WorkExperienceServiceImpl(WorkExperienceRepository workExperienceRepository,
                                     CountryRepository countryRepository,
                                     UserContext userContext) {
        this.workExperienceRepository = workExperienceRepository;
        this.countryRepository = countryRepository;
        this.userContext = userContext;
    }


    @Override
    public CandidateJobExperience createWorkExperience(CreateWorkExperienceRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        Long test = 3L;


        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        // Create a new profession object to insert into the database
        CandidateJobExperience candidateJobExperience = new CandidateJobExperience();
        candidateJobExperience.setCandidate(candidate);
        candidateJobExperience.setCountry(country);
        candidateJobExperience.setCompanyName(request.getCompanyName());
        candidateJobExperience.setJobTitle(request.getRole());
        candidateJobExperience.setStartDate(request.getStartDate());
        candidateJobExperience.setEndDate(request.getEndDate());
        candidateJobExperience.setFullTime(request.getFullTime());
        candidateJobExperience.setPaid(request.getPaid());
        candidateJobExperience.setDescription(request.getDescription());

        // Save the profession
        return workExperienceRepository.save(candidateJobExperience);
    }

    @Override
    public void deleteWorkExperience(Long id) {
        Candidate candidate = userContext.getLoggedInCandidate();
        CandidateJobExperience candidateJobExperience = workExperienceRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateJobExperience.class, id));

        // Check that the user is deleting their own profession
        if (!candidate.getId().equals(candidateJobExperience.getCandidate().getId())) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }

        workExperienceRepository.delete(candidateJobExperience);
    }
}
