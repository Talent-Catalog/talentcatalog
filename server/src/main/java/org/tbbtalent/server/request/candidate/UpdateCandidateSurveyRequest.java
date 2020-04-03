package org.tbbtalent.server.request.candidate;

public class UpdateCandidateSurveyRequest {

    private Long surveyTypeId;
    private String surveyComment;

    public Long getSurveyTypeId() { return surveyTypeId; }

    public void setSurveyTypeId(Long surveyTypeId) { this.surveyTypeId = surveyTypeId; }

    public String getSurveyComment() { return surveyComment; }

    public void setSurveyComment(String surveyComment) { this.surveyComment = surveyComment; }
}
