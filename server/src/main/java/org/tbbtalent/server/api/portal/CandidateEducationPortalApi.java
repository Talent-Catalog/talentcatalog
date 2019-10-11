package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.service.CandidateEducationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

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
    public Map<String, Object> updateCandidateEducation(@Valid @RequestBody CreateCandidateEducationRequest request) {
        CandidateEducation candidateEducation = this.candidateEducationService.updateCandidateEducation(request);
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
                ;
    }

}
