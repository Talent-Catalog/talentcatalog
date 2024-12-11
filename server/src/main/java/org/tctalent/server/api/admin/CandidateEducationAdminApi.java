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

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.UsernameTakenException;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tctalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tctalent.server.service.db.CandidateEducationService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate-education")
@RequiredArgsConstructor
public class CandidateEducationAdminApi {

    private final CandidateEducationService candidateEducationService;
    private final CountryService countryService;

    @GetMapping("{id}/list")
    public List<Map<String, Object>> get(@PathVariable("id") long id) {
        List<CandidateEducation> candidateEducations = candidateEducationService.list(id);
        return candidateEducationDto().buildList(candidateEducations);
    }

    @PostMapping()
    public Map<String, Object> create(@RequestBody CreateCandidateEducationRequest request) throws UsernameTakenException {
        CandidateEducation candidateEducation = candidateEducationService.createCandidateEducation(request);
        return candidateEducationDto().build(candidateEducation);
    }

    @PutMapping()
    public Map<String, Object> update(@RequestBody UpdateCandidateEducationRequest request) {
        CandidateEducation candidateEducation = candidateEducationService.updateCandidateEducation(request);
        return candidateEducationDto().build(candidateEducation);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        candidateEducationService.deleteCandidateEducation(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder candidateEducationDto() {
        return new DtoBuilder()
                .add("id")
                .add("educationType")
                .add("country", countryService.selectBuilder())
                .add("educationMajor", majorDto())
                .add("lengthOfCourseYears")
                .add("institution")
                .add("courseName")
                .add("yearCompleted")
                .add("incomplete")
                ;
    }

    private DtoBuilder majorDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }
}
