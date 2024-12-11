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

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tctalent.server.exception.UsernameTakenException;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tctalent.server.request.candidate.language.UpdateCandidateLanguageRequest;
import org.tctalent.server.service.db.CandidateLanguageService;
import org.tctalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/candidate-language")
@RequiredArgsConstructor
public class CandidateLanguageAdminApi {

    private final CandidateLanguageService candidateLanguageService;

    @GetMapping("{id}/list")
    public List<Map<String, Object>> get(@PathVariable("id") long id) {
        List<CandidateLanguage> candidateLanguages = candidateLanguageService.list(id);
        return candidateLanguageDto().buildList(candidateLanguages);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody CreateCandidateLanguageRequest request) throws UsernameTakenException {
        CandidateLanguage candidateLanguage = candidateLanguageService.createCandidateLanguage(request);
        return candidateLanguageDto().build(candidateLanguage);
    }

    @PutMapping
    public Map<String, Object> update(@RequestBody UpdateCandidateLanguageRequest request) {
        CandidateLanguage candidateLanguage = candidateLanguageService.updateCandidateLanguage(request);
        return candidateLanguageDto().build(candidateLanguage);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        candidateLanguageService.deleteCandidateLanguage(id);
        return ResponseEntity.ok().build();
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
