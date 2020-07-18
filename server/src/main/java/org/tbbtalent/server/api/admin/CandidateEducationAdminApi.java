package org.tbbtalent.server.api.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tbbtalent.server.service.CandidateEducationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

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
        CandidateEducation candidateEducation = this.candidateEducationService.createCandidateEducation(candidateId, request);
        return candidateEducationDto().build(candidateEducation);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateEducationRequest request) {
        request.setId(id);
        CandidateEducation candidateEducation = this.candidateEducationService.updateCandidateEducation(id, request);
        return candidateEducationDto().build(candidateEducation);
    }


    private DtoBuilder candidateEducationDto() {
        return new DtoBuilder()
                .add("id")
                .add("educationType")
                .add("country", countryDto())
                .add("educationMajor", majorDto())
                .add("lengthOfCourseYears")
                .add("institution")
                .add("courseName")
                .add("yearCompleted")
                .add("incomplete")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

    private DtoBuilder majorDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }


}
