package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.CreateCandidateRequest;
import org.tbbtalent.server.service.CandidateService;

import java.util.List;

@RestController("/api/admin/candidate")
public class CandidateAdminApi {

    private final CandidateService candidateService;

    @Autowired
    public CandidateAdminApi(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping
    public List<Candidate> search() {
        return this.candidateService.searchCandidates();
    }

    @PostMapping
    public Candidate create(@RequestBody CreateCandidateRequest request) {
        return this.candidateService.createCandidates(request);
    }
}
