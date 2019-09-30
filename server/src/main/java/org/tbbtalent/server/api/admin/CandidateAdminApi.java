package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.candidate.CreateCandidateRequest;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateRequest;
import org.tbbtalent.server.service.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate")
public class CandidateAdminApi {

    private final CandidateService candidateService;

    @Autowired
    public CandidateAdminApi(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchCandidateRequest request) {
        Page<Candidate> candidates = this.candidateService.searchCandidates(request);
        return candidateDto().buildPage(candidates);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        Candidate candidate = this.candidateService.getCandidate(id);
        return candidateDto().build(candidate);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody CreateCandidateRequest request) throws UsernameTakenException {
        Candidate candidate = this.candidateService.createCandidate(request);
        return candidateDto().build(candidate);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                            @RequestBody UpdateCandidateRequest request) {
        Candidate candidate = this.candidateService.updateCandidate(id, request);
        return candidateDto().build(candidate);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) {
        return this.candidateService.deleteCandidate(id);
    }

    private DtoBuilder candidateDto() {
        return new DtoBuilder()
                .add("id")
                .add("candidateNumber")
                .add("user", userDto())
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                .add("email")
                ;
    }

}
