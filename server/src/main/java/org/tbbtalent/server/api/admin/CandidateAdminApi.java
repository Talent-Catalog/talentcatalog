package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.CreateCandidateRequest;
import org.tbbtalent.server.request.UpdateCandidateRequest;
import org.tbbtalent.server.service.CandidateService;

import java.util.List;

@RestController()
@RequestMapping("/api/admin/candidate")
public class CandidateAdminApi {

    private final CandidateService candidateService;

    @Autowired
    public CandidateAdminApi(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping()
    public List<Candidate> search() {
        return this.candidateService.searchCandidates();
    }

    @GetMapping("{id}")
    public Candidate get(@PathVariable("id") long id) {
        return this.candidateService.getCandidate(id);
    }

    @PostMapping
    public Candidate create(@RequestBody CreateCandidateRequest request) {
        return this.candidateService.createCandidate(request);
    }

    @PutMapping("{id}")
    public Candidate update(@PathVariable("id") long id,
                            @RequestBody UpdateCandidateRequest request) {
        return this.candidateService.updateCandidate(id, request);
    }
}
