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
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.service.db.LanguageLevelService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/language-level")
public class LanguageLevelPortalApi {

    private final LanguageLevelService languageLevelService;

    @Autowired
    public LanguageLevelPortalApi(LanguageLevelService languageLevelService) {
        this.languageLevelService = languageLevelService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllLanguageLevels() {
        List<LanguageLevel> languageLevels = languageLevelService.listLanguageLevels();
        return languageLevelDto().buildList(languageLevels);
    }

    private DtoBuilder languageLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("level")
                ;
    }

}
