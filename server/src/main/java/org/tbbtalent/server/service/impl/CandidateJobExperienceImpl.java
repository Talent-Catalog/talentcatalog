package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateJobExperience;
import org.tbbtalent.server.model.CandidateOccupation;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.repository.CandidateOccupationRepository;
import org.tbbtalent.server.repository.CountryRepository;
import org.tbbtalent.server.repository.CandidateJobExperienceRepository;
import org.tbbtalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateJobExperienceService;

@Service
public class CandidateJobExperienceImpl implements CandidateJobExperienceService {

    private final CandidateJobExperienceRepository candidateJobExperienceRepository;
    private final CountryRepository countryRepository;
    private final CandidateOccupationRepository candidateOccupationRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateJobExperienceImpl(CandidateJobExperienceRepository candidateJobExperienceRepository,
                                      CandidateOccupationRepository candidateOccupationRepository,
                                      CountryRepository countryRepository,
                                      UserContext userContext) {
        this.candidateJobExperienceRepository = candidateJobExperienceRepository;
        this.countryRepository = countryRepository;
        this.candidateOccupationRepository = candidateOccupationRepository;
        this.userContext = userContext;
    }


    @Override
    public CandidateJobExperience createJobExperience(CreateJobExperienceRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        Long test = 3L;


        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        // Load the candidate occupation from the database - throw an exception if not found
        CandidateOccupation occupation = candidateOccupationRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(CandidateOccupation.class, request.getCandidateOccupationId()));

        // Create a new candidateOccupation object to insert into the database
        CandidateJobExperience candidateJobExperience = new CandidateJobExperience();
        candidateJobExperience.setCandidate(candidate);
        candidateJobExperience.setCountry(country);
        candidateJobExperience.setCandidateOccupation(occupation);
        candidateJobExperience.setCompanyName(request.getCompanyName());
        candidateJobExperience.setRole(request.getRole());
        candidateJobExperience.setStartDate(request.getStartDate());
        candidateJobExperience.setEndDate(request.getEndDate());
        candidateJobExperience.setFullTime(request.getFullTime());
        candidateJobExperience.setPaid(request.getPaid());
        candidateJobExperience.setDescription(request.getDescription());

        // Save the candidateOccupation
        return candidateJobExperienceRepository.save(candidateJobExperience);
    }

    @Override
    public void deleteJobExperience(Long id) {
        Candidate candidate = userContext.getLoggedInCandidate();
        CandidateJobExperience candidateJobExperience = candidateJobExperienceRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateJobExperience.class, id));

        // Check that the user is deleting their own candidateOccupation
        if (!candidate.getId().equals(candidateJobExperience.getCandidate().getId())) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }

        candidateJobExperienceRepository.delete(candidateJobExperience);
    }
}
