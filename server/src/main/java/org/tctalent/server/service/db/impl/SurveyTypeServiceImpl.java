/*
 * Copyright (c) 2024 Talent Catalog.
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

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.repository.db.SurveyTypeRepository;
import org.tctalent.server.repository.db.SurveyTypeSpecification;
import org.tctalent.server.request.survey.SearchSurveyTypeRequest;
import org.tctalent.server.service.db.SurveyTypeService;
import org.tctalent.server.service.db.TranslationService;

@Service
@Slf4j
public class SurveyTypeServiceImpl implements SurveyTypeService {

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

        LogBuilder.builder(log)
            .action("SearchActiveSurveyTypes")
            .message("Found " + surveyTypes.getTotalElements() + " survey types in search")
            .logInfo();

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

    @Override
    public SurveyType findByName(String name) {
        return surveyTypeRepository.findByName(name)
            .orElseThrow(() -> new NoSuchObjectException(SurveyType.class, name));
    }
}
