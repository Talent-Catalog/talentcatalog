package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.EducationType;
import org.tbbtalent.server.service.EducationTypeService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/education-type")
public class EducationTypePortalApi {

    private final EducationTypeService educationTypeService;

    @Autowired
    public EducationTypePortalApi(EducationTypeService educationTypeService) {
        this.educationTypeService = educationTypeService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllEducationTypes() {
        List<EducationType> educationTypes = educationTypeService.listEducationTypes();
        return educationTypeDto().buildList(educationTypes);
    }

    private DtoBuilder educationTypeDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
