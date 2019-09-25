package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateOccupation;
import org.tbbtalent.server.model.Industry;
import org.tbbtalent.server.model.Occupation;
import org.tbbtalent.server.repository.IndustryRepository;
import org.tbbtalent.server.repository.CandidateOccupationRepository;
import org.tbbtalent.server.repository.OccupationRepository;
import org.tbbtalent.server.request.profession.CreateCandidateOccupationRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateOccupationService;

@Service
public class CandidateOccupationServiceImpl implements CandidateOccupationService {

    private final CandidateOccupationRepository candidateOccupationRepository;
    private final OccupationRepository occupationRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateOccupationServiceImpl(CandidateOccupationRepository candidateOccupationRepository,
                                          OccupationRepository occupationRepository,
                                          UserContext userContext) {
        this.candidateOccupationRepository = candidateOccupationRepository;
        this.occupationRepository = occupationRepository;
        this.userContext = userContext;
    }


    @Override
    public CandidateOccupation createCandidateOccupation(CreateCandidateOccupationRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Load the industry from the database - throw an exception if not found
        Occupation occupation = occupationRepository.findById(request.getOccupationId())
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, request.getOccupationId()));

        // Create a new profession object to insert into the database
        CandidateOccupation candidateOccupation = new CandidateOccupation();
        candidateOccupation.setCandidate(candidate);
        candidateOccupation.setOccupation(occupation);
        candidateOccupation.setYearsExperience(request.getYearsExperience());

        // Save the profession
        return candidateOccupationRepository.save(candidateOccupation);
    }

    @Override
    public void deleteCandidateOccupation(Long id) {
        Candidate candidate = userContext.getLoggedInCandidate();
        CandidateOccupation candidateOccupation = candidateOccupationRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateOccupation.class, id));

        // Check that the user is deleting their own profession
        if (!candidate.getId().equals(candidateOccupation.getCandidate().getId())) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }

        candidateOccupationRepository.delete(candidateOccupation);
    }
}
