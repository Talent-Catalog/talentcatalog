package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateSurvey;
import org.tbbtalent.server.request.candidate.survey.CreateCandidateSurveyRequest;

public interface CandidateSurveyService {

    CandidateSurvey createCandidateSurvey(CreateCandidateSurveyRequest request);

//    CandidateSurvey updateCandidateSurvey(long id, UpdateCandidateSurveyRequest request);

}
