package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.CreateCandidateRequest;
import org.tbbtalent.server.request.SearchCandidateRequest;
import org.tbbtalent.server.request.UpdateCandidateRequest;
import org.tbbtalent.server.service.CandidateService;

@RestController()
@RequestMapping("/api/admin/candidate")
public class CandidateAdminApi {

    private final CandidateService candidateService;

    @Autowired
    public CandidateAdminApi(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping("search")
    public Page<Candidate> search(@RequestBody SearchCandidateRequest request) {
        return this.candidateService.searchCandidates(request);
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

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) {
        return this.candidateService.deleteCandidate(id);
    }
}
