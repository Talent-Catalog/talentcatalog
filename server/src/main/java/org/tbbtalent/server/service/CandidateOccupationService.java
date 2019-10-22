package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateOccupation;
import org.tbbtalent.server.model.Occupation;
import org.tbbtalent.server.request.candidate.occupation.CreateCandidateOccupationRequest;
import org.tbbtalent.server.request.candidate.occupation.UpdateCandidateOccupationsRequest;
import org.tbbtalent.server.request.candidate.occupation.VerifyCandidateOccupationRequest;

import java.util.List;

public interface CandidateOccupationService {

    CandidateOccupation createCandidateOccupation(CreateCandidateOccupationRequest request);

    void deleteCandidateOccupation(Long id);

    List<CandidateOccupation> listMyOccupations();

    List<CandidateOccupation> listCandidateOccupations(Long candidateId);

    /* Lists only verified occupations */
    List<Occupation> listVerifiedOccupations();

    /* Lists all occupations elected by candidates, regardless of verified flag */
    List<Occupation> listOccupations();

    List<CandidateOccupation> updateCandidateOccupations(UpdateCandidateOccupationsRequest request);

    CandidateOccupation verifyCandidateOccupation(long id, VerifyCandidateOccupationRequest request);
}
