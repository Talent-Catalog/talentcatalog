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

package org.tctalent.server.api.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.service.db.SurveyTypeService;
import org.tctalent.server.util.dto.DtoBuilder;

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
