package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateOccupation;
import org.tbbtalent.server.request.candidate.occupation.CreateCandidateOccupationRequest;

import java.util.List;

public interface CandidateOccupationService {

    CandidateOccupation createCandidateOccupation(CreateCandidateOccupationRequest request);

    void deleteCandidateOccupation(Long id);

    List<CandidateOccupation> listMyOccupations();
}
