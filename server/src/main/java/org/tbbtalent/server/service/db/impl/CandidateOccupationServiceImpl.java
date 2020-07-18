package org.tbbtalent.server.service.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateOccupation;
import org.tbbtalent.server.model.db.Occupation;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateJobExperienceRepository;
import org.tbbtalent.server.repository.db.CandidateOccupationRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.OccupationRepository;
import org.tbbtalent.server.request.candidate.occupation.CreateCandidateOccupationRequest;
import org.tbbtalent.server.request.candidate.occupation.UpdateCandidateOccupationRequest;
import org.tbbtalent.server.request.candidate.occupation.UpdateCandidateOccupationsRequest;
import org.tbbtalent.server.request.candidate.occupation.VerifyCandidateOccupationRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CandidateNoteService;
import org.tbbtalent.server.service.db.CandidateOccupationService;

@Service
public class CandidateOccupationServiceImpl implements CandidateOccupationService {

    private final CandidateOccupationRepository candidateOccupationRepository;
    private final CandidateJobExperienceRepository candidateJobExperienceRepository;
    private final OccupationRepository occupationRepository;
    private final CandidateNoteService candidateNoteService;
    private final CandidateRepository candidateRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateOccupationServiceImpl(CandidateOccupationRepository candidateOccupationRepository,
                                          CandidateJobExperienceRepository candidateJobExperienceRepository,
                                          OccupationRepository occupationRepository,
                                          CandidateRepository candidateRepository,
                                          CandidateNoteService candidateNoteService, UserContext userContext) {
        this.candidateOccupationRepository = candidateOccupationRepository;
        this.candidateJobExperienceRepository = candidateJobExperienceRepository;
        this.candidateRepository = candidateRepository;
        this.occupationRepository = occupationRepository;
        this.candidateNoteService = candidateNoteService;
        this.userContext = userContext;
    }


    @Override
    public CandidateOccupation createCandidateOccupation(CreateCandidateOccupationRequest request) {
        User user = userContext.getLoggedInUser();

        // Load the industry from the database - throw an exception if not found
        Occupation occupation = occupationRepository.findById(request.getOccupationId())
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, request.getOccupationId()));

        // Create a new candidateOccupation object to insert into the database
        CandidateOccupation candidateOccupation = new CandidateOccupation();

        Candidate candidate;
        /* Check if request is coming from admin */
        if (user.getRole().equals(Role.admin)) {
            candidate = candidateRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));
            // Set verified if request coming from admin
            candidateOccupation.setVerified(request.isVerified());

            candidateOccupation.setAuditFields(userContext.getLoggedInUser());
            // removed verification note as verified no longer used
