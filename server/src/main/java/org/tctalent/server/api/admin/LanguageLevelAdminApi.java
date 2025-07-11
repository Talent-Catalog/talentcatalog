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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
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
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.request.language.level.CreateLanguageLevelRequest;
import org.tctalent.server.request.language.level.SearchLanguageLevelRequest;
import org.tctalent.server.request.language.level.UpdateLanguageLevelRequest;
import org.tctalent.server.service.db.LanguageLevelService;
import org.tctalent.server.service.db.LanguageService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/language-level")
public class LanguageLevelAdminApi {

    private final LanguageService languageService;
    private final LanguageLevelService languageLevelService;
    private final DtoBuilder systemLanguageDtoBuilder = new SystemLanguageDtoBuilder();

    @Autowired
    public LanguageLevelAdminApi(LanguageService languageService,
        LanguageLevelService languageLevelService) {
        this.languageService = languageService;
        this.languageLevelService = languageLevelService;
    }

    @PostMapping("system/{langCode}")
    public Map<String, Object> addSystemLanguageTranslations(
        @PathVariable("langCode") String langCode, @RequestParam("file") MultipartFile file)
        throws EntityExistsException, IOException, NoSuchObjectException {
        SystemLanguage systemLanguage =
            this.languageService.addSystemLanguageTranslations(
                langCode, "language_level", file.getInputStream());

        return systemLanguageDtoBuilder.build(systemLanguage);
    }

    @GetMapping()
    public List<Map<String, Object>> listAllLanguages() {
        List<LanguageLevel> languageLevels = languageLevelService.listLanguageLevels();
        return languageLevelDto().buildList(languageLevels);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchLanguageLevelRequest request) {
        Page<LanguageLevel> languages = this.languageLevelService.searchLanguageLevels(request);
        return languageLevelDto().buildPage(languages);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        LanguageLevel languageLevel = this.languageLevelService.getLanguageLevel(id);
        return languageLevelDto().build(languageLevel);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateLanguageLevelRequest request) throws EntityExistsException {
        LanguageLevel languageLevel = this.languageLevelService.createLanguageLevel(request);
        return languageLevelDto().build(languageLevel);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateLanguageLevelRequest request) throws EntityExistsException  {

        LanguageLevel languageLevel = this.languageLevelService.updateLanguageLevel(id, request);
        return languageLevelDto().build(languageLevel);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.languageLevelService.deleteLanguageLevel(id);
    }

    private DtoBuilder languageLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("level")
                .add("cefrLevel")
                .add("status")
                ;
    }

}
