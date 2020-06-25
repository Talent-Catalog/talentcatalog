package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.repository.CandidateJobExperienceRepository;
import org.tbbtalent.server.repository.CandidateOccupationRepository;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.CountryRepository;
import org.tbbtalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.SearchJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.UpdateJobExperienceRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateJobExperienceService;

@Service
public class CandidateJobExperienceImpl implements CandidateJobExperienceService {

    private final CandidateJobExperienceRepository candidateJobExperienceRepository;
    private final CountryRepository countryRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateOccupationRepository candidateOccupationRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateJobExperienceImpl(CandidateJobExperienceRepository candidateJobExperienceRepository,
                                      CandidateOccupationRepository candidateOccupationRepository,
                                      CountryRepository countryRepository,
                                      CandidateRepository candidateRepository,
                                      UserContext userContext) {
        this.candidateJobExperienceRepository = candidateJobExperienceRepository;
        this.countryRepository = countryRepository;
        this.candidateRepository = candidateRepository;
        this.candidateOccupationRepository = candidateOccupationRepository;
        this.userContext = userContext;
    }

    @Override
    public Page<CandidateJobExperience> searchCandidateJobExperience(SearchJobExperienceRequest request) {
        if (request.getCandidateId() != null) {
            return candidateJobExperienceRepository.findByCandidateId(request.getCandidateId(), request.getPageRequest());
        } else {
            return candidateJobExperienceRepository.findByCandidateOccupationId(request.getCandidateOccupationId(), request.getPageRequest());
        }
    }

    @Override
    public CandidateJobExperience createCandidateJobExperience(CreateJobExperienceRequest request) {
        Candidate candidate;
        /* Check if the candidate ID is explicitly set - this means the request is coming from admin */
        if (request.getCandidateId() != null) {
            candidate = candidateRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));
        } else {
            candidate = userContext.getLoggedInCandidate();
            candidate.setAuditFields(candidate.getUser());
            candidateRepository.save(candidate);
        }

        // Load the country from the database - throw an exception if not found
        Country country = getCountry(request.getCountryId());

        // Load the candidate occupation from the database - throw an exception if not found
        CandidateOccupation occupation = getCandidateOccupation(request.getCandidateOccupationId());

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
    public CandidateJobExperience updateCandidateJobExperience(UpdateJobExperienceRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        CandidateJobExperience experience = updateCandidateJobExperience(request.getId(), request);
        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);

        return experience;
    }

    @Override
    public CandidateJobExperience updateCandidateJobExperience(Long id, UpdateJobExperienceRequest request) {
        // Load the candidate from the database - throw an exception if not found
        CandidateJobExperience candidateJobExperience = candidateJobExperienceRepository
                .findByIdLoadCandidateOccupation(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateJobExperience.class, id));

        Country country = getCountry(request.getCountryId());

        // Default to the existing candidate occupation
        CandidateOccupation candidateOccupation = candidateJobExperience.getCandidateOccupation();

        // Check if the candidate occupation needs to be updated
        if (request.getCandidateOccupationId() != null) {
            if (candidateOccupation == null || !candidateOccupation.getId().equals(request.getCandidateOccupationId())) {
                candidateOccupation = getCandidateOccupation(request.getCandidateOccupationId());
            }
        }

        // Update the database record
        candidateJobExperience.setCountry(country);
        candidateJobExperience.setCompanyName(request.getCompanyName());
        candidateJobExperience.setRole(request.getRole());
        candidateJobExperience.setStartDate(request.getStartDate());
        candidateJobExperience.setEndDate(request.getEndDate());
        candidateJobExperience.setFullTime(request.getFullTime());
        candidateJobExperience.setPaid(request.getPaid());
        candidateJobExperience.setDescription(request.getDescription());
        candidateJobExperience.setCandidateOccupation(candidateOccupation);
        candidateJobExperienceRepository.save(candidateJobExperience);

        // Save the candidateOccupation
        return candidateJobExperience;
    }

    @Override
    public void deleteCandidateJobExperience(Long id) {
        User user = userContext.getLoggedInUser();

        CandidateJobExperience candidateJobExperience = candidateJobExperienceRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateJobExperience.class, id));

        Candidate candidate;

        // If request is coming from admin portal
        if (user.getRole().equals(Role.admin)) {
            candidate = candidateRepository.findById(candidateJobExperience.getCandidate().getId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateJobExperience.getCandidate().getId()));
        } else {
            candidate = userContext.getLoggedInCandidate();
            // Check that the user is deleting their own attachment
            if (!candidate.getId().equals(candidateJobExperience.getCandidate().getId())) {
                throw new InvalidCredentialsException("You do not have permission to perform that action");
            }
        }

        candidateJobExperienceRepository.delete(candidateJobExperience);

        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);
    }

    // Load the country from the database - throw an exception if not found
    private Country getCountry(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new NoSuchObjectException(Country.class, countryId));
    }

    // Load the candidate occupation from the database - throw an exception if not found
    private CandidateOccupation getCandidateOccupation(Long candidateOccupationId) {
        return candidateOccupationRepository.findById(candidateOccupationId)
                .orElseThrow(() -> new NoSuchObjectException(CandidateOccupation.class, candidateOccupationId));
    }
}