//            candidateNoteService.createCandidateNote(new CreateCandidateNoteRequest(request.getCandidateId(),
//                    occupation.getName() +" verification status set to "+request.isVerified(), request.getComment()));
        } else {
            candidate = userContext.getLoggedInCandidate();
        }

        candidateOccupation.setCandidate(candidate);

        // Check candidate doesn't already have this occupation and isn't the same candidateOccupation
        CandidateOccupation existing = candidateOccupationRepository.findByCandidateIdAAndOccupationId(candidateOccupation.getCandidate().getId(), request.getOccupationId());
        if (existing != null && !existing.getId().equals(candidateOccupation.getId())){
            throw new EntityExistsException("occupation");
        }
        candidateOccupation.setOccupation(occupation);
        candidateOccupation.setYearsExperience(request.getYearsExperience());

        // Save the candidateOccupation
        candidateOccupation = candidateOccupationRepository.save(candidateOccupation);

        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);

        return candidateOccupation;
    }

    @Override
    public void deleteCandidateOccupation(Long id) {
        User user = userContext.getLoggedInUser();

        CandidateOccupation candidateOccupation = candidateOccupationRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateOccupation.class, id));

        Candidate candidate;

        // If request is coming from admin portal
        if (user.getRole().equals(Role.admin)) {
            candidate = candidateRepository.findById(candidateOccupation.getCandidate().getId())
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateOccupation.getCandidate().getId()));
        } else {
            candidate = userContext.getLoggedInCandidate();
            // Check that the user is deleting their own attachment
            if (!candidate.getId().equals(candidateOccupation.getCandidate().getId())) {
                throw new InvalidCredentialsException("You do not have permission to perform that action");
            }
        }

        candidateOccupationRepository.delete(candidateOccupation);

        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);
    }

    @Override
    public List<CandidateOccupation> listMyOccupations() {
        Candidate candidate = userContext.getLoggedInCandidate();
        return candidateOccupationRepository.findByCandidateIdLoadOccupation(candidate.getId());

    }

    @Override
    public List<CandidateOccupation> listCandidateOccupations(Long candidateId) {
        return candidateOccupationRepository.findByCandidateId(candidateId);
    }

    @Override
    public List<Occupation> listVerifiedOccupations() {
        List<Occupation> verifiedOccupations = candidateOccupationRepository.findAllVerifiedOccupations();
        return verifiedOccupations;
    }

    @Override
    public List<Occupation> listOccupations() {
        List<Occupation> occupations = candidateOccupationRepository.findAllOccupations();
        return occupations;
    }

    @Override
    public List<CandidateOccupation> updateCandidateOccupations(UpdateCandidateOccupationsRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        List<CandidateOccupation> updatedOccupations = new ArrayList<>();
        List<Long> updatedOccupationIds = new ArrayList<>();

        List<CandidateOccupation> candidateOccupations = candidateOccupationRepository.findByCandidateId(candidate.getId());
        Map<Long, CandidateOccupation> map = candidateOccupations.stream().collect( Collectors.toMap(CandidateOccupation::getId,
                Function.identity()) );

        for (UpdateCandidateOccupationRequest update : request.getUpdates()) {
            /* Check if occupation has been previously saved */
            CandidateOccupation candidateOccupation = update.getId() != null ? map.get(update.getId()) : null;
            if (candidateOccupation != null){
                /* Check if the occupation has changed */
                if (!update.getOccupationId().equals(candidateOccupation.getOccupation().getId())){
                    Occupation occupation = occupationRepository.findById(update.getOccupationId())
                            .orElseThrow(() -> new NoSuchObjectException(Occupation.class, update.getOccupationId()));
                    candidateOccupation.setOccupation(occupation);
                }
                candidateOccupation.setYearsExperience(update.getYearsExperience());
            } else {
                /* Create a new candidate occupation */
                Occupation occupation = occupationRepository.findById(update.getOccupationId())
                        .orElseThrow(() -> new NoSuchObjectException(Occupation.class, update.getOccupationId()));
                candidateOccupation = new CandidateOccupation(candidate, occupation, update.getYearsExperience());
            }
            updatedOccupations.add(candidateOccupationRepository.save(candidateOccupation));
            updatedOccupationIds.add(candidateOccupation.getId());
        }

        for (Long existingCandidateOccupationId : map.keySet()) {
            /* Check if the candidate occupation has been removed */
            if (!updatedOccupationIds.contains(existingCandidateOccupationId)){
                //The user will have confirmed that they are OK to lose any 
                // associated experiences
                candidateOccupationRepository.deleteById(existingCandidateOccupationId);
            }
        }

        candidate.setAuditFields(candidate.getUser());
        candidateRepository.save(candidate);

        return candidateOccupations;
    }

    @Override
    //@Audit(type = AuditType.CANDIDATE_OCCUPATION, action = AuditAction.VERIFY, extraInfo = "Set verified to {input.verified} with comment: {input.comment}")
    public CandidateOccupation verifyCandidateOccupation(VerifyCandidateOccupationRequest request) {
        CandidateOccupation candidateOccupation = candidateOccupationRepository.findByIdLoadCandidate(request.getId())
                .orElseThrow(() -> new NoSuchObjectException(CandidateOccupation.class, request.getId()));

        // Load the verified occupation from the database - throw an exception if not found
        Occupation verifiedOccupation = occupationRepository.findById(request.getOccupationId())
                .orElseThrow(() -> new NoSuchObjectException(Occupation.class, request.getOccupationId()));

        // Check candidate doesn't already have this occupation and isn't the same candidateOccupation
        CandidateOccupation existing = candidateOccupationRepository.findByCandidateIdAAndOccupationId(candidateOccupation.getCandidate().getId(), request.getOccupationId());
        if (existing != null && !existing.getId().equals(candidateOccupation.getId())){
            throw new EntityExistsException("occupation");
        }

        candidateOccupation.setOccupation(verifiedOccupation);
        candidateOccupation.setYearsExperience(request.getYearsExperience());
        candidateOccupation.setVerified(request.isVerified());

        candidateOccupation.setAuditFields(userContext.getLoggedInUser());
        // removed as no longer use verification
//        candidateNoteService.createCandidateNote(new CreateCandidateNoteRequest(candidateOccupation.getCandidate().getId(),
//                candidateOccupation.getOccupation().getName() +" verification status set to "+request.isVerified(), request.getComment()));

        return candidateOccupationRepository.save(candidateOccupation);

    }
}
