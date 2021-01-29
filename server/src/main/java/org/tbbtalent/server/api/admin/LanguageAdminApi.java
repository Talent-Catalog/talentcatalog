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
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.Language;
import org.tbbtalent.server.model.db.SystemLanguage;
import org.tbbtalent.server.request.language.CreateLanguageRequest;
import org.tbbtalent.server.request.language.SearchLanguageRequest;
import org.tbbtalent.server.request.language.UpdateLanguageRequest;
import org.tbbtalent.server.service.db.LanguageService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/language")
public class LanguageAdminApi {

    private final LanguageService languageService;

    @Autowired
    public LanguageAdminApi(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllLanguages() {
        List<Language> languages = languageService.listLanguages();
        return languageDto().buildList(languages);
    }

    @GetMapping(value = "system")
    public List<Map<String, Object>> getSystemLanguages() {
        List<SystemLanguage> languages = languageService.listSystemLanguages();
        return systemLanguageDto().buildList(languages);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchLanguageRequest request) {
        Page<Language> languages = this.languageService.searchLanguages(request);
        return languageDto().buildPage(languages);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        Language language = this.languageService.getLanguage(id);
        return languageDto().build(language);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateLanguageRequest request) throws EntityExistsException {
        Language language = this.languageService.createLanguage(request);
        return languageDto().build(language);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateLanguageRequest request) throws EntityExistsException  {

        Language language = this.languageService.updateLanguage(id, request);
        return languageDto().build(language);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.languageService.deleteLanguage(id);
    }

    private DtoBuilder languageDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

    private DtoBuilder systemLanguageDto() {
        return new DtoBuilder()
                .add("id")
                .add("language")
                .add("label")
                ;
    }


}
