package org.tbbtalent.server.service;

import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.CreateCandidateRequest;

import java.util.List;

public interface CandidateService {

    List<Candidate> searchCandidates();

    Candidate createCandidates(CreateCandidateRequest request);
}
