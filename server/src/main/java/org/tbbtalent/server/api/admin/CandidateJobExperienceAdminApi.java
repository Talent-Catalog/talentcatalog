package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.model.CandidateJobExperience;
import org.tbbtalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.SearchJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.UpdateJobExperienceRequest;
import org.tbbtalent.server.service.CandidateJobExperienceService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-job-experience")
public class CandidateJobExperienceAdminApi {

    private final CandidateJobExperienceService candidateJobExperienceService;

    @Autowired
    public CandidateJobExperienceAdminApi(CandidateJobExperienceService candidateJobExperienceService) {
        this.candidateJobExperienceService = candidateJobExperienceService;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchJobExperienceRequest request) {
        Page<CandidateJobExperience> candidateJobExperiences = this.candidateJobExperienceService.searchCandidateJobExperience(request);
        return candidateJobExperienceDto().buildPage(candidateJobExperiences);
    }

    @PostMapping("{id}")
    public Map<String, Object> create(@Valid @PathVariable("id") Long candidateId,
                                      @RequestBody CreateJobExperienceRequest request) throws EntityExistsException {
        request.setCandidateId(candidateId);
        CandidateJobExperience candidateJobExperience = this.candidateJobExperienceService.createCandidateJobExperience(request);
        return candidateJobExperienceDto().build(candidateJobExperience);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") Long id,
                                      @RequestBody UpdateJobExperienceRequest request) {
        CandidateJobExperience candidateJobExperience = this.candidateJobExperienceService.updateCandidateJobExperience(id, request);
        return candidateJobExperienceDto().build(candidateJobExperience);
    }

    @DeleteMapping("{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {
        candidateJobExperienceService.deleteCandidateJobExperience(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder candidateJobExperienceDto() {
        return new DtoBuilder()
                .add("id")
                .add("companyName")
                .add("role")
                .add("startDate")
                .add("endDate")
                .add("fullTime")
                .add("paid")
                .add("description")
                .add("country", countryDto())
                .add("candidateOccupation", candidateOccupationDto())
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

    private DtoBuilder candidateOccupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("occupation", occupationDto())
                .add("yearsExperience")
                ;
    }

    private DtoBuilder occupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
