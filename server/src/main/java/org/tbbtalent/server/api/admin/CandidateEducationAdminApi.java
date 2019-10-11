package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tbbtalent.server.service.CandidateEducationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-education")
public class CandidateEducationAdminApi {

    private final CandidateEducationService candidateEducationService;

    @Autowired
    public CandidateEducationAdminApi(CandidateEducationService candidateEducationService) {
        this.candidateEducationService = candidateEducationService;
    }


    @GetMapping("{id}/list")
    public List<Map<String, Object>> get(@PathVariable("id") long id) {
        List<CandidateEducation> candidateEducations = this.candidateEducationService.list(id);
        return candidateEducationDto().buildList(candidateEducations);
    }

    @PostMapping("{id}")
    public Map<String, Object> create(@PathVariable("id") long candidateId,
                                      @RequestBody CreateCandidateEducationRequest request) throws UsernameTakenException {
        CandidateEducation candidateEducation = this.candidateEducationService.createCandidateEducationAdmin(candidateId, request);
        return candidateEducationDto().build(candidateEducation);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateEducationRequest request) {
        CandidateEducation candidateEducation = this.candidateEducationService.updateCandidateEducationAdmin(id, request);
        return candidateEducationDto().build(candidateEducation);
    }


    private DtoBuilder candidateEducationDto() {
        return new DtoBuilder()
                .add("id")
                .add("educationType")
                .add("country", countryDto())
                .add("lengthOfCourseYears")
                .add("institution")
                .add("courseName")
                .add("dateCompleted")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }


}
