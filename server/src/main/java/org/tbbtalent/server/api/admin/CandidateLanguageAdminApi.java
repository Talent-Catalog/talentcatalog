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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.CandidateLanguage;
import org.tbbtalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tbbtalent.server.request.candidate.language.UpdateCandidateLanguageRequest;
import org.tbbtalent.server.service.db.CandidateLanguageService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-language")
public class CandidateLanguageAdminApi {

    private final CandidateLanguageService candidateLanguageService;

    @Autowired
    public CandidateLanguageAdminApi(CandidateLanguageService candidateLanguageService) {
        this.candidateLanguageService = candidateLanguageService;
    }


    @GetMapping("{id}/list")
    public List<Map<String, Object>> get(@PathVariable("id") long id) {
        List<CandidateLanguage> candidateLanguages = this.candidateLanguageService.list(id);
        return candidateLanguageDto().buildList(candidateLanguages);
    }

    @PostMapping("{id}")
    public Map<String, Object> create(@PathVariable("id") long candidateId,
                                      @RequestBody CreateCandidateLanguageRequest request) throws UsernameTakenException {
        CandidateLanguage candidateLanguage = this.candidateLanguageService.createCandidateLanguageAdmin(candidateId, request);
        return candidateLanguageDto().build(candidateLanguage);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateLanguageRequest request) {
        CandidateLanguage candidateLanguage = this.candidateLanguageService.updateCandidateLanguage(id, request);
        return candidateLanguageDto().build(candidateLanguage);
    }




    private DtoBuilder candidateLanguageDto() {
        return new DtoBuilder()
                .add("id")
                .add("migrationLanguage")
                .add("language", languageDto())
                .add("writtenLevel", languageLevelDto())
                .add("spokenLevel",languageLevelDto())
                ;
    }

    private DtoBuilder languageDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder languageLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("level")
                ;
    }

}
