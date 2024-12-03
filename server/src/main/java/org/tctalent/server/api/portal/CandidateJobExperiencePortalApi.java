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

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tctalent.server.request.work.experience.UpdateJobExperienceRequest;
import org.tctalent.server.service.db.CandidateJobExperienceService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/job-experience")
public class CandidateJobExperiencePortalApi {
    private final OccupationService occupationService;
    private final CandidateJobExperienceService candidateJobExperienceService;

    @Autowired
    public CandidateJobExperiencePortalApi(OccupationService occupationService,
        CandidateJobExperienceService candidateJobExperienceService) {
        this.occupationService = occupationService;
        this.candidateJobExperienceService = candidateJobExperienceService;
    }

    @PostMapping()
    public Map<String, Object> createJobExperience(@Valid @RequestBody CreateJobExperienceRequest request) {
        CandidateJobExperience candidateJobExperience = this.candidateJobExperienceService.createCandidateJobExperience(request);
        return jobExperienceDto().build(candidateJobExperience);
    }

    @PostMapping("update")
    public Map<String, Object> updateJobExperience(@Valid @RequestBody UpdateJobExperienceRequest request) {
        CandidateJobExperience candidateJobExperience = this.candidateJobExperienceService.updateCandidateJobExperience(request);
        return jobExperienceDto().build(candidateJobExperience);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteJobExperience(@PathVariable("id") Long id) {
        candidateJobExperienceService.deleteCandidateJobExperience(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder jobExperienceDto() {
        return new DtoBuilder()
            .add("id")
            .add("country", countryDto())
            .add("candidateOccupation", candidateOccupationDto())
            .add("companyName")
            .add("role")
            .add("startDate")
            .add("endDate")
            .add("fullTime")
            .add("paid")
            .add("description")
            ;
    }

    private DtoBuilder candidateOccupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("occupation", occupationService.selectBuilder())
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }

}
