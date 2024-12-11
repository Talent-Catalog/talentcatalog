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

package org.tctalent.server.api.portal;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.service.db.EducationMajorService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/education-major")
public class EducationMajorPortalApi {

    private final EducationMajorService educationMajorService;

    @Autowired
    public EducationMajorPortalApi(EducationMajorService educationMajorService) {
        this.educationMajorService = educationMajorService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllEducationMajors() {
        List<EducationMajor> educationMajors = educationMajorService.listActiveEducationMajors();
        return educationMajorDto().buildList(educationMajors);
    }

    private DtoBuilder educationMajorDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
