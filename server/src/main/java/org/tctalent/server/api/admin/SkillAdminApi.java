/*
 * Copyright (c) 2025 Talent Catalog.
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.service.api.ExtractSkillsRequest;
import org.tctalent.server.service.api.SkillName;
import org.tctalent.server.service.db.SkillsService;

/**
 * API for accessing skills.
 *
 * @author John Cameron
 */
@RestController
@RequestMapping("/api/public/skill")
@RequiredArgsConstructor
public class SkillAdminApi {
    private final SkillsService skillsService;

    @GetMapping("names")
    public Page<SkillName> getSkillNames(
        @RequestParam(value = "page", defaultValue="0") int page,
        @RequestParam(value = "size", defaultValue="100") int size,
        @RequestParam(value = "lang", defaultValue="en") String lang
    ) {

        PageRequest request = PageRequest.of(page, size);
        return skillsService.getSkillNames(request, lang);
    }

    @PostMapping("extract_skills")
    public List<SkillName> extractSkillNames(@RequestBody ExtractSkillsRequest request) {
        return skillsService.extractSkillNames(request.getText(), request.getLang());
    }
}
