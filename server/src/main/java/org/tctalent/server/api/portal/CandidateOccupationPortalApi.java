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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.request.candidate.occupation.CreateCandidateOccupationRequest;
import org.tctalent.server.request.candidate.occupation.UpdateCandidateOccupationsRequest;
import org.tctalent.server.service.db.CandidateOccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/candidate-occupation")
public class CandidateOccupationPortalApi {

    private final CandidateOccupationService candidateOccupationService;

    @Autowired
    public CandidateOccupationPortalApi(CandidateOccupationService candidateOccupationService) {
        this.candidateOccupationService = candidateOccupationService;
    }

    @GetMapping("list")
    public List<Map<String, Object>> listMyOccupations() {
        List<CandidateOccupation> candidateOccupations = candidateOccupationService.listMyOccupations();
        return candidateOccupationDto().buildList(candidateOccupations);
    }

    @PostMapping()
    public Map<String, Object> createCandidateOccupation(@Valid @RequestBody CreateCandidateOccupationRequest request) {
        CandidateOccupation candidateOccupation = candidateOccupationService.createCandidateOccupation(request);
        return candidateOccupationDto().build(candidateOccupation);
    }


    @PostMapping("/update")
    public List<Map<String, Object>> createUpdateCandidateOccupation(@Valid @RequestBody UpdateCandidateOccupationsRequest request) {
        List<CandidateOccupation> candidateOccupations = candidateOccupationService.updateCandidateOccupations(request);
        return candidateOccupationDto().buildList(candidateOccupations);
    }


    @DeleteMapping("{id}")
    public ResponseEntity deleteCandidateOccupation(@PathVariable("id") Long id) {
        candidateOccupationService.deleteCandidateOccupation(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder candidateOccupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("occupation", occupationDto())
                .add("yearsExperience")
                .add("migrationOccupation")
                ;
    }

    private DtoBuilder occupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
