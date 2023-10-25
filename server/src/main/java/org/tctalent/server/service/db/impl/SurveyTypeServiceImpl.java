/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.repository.db.SurveyTypeRepository;
import org.tctalent.server.repository.db.SurveyTypeSpecification;
import org.tctalent.server.request.survey.SearchSurveyTypeRequest;
import org.tctalent.server.service.db.SurveyTypeService;
import org.tctalent.server.service.db.TranslationService;

import java.util.List;

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
    public List<SurveyType> listActiveSurveyTypes() {
        List<SurveyType> surveyTypes = surveyTypeRepository.findByStatus(Status.active);
        translationService.translate(surveyTypes, "survey_type");
        return surveyTypes;
    }

    @Override
    public Page<SurveyType> searchActiveSurveyTypes(SearchSurveyTypeRequest request) {
        request.setStatus(Status.active);
        Page<SurveyType> surveyTypes = surveyTypeRepository.findAll(
                SurveyTypeSpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + surveyTypes.getTotalElements() + " survey types in search");
        if (!StringUtils.isBlank(request.getLanguage())){
            translationService.translate(surveyTypes.getContent(), "survey_type", request.getLanguage());
        }
        return surveyTypes;
    }

    @Override
    public List<SurveyType> listSurveyTypes() {
        List<SurveyType> surveyTypes = surveyTypeRepository.findAll();
        translationService.translate(surveyTypes, "survey_type");
        return surveyTypes;
    }



}
