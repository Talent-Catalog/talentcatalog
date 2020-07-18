package org.tbbtalent.server.api.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.SurveyType;
import org.tbbtalent.server.service.SurveyTypeService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/survey-type")
public class SurveyTypeAdminApi {

    private final SurveyTypeService surveyTypeService;

    @Autowired
    public SurveyTypeAdminApi(SurveyTypeService surveyTypeService) {
        this.surveyTypeService = surveyTypeService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllSurveyTypes() {
        List<SurveyType> surveyTypes = surveyTypeService.listSurveyTypes();
        return surveyTypeDto().buildList(surveyTypes);
    }

    private DtoBuilder surveyTypeDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
