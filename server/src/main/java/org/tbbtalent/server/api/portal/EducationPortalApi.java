package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.Education;
import org.tbbtalent.server.request.education.CreateEducationRequest;
import org.tbbtalent.server.service.EducationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/education")
public class EducationPortalApi {

    private final EducationService educationService;

    @Autowired
    public EducationPortalApi(EducationService educationService) {
        this.educationService = educationService;
    }

    @PostMapping()
    public Map<String, Object> createEducation(@Valid @RequestBody CreateEducationRequest request) {
        Education education = educationService.createEducation(request);
        return educationDto().build(education);
    }


    private DtoBuilder educationDto() {
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
