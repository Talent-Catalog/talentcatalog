package org.tbbtalent.server.api.portal;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tbbtalent.server.service.db.CandidateEducationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/candidate-education")
public class CandidateEducationPortalApi {

    private final CandidateEducationService candidateEducationService;

    @Autowired
    public CandidateEducationPortalApi(CandidateEducationService candidateEducationService) {
        this.candidateEducationService = candidateEducationService;
    }

    @PostMapping()
    public Map<String, Object> createCandidateEducation(@Valid @RequestBody CreateCandidateEducationRequest request) {
        CandidateEducation candidateEducation = candidateEducationService.createCandidateEducation(request);
        return candidateEducationDto().build(candidateEducation);
    }

    @PostMapping("update")
    public Map<String, Object> updateCandidateEducation(@Valid @RequestBody UpdateCandidateEducationRequest request) {
        CandidateEducation candidateEducation = this.candidateEducationService.updateCandidateEducation(request);
        return candidateEducationDto().build(candidateEducation);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteCandidateEducation(@PathVariable("id") Long id) {
        candidateEducationService.deleteCandidateEducation(id);
        return ResponseEntity.ok().build();
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
                .add("incomplete")
                .add("yearCompleted")
                ;
    }

    private DtoBuilder majorDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
