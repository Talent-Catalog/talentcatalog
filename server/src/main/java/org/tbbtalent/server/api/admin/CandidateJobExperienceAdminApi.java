package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.model.CandidateJobExperience;
import org.tbbtalent.server.request.work.experience.CreateJobExperienceRequest;
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
//
//    @PostMapping("search")
//    public Map<String, Object> search(@RequestBody SearchCandidateJobExperienceRequest request) {
//        Page<CandidateJobExperience> candidateJobExperiences = this.candidateJobExperienceService.searchCandidateJobExperience(request);
//        return candidateJobExperienceDto().buildPage(candidateJobExperiences);
//    }

    @PostMapping("{id}")
    public Map<String, Object> create(@Valid @PathVariable("id") long id, @RequestBody CreateJobExperienceRequest request) throws EntityExistsException {
        CandidateJobExperience candidateJobExperience = this.candidateJobExperienceService.createCandidateJobExperience(id, request);
        return candidateJobExperienceDto().build(candidateJobExperience);
    }

//    @PutMapping("{id}")
//    public Map<String, Object> update(@PathVariable("id") long id,
//                                      @RequestBody UpdateCandidateJobExperienceRequest request) {
//        CandidateJobExperience candidateJobExperience = this.candidateJobExperienceService.updateJobExperience(id, request);
//        return candidateJobExperienceDto().build(candidateJobExperience);
//    }


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
