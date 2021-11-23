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
import org.tbbtalent.server.model.db.EducationMajor;
import org.tbbtalent.server.model.db.SystemLanguage;
import org.tbbtalent.server.request.education.major.CreateEducationMajorRequest;
import org.tbbtalent.server.request.education.major.SearchEducationMajorRequest;
import org.tbbtalent.server.request.education.major.UpdateEducationMajorRequest;
import org.tbbtalent.server.service.db.EducationMajorService;
import org.tbbtalent.server.service.db.LanguageService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/education-major")
public class EducationMajorAdminApi {

    private final EducationMajorService educationMajorService;
    private final LanguageService languageService;
    private final DtoBuilder systemLanguageDtoBuilder = new SystemLanguageDtoBuilder();

    @Autowired
    public EducationMajorAdminApi(EducationMajorService educationMajorService,
        LanguageService languageService) {
        this.educationMajorService = educationMajorService;
        this.languageService = languageService;
    }

    @PostMapping("system/{langCode}")
    public Map<String, Object> addSystemLanguageTranslations(
        @PathVariable("langCode") String langCode, @RequestParam("file") MultipartFile file)
        throws EntityExistsException, IOException, NoSuchObjectException {
        SystemLanguage systemLanguage =
            this.languageService.addSystemLanguageTranslations(
                langCode, "education_major", file.getInputStream());

        return systemLanguageDtoBuilder.build(systemLanguage);
    }

    @GetMapping()
    public List<Map<String, Object>> listAllEducationMajors() {
        List<EducationMajor> educationMajors = educationMajorService.listActiveEducationMajors();
        return educationMajorDto().buildList(educationMajors);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchEducationMajorRequest request) {
        Page<EducationMajor> nationalities = this.educationMajorService.searchEducationMajors(request);
        return educationMajorDto().buildPage(nationalities);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        EducationMajor educationMajor = this.educationMajorService.getEducationMajor(id);
        return educationMajorDto().build(educationMajor);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateEducationMajorRequest request) throws EntityExistsException {
        EducationMajor educationMajor = this.educationMajorService.createEducationMajor(request);
        return educationMajorDto().build(educationMajor);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateEducationMajorRequest request) throws EntityExistsException  {

        EducationMajor educationMajor = this.educationMajorService.updateEducationMajor(id, request);
        return educationMajorDto().build(educationMajor);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.educationMajorService.deleteEducationMajor(id);
    }


    private DtoBuilder educationMajorDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

}
