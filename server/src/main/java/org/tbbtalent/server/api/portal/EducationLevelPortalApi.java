package org.tbbtalent.server.api.portal;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.EducationLevel;
import org.tbbtalent.server.service.EducationLevelService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/education-level")
public class EducationLevelPortalApi {

    private final EducationLevelService educationLevelService;

    @Autowired
    public EducationLevelPortalApi(EducationLevelService educationLevelService) {
        this.educationLevelService = educationLevelService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllEducationLevels() {
        List<EducationLevel> educationLevels = educationLevelService.listEducationLevels();
        return educationLevelDto().buildList(educationLevels);
    }

    private DtoBuilder educationLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("level")
                .add("educationType")
                ;
    }

}
