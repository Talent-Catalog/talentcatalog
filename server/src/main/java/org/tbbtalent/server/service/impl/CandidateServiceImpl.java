package org.tbbtalent.server.service.impl;

import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.CreateCandidateRequest;
import org.tbbtalent.server.service.CandidateService;

import java.util.ArrayList;
import java.util.List;

@Service
public class CandidateServiceImpl implements CandidateService {

    private List<Candidate> candidates;
    private long nextId = 1;

    public CandidateServiceImpl() {
        this.candidates = new ArrayList<>();

        this.candidates.add(new Candidate(nextId++, "CN0001", "Test1", "Person1"));
        this.candidates.add(new Candidate(nextId++, "CN0002", "Test2", "Person2"));
        this.candidates.add(new Candidate(nextId++, "CN0003", "Test3", "Person3"));
    }

    @Override
    public List<Candidate> searchCandidates() {
        return this.candidates;
    }

    @Override
    public Candidate createCandidates(CreateCandidateRequest request) {
        long id = this.nextId++;
        String candidateNumber = String.format("CN%04d", id);
        Candidate candidate = new Candidate(
                id, candidateNumber,
                request.getFirstName(),
                request.getLastName());
        this.candidates.add(candidate);
        return candidate;
    }
}
