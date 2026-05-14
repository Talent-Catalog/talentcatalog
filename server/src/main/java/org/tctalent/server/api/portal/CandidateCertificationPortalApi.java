/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tctalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;
import org.tctalent.server.service.db.CandidateCertificationService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/candidate-certification")
public class CandidateCertificationPortalApi {

    private final CandidateCertificationService candidateCertificationService;

    @Autowired
    public CandidateCertificationPortalApi(CandidateCertificationService candidateCertificationService) {
        this.candidateCertificationService = candidateCertificationService;
    }

    @PostMapping()
    public Map<String, Object> createCandidateCertification(@Valid @RequestBody CreateCandidateCertificationRequest request) {
        CandidateCertification candidateCertification = candidateCertificationService.createCandidateCertification(request);
        return candidateCertificationDto().build(candidateCertification);
    }

    @PutMapping()
    public Map<String, Object> update(@RequestBody UpdateCandidateCertificationRequest request) {
        CandidateCertification candidateCertification = this.candidateCertificationService.updateCandidateCertification(request);
        return candidateCertificationDto().build(candidateCertification);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteCandidateCertification(@PathVariable("id") Long id) {
        candidateCertificationService.deleteCandidateCertification(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder candidateCertificationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("institution")
                .add("dateCompleted")
                ;
    }

}
