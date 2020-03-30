package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateSurvey;
import org.tbbtalent.server.model.SurveyType;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.CandidateSurveyRepository;
import org.tbbtalent.server.repository.SurveyTypeRepository;
import org.tbbtalent.server.request.candidate.survey.CreateCandidateSurveyRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateSurveyService;

@Service
public class CandidateSurveyServiceImpl implements CandidateSurveyService {

    private final CandidateSurveyRepository candidateSurveyRepository;
    private final SurveyTypeRepository surveyTypeRepository;
    private final CandidateRepository candidateRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateSurveyServiceImpl(CandidateSurveyRepository candidateSurveyRepository,
                                             SurveyTypeRepository surveyTypeRepository,
                                             CandidateRepository candidateRepository,
                                             UserContext userContext) {
        this.candidateSurveyRepository = candidateSurveyRepository;
        this.surveyTypeRepository = surveyTypeRepository;
        this.candidateRepository = candidateRepository;
        this.userContext = userContext;
    }


    @Override
    public CandidateSurvey createCandidateSurvey(CreateCandidateSurveyRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        SurveyType surveyType = surveyTypeRepository.findById(request.getSurveyTypeId())
                .orElseThrow(() -> new NoSuchObjectException(SurveyType.class, request.getSurveyTypeId()));

        // Create a new candidateOccupation object to insert into the database
        CandidateSurvey candidateSurvey = new CandidateSurvey();
        candidateSurvey.setCandidate(candidate);
        candidateSurvey.setSurveyType(surveyType);
        candidateSurvey.setComment(request.getComment());

        // Save the candidateSurvey
        return candidateSurveyRepository.save(candidateSurvey);

    }

//    @Override
//    public CandidateSurvey updateCandidateSurvey(long id, UpdateCandidateSurveyRequest request) {
//
//        CandidateSurvey candidateSurvey = this.candidateSurveyRepository.findById(id)
//                .orElseThrow(() -> new NoSuchObjectException(CandidateSurvey.class, id));
//
//        // Update education object to insert into the database
//        candidateSurvey.setComment(request.getComment());
//        candidateSurvey.setDateCompleted(request.getDateCompleted());
//
//        // Save the candidateOccupation
//        return candidateSurveyRepository.save(candidateSurvey);
//
//    }
    
}
