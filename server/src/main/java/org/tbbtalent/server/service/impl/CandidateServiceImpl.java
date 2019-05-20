package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.service.CandidateService;

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
}
