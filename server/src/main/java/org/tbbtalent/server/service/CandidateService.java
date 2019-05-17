package org.tbbtalent.server.service;

import org.tbbtalent.server.model.Candidate;

import java.util.List;

public interface CandidateService {

    List<Candidate> searchCandidates();
}
