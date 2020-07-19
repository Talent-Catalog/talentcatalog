/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.tbbtalent.server.model.db.CandidateOccupation;
import org.tbbtalent.server.model.db.Occupation;
import org.tbbtalent.server.request.candidate.occupation.CreateCandidateOccupationRequest;
import org.tbbtalent.server.request.candidate.occupation.UpdateCandidateOccupationsRequest;
import org.tbbtalent.server.request.candidate.occupation.VerifyCandidateOccupationRequest;

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

    CandidateOccupation verifyCandidateOccupation(VerifyCandidateOccupationRequest request);
}
