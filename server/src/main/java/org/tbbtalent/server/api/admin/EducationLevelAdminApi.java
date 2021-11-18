/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.api.admin;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.EducationLevel;
import org.tbbtalent.server.model.db.SystemLanguage;
import org.tbbtalent.server.request.education.level.CreateEducationLevelRequest;
import org.tbbtalent.server.request.education.level.SearchEducationLevelRequest;
import org.tbbtalent.server.request.education.level.UpdateEducationLevelRequest;
import org.tbbtalent.server.service.db.EducationLevelService;
import org.tbbtalent.server.service.db.LanguageService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/education-level")
public class EducationLevelAdminApi {

    private final EducationLevelService educationLevelService;
    private final LanguageService languageService;
    private final DtoBuilder systemLanguageDtoBuilder = new SystemLanguageDtoBuilder();

    @Autowired
    public EducationLevelAdminApi(EducationLevelService educationLevelService,
        LanguageService languageService) {
        this.educationLevelService = educationLevelService;
        this.languageService = languageService;
    }

    @PostMapping("system/{langCode}")
    public Map<String, Object> addSystemLanguageTranslations(
        @PathVariable("langCode") String langCode, @RequestParam("file") MultipartFile file)
        throws EntityExistsException, IOException, NoSuchObjectException {
        SystemLanguage systemLanguage =
            this.languageService.addSystemLanguageTranslations(
                langCode, "education_level", file.getInputStream());

        return systemLanguageDtoBuilder.build(systemLanguage);
    }

    @GetMapping()
    public List<Map<String, Object>> listAllLanguages() {
        List<EducationLevel> educationLevels = educationLevelService.listEducationLevels();
        return educationLevelDto().buildList(educationLevels);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchEducationLevelRequest request) {
        Page<EducationLevel> languages = this.educationLevelService.searchEducationLevels(request);
        return educationLevelDto().buildPage(languages);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        EducationLevel educationLevel = this.educationLevelService.getEducationLevel(id);
        return educationLevelDto().build(educationLevel);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateEducationLevelRequest request) throws EntityExistsException {
        EducationLevel educationLevel = this.educationLevelService.createEducationLevel(request);
        return educationLevelDto().build(educationLevel);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateEducationLevelRequest request) throws EntityExistsException  {

        EducationLevel educationLevel = this.educationLevelService.updateEducationLevel(id, request);
        return educationLevelDto().build(educationLevel);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.educationLevelService.deleteEducationLevel(id);
    }

    private DtoBuilder educationLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                .add("level")
                ;
    }

}
