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
import org.tbbtalent.server.model.db.CandidateJobExperience;
import org.tbbtalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.UpdateJobExperienceRequest;
import org.tbbtalent.server.service.db.CandidateJobExperienceService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/job-experience")
public class CandidateJobExperiencePortalApi {

    private final CandidateJobExperienceService candidateJobExperienceService;

    @Autowired
    public CandidateJobExperiencePortalApi(CandidateJobExperienceService candidateJobExperienceService) {
        this.candidateJobExperienceService = candidateJobExperienceService;
    }

    @PostMapping()
    public Map<String, Object> createJobExperience(@Valid @RequestBody CreateJobExperienceRequest request) {
        CandidateJobExperience candidateJobExperience = this.candidateJobExperienceService.createCandidateJobExperience(request);
        return jobExperienceDto().build(candidateJobExperience);
    }

    @PostMapping("update")
    public Map<String, Object> updateJobExperience(@Valid @RequestBody UpdateJobExperienceRequest request) {
        CandidateJobExperience candidateJobExperience = this.candidateJobExperienceService.updateCandidateJobExperience(request);
        return jobExperienceDto().build(candidateJobExperience);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteJobExperience(@PathVariable("id") Long id) {
        candidateJobExperienceService.deleteCandidateJobExperience(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder jobExperienceDto() {
        return new DtoBuilder()
            .add("id")
            .add("country", countryDto())
            .add("candidateOccupation", candidateOccupationDto())
            .add("companyName")
            .add("role")
            .add("startDate")
            .add("endDate")
            .add("fullTime")
            .add("paid")
            .add("description")
            ;
    }

    private DtoBuilder candidateOccupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("occupation", occupationDto())
                ;
    }

    private DtoBuilder occupationDto() {
        return new DtoBuilder()
                .add("id")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }

}
