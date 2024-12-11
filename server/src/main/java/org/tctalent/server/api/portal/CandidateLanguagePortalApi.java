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

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tctalent.server.request.candidate.language.UpdateCandidateLanguagesRequest;
import org.tctalent.server.service.db.CandidateLanguageService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/candidate-language")
public class CandidateLanguagePortalApi {

    private final CandidateLanguageService candidateLanguageService;

    @Autowired
    public CandidateLanguagePortalApi(CandidateLanguageService candidateLanguageService) {
        this.candidateLanguageService = candidateLanguageService;
    }

    @PostMapping()
    public Map<String, Object> createCandidateLanguage(@Valid @RequestBody CreateCandidateLanguageRequest request) {
        CandidateLanguage candidateLanguage = candidateLanguageService.createCandidateLanguage(request);
        return candidateLanguageDto().build(candidateLanguage);
    }

    @PostMapping("update")
    public List<Map<String, Object>> updateCandidateLanguage(@Valid @RequestBody UpdateCandidateLanguagesRequest request) {
        List<CandidateLanguage> candidateLanguage = candidateLanguageService.updateCandidateLanguages(request);
        return candidateLanguageDto().buildList(candidateLanguage);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteCandidateLanguage(@PathVariable("id") Long id) {
        candidateLanguageService.deleteCandidateLanguage(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder candidateLanguageDto() {
        return new DtoBuilder()
                .add("id")
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
                .add("level")
                ;
    }

}
