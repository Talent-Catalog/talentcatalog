package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.candidate.*;
import org.tbbtalent.server.service.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.server.ExportException;
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
        Map<String, Object> map = candidateBaseDto().buildPage(candidates);
        return map;
    }

    @PostMapping("find")
    public Map<String, Object> findByCandidateNumber(@RequestBody CandidateQuickSearchRequest request) {
        Page<Candidate> candidates = this.candidateService.searchCandidates(request);
        Map<String, Object> map = candidateBaseDto().buildPage(candidates);
        return map;
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

    @PutMapping("{id}/status")
    public Map<String, Object> update(@PathVariable("id") long id,
                            @RequestBody UpdateCandidateStatusRequest request) {
        Candidate candidate = this.candidateService.updateCandidateStatus(id, request);
        return candidateDto().build(candidate);
    }

    @PutMapping("{id}")
    public Map<String, Object> updateContactDetails(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateRequest request) {
        Candidate candidate = this.candidateService.updateCandidate(id, request);
        return candidateDto().build(candidate);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) {
        return this.candidateService.deleteCandidate(id);
    }


    @PostMapping(value = "export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void export(@RequestBody SearchCandidateRequest request,
                       HttpServletResponse response) throws IOException, ExportException {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "candidates.csv\"");
        candidateService.exportToCsv(request, response.getWriter());
    }

    private DtoBuilder candidateBaseDto() {
        return new DtoBuilder()
                .add("id")
                .add("status")
                .add("candidateNumber")
                .add("gender")
                .add("dob")
                .add("phone")
                .add("whatsapp")
                .add("city")
                .add("address1")
                .add("yearOfArrival")
                .add("additionalInfo")
                .add("candidateMessage")
                .add("country", countryDto())
                .add("nationality", nationalityDto())
                .add("user", userDto())
                .add("candidateShortlistItems", shortlistDto())
                ;
    }

    private DtoBuilder candidateDto() {
        return candidateBaseDto()
                .add("maxEducationLevel", educationLevelDto())
                .add("user", userDto())
                .add("candidateShortlistItems", shortlistDto())
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

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder nationalityDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder shortlistDto() {
        return new DtoBuilder()
                .add("id")
                .add("shortlistStatus")
                .add("savedSearch", savedSearchDto())
                ;
    }

    private DtoBuilder savedSearchDto() {
        return new DtoBuilder()
                .add("id")
                ;
    }

    private DtoBuilder educationMajor() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder educationLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
