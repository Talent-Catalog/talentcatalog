package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.CandidateSurvey;
import org.tbbtalent.server.request.candidate.survey.CreateCandidateSurveyRequest;
import org.tbbtalent.server.service.CandidateSurveyService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/candidate-survey")
public class CandidateSurveyPortalApi {

    private final CandidateSurveyService candidateSurveyService;

    @Autowired
    public CandidateSurveyPortalApi(CandidateSurveyService candidateSurveyService) {
        this.candidateSurveyService = candidateSurveyService;
    }

    @PostMapping()
    public Map<String, Object> createCandidateSurvey(@Valid @RequestBody CreateCandidateSurveyRequest request) {
        CandidateSurvey candidateSurvey = candidateSurveyService.createCandidateSurvey(request);
        return candidateSurveyDto().build(candidateSurvey);
    }

//    @PostMapping("update")
//    public Map<String, Object> updateCandidateSurvey(@Valid @RequestBody UpdateCandidateSurveyRequest request) {
//        CandidateSurvey candidateSurvey = this.candidateSurveyService.updateCandidateSurvey(request);
//        return candidateSurveyDto().build(candidateSurvey);
//    }
//
//    @DeleteMapping("{id}")
//    public ResponseEntity deleteCandidateSurvey(@PathVariable("id") Long id) {
//        candidateSurveyService.deleteCandidateSurvey(id);
//        return ResponseEntity.ok().build();
//    }

    private DtoBuilder candidateSurveyDto() {
        return new DtoBuilder()
                .add("id")
                .add("surveyType", surveyTypeDto())
                .add("comment")
                ;
    }

    private DtoBuilder surveyTypeDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
