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

import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
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
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.request.education.level.CreateEducationLevelRequest;
import org.tctalent.server.request.education.level.SearchEducationLevelRequest;
import org.tctalent.server.request.education.level.UpdateEducationLevelRequest;
import org.tctalent.server.service.db.EducationLevelService;
import org.tctalent.server.service.db.LanguageService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
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
        try (InputStream translations = file.getInputStream()) {
            SystemLanguage systemLanguage =
                languageService.addSystemLanguageTranslations(
                    langCode, "education_level", translations);
            return systemLanguageDtoBuilder.build(systemLanguage);
        }
    }

    @GetMapping
    public List<Map<String, Object>> listAllLanguages() {
        List<EducationLevel> educationLevels = educationLevelService.listEducationLevels();
        return educationLevelDto().buildList(educationLevels);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchEducationLevelRequest request) {
        Page<EducationLevel> languages = educationLevelService.searchEducationLevels(request);
        return educationLevelDto().buildPage(languages);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        EducationLevel educationLevel = educationLevelService.getEducationLevel(id);
        return educationLevelDto().build(educationLevel);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateEducationLevelRequest request) throws EntityExistsException {
        EducationLevel educationLevel = educationLevelService.createEducationLevel(request);
        return educationLevelDto().build(educationLevel);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateEducationLevelRequest request) throws EntityExistsException  {

        EducationLevel educationLevel = educationLevelService.updateEducationLevel(id, request);
        return educationLevelDto().build(educationLevel);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return educationLevelService.deleteEducationLevel(id);
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
