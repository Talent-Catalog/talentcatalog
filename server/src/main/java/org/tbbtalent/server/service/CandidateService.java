package org.tbbtalent.server.service;

import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.CreateCandidateRequest;
import org.tbbtalent.server.request.UpdateCandidateRequest;

import java.util.List;

public interface CandidateService {

    List<Candidate> searchCandidates();

    Candidate getCandidate(long id);

    Candidate createCandidate(CreateCandidateRequest request);

    Candidate updateCandidate(long id, UpdateCandidateRequest request);
}
