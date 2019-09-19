package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.WorkExperience;
import org.tbbtalent.server.request.work.experience.CreateWorkExperienceRequest;
import org.tbbtalent.server.service.WorkExperienceService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/work-experience")
public class WorkExperiencePortalApi {

    private final WorkExperienceService workExperienceService;

    @Autowired
    public WorkExperiencePortalApi(WorkExperienceService workExperienceService) {
        this.workExperienceService = workExperienceService;
    }

    @PostMapping()
    public Map<String, Object> createWorkExperience(@Valid @RequestBody CreateWorkExperienceRequest request) {
        WorkExperience workExperience = workExperienceService.createWorkExperience(request);
        return workExperienceDto().build(workExperience);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteWorkExperience(@PathVariable("id") Long id) {
        workExperienceService.deleteWorkExperience(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder workExperienceDto() {
        return new DtoBuilder()
            .add("id")
            .add("country", countryDto())
            .add("companyName")
            .add("role")
            .add("startDate")
            .add("endDate")
            .add("fullTime")
            .add("paid")
            .add("description")
            ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }

}
