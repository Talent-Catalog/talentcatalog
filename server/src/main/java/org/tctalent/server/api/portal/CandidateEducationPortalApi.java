/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tctalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tctalent.server.service.db.CandidateEducationService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/candidate-education")
public class CandidateEducationPortalApi {

    private final CandidateEducationService candidateEducationService;

    @Autowired
    public CandidateEducationPortalApi(CandidateEducationService candidateEducationService) {
        this.candidateEducationService = candidateEducationService;
    }

    @PostMapping()
    public Map<String, Object> createCandidateEducation(@Valid @RequestBody CreateCandidateEducationRequest request) {
        CandidateEducation candidateEducation = candidateEducationService.createCandidateEducation(request);
        return candidateEducationDto().build(candidateEducation);
    }

    @PostMapping("update")
    public Map<String, Object> updateCandidateEducation(@Valid @RequestBody UpdateCandidateEducationRequest request) {
        CandidateEducation candidateEducation = this.candidateEducationService.updateCandidateEducation(request);
        return candidateEducationDto().build(candidateEducation);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteCandidateEducation(@PathVariable("id") Long id) {
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
                .add("incomplete")
                .add("yearCompleted")
                ;
    }

    private DtoBuilder majorDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
