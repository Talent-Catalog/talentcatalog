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

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tbbtalent.server.service.db.CandidateEducationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-education")
@RequiredArgsConstructor
public class CandidateEducationAdminApi {

    private final CandidateEducationService candidateEducationService;

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
                .add("country", countryDto())
                .add("educationMajor", majorDto())
                .add("lengthOfCourseYears")
                .add("institution")
                .add("courseName")
                .add("yearCompleted")
                .add("incomplete")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
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
