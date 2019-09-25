package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateOccupation;
import org.tbbtalent.server.request.profession.CreateCandidateOccupationRequest;

public interface CandidateOccupationService {

    CandidateOccupation createCandidateOccupation(CreateCandidateOccupationRequest request);

    void deleteCandidateOccupation(Long id);
}
