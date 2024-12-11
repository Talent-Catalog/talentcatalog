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

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tctalent.server.request.work.experience.SearchJobExperienceRequest;
import org.tctalent.server.request.work.experience.UpdateJobExperienceRequest;
import org.tctalent.server.service.db.CandidateJobExperienceService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate-job-experience")
@RequiredArgsConstructor
public class CandidateJobExperienceAdminApi {

    private final CandidateJobExperienceService candidateJobExperienceService;
    private final CountryService countryService;
    private final OccupationService occupationService;

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchJobExperienceRequest request) {
        Page<CandidateJobExperience> candidateJobExperiences =
                candidateJobExperienceService.searchCandidateJobExperience(request);
        return candidateJobExperienceDto().buildPage(candidateJobExperiences);
    }

    @PostMapping("{id}")
    public Map<String, Object> create(@Valid @PathVariable("id") Long candidateId,
                                      @RequestBody CreateJobExperienceRequest request) throws EntityExistsException {
        request.setCandidateId(candidateId);
        CandidateJobExperience candidateJobExperience =
                candidateJobExperienceService.createCandidateJobExperience(request);
        return candidateJobExperienceDto().build(candidateJobExperience);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") Long id,
                                      @RequestBody UpdateJobExperienceRequest request) {
        CandidateJobExperience candidateJobExperience =
                candidateJobExperienceService.updateCandidateJobExperience(id, request);
        return candidateJobExperienceDto().build(candidateJobExperience);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
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
                .add("country", countryService.selectBuilder())
                .add("candidateOccupation", candidateOccupationDto())
                ;
    }

    private DtoBuilder candidateOccupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("occupation", occupationService.selectBuilder())
                .add("yearsExperience")
                ;
    }
}
