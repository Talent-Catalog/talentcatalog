package org.tbbtalent.server.request.candidate.survey;

import javax.validation.constraints.NotNull;

public class CreateCandidateSurveyRequest {

    @NotNull
    private Long surveyTypeId;

    private String comment;

    public CreateCandidateSurveyRequest() {
    }

    public CreateCandidateSurveyRequest(@NotNull Long surveyTypeId, String comment) {
        this.surveyTypeId = surveyTypeId;
        this.comment = comment;
    }

    public Long getSurveyTypeId() { return surveyTypeId; }

    public void setSurveyTypeId(Long surveyTypeId) { this.surveyTypeId = surveyTypeId; }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
