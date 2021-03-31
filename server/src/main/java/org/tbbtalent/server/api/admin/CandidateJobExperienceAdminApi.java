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
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.model.db.CandidateJobExperience;
import org.tbbtalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.ListJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.SearchJobExperienceRequest;
import org.tbbtalent.server.request.work.experience.UpdateJobExperienceRequest;
import org.tbbtalent.server.service.db.CandidateJobExperienceService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-job-experience")
public class CandidateJobExperienceAdminApi {

    private final CandidateJobExperienceService candidateJobExperienceService;

    @Autowired
    public CandidateJobExperienceAdminApi(CandidateJobExperienceService candidateJobExperienceService) {
        this.candidateJobExperienceService = candidateJobExperienceService;
    }

    @PostMapping("list")
    public List<Map<String, Object>> list(@RequestBody ListJobExperienceRequest request) {
        List<CandidateJobExperience> candidateJobExperiences = this.candidateJobExperienceService.listCandidateJobExperiences(request);
        return candidateJobExperienceDto().buildList(candidateJobExperiences);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchJobExperienceRequest request) {
        Page<CandidateJobExperience> candidateJobExperiences = this.candidateJobExperienceService.searchCandidateJobExperience(request);
        return candidateJobExperienceDto().buildPage(candidateJobExperiences);
    }

    @PostMapping("{id}")
    public Map<String, Object> create(@Valid @PathVariable("id") Long candidateId,
                                      @RequestBody CreateJobExperienceRequest request) throws EntityExistsException {
        request.setCandidateId(candidateId);
        CandidateJobExperience candidateJobExperience = this.candidateJobExperienceService.createCandidateJobExperience(request);
        return candidateJobExperienceDto().build(candidateJobExperience);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") Long id,
                                      @RequestBody UpdateJobExperienceRequest request) {
        CandidateJobExperience candidateJobExperience = this.candidateJobExperienceService.updateCandidateJobExperience(id, request);
        return candidateJobExperienceDto().build(candidateJobExperience);
    }

    @DeleteMapping("{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {
        candidateJobExperienceService.deleteCandidateJobExperience(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder candidateJobExperienceDto() {
        return new DtoBuilder()
                .add("id")
                .add("companyName")
                .add("role")
                .add("startDate")
                .add("endDate")
                .add("fullTime")
                .add("paid")
                .add("description")
                .add("country", countryDto())
                .add("candidateOccupation", candidateOccupationDto())
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

    private DtoBuilder candidateOccupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("occupation", occupationDto())
                .add("yearsExperience")
                ;
    }

    private DtoBuilder occupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
