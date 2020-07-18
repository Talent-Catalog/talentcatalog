package org.tbbtalent.server.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.SurveyType;
import org.tbbtalent.server.repository.db.SurveyTypeRepository;
import org.tbbtalent.server.service.SurveyTypeService;
import org.tbbtalent.server.service.TranslationService;

@Service
public class SurveyTypeServiceImpl implements SurveyTypeService {

    private static final Logger log = LoggerFactory.getLogger(SurveyTypeServiceImpl.class);

    private final SurveyTypeRepository surveyTypeRepository;
    private final TranslationService translationService;

    @Autowired
    public SurveyTypeServiceImpl(SurveyTypeRepository surveyTypeRepository,
                                  TranslationService translationService) {
        this.surveyTypeRepository = surveyTypeRepository;
        this.translationService = translationService;
    }

    @Override
    public List<SurveyType> listSurveyTypes() {
        List<SurveyType> surveyTypes = surveyTypeRepository.findByStatus(Status.active);
        translationService.translate(surveyTypes, "survey_type");
        return surveyTypes;
    }



}
