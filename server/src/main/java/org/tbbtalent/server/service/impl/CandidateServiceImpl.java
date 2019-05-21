package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.request.CreateCandidateRequest;
import org.tbbtalent.server.service.CandidateService;

import java.util.ArrayList;
import java.util.List;

@Service
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;

    @Autowired
    public CandidateServiceImpl(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Override
    public List<Candidate> searchCandidates() {
        return this.candidateRepository.findAll();
    }

    @Override
    public Candidate createCandidates(CreateCandidateRequest request) {
        String candidateNumber = String.format("CN%04d", id);
        Candidate candidate = new Candidate(
                candidateNumber,
                request.getFirstName(),
                request.getLastName());
        this.candidates.add(candidate);
        return candidate;
    }
}
