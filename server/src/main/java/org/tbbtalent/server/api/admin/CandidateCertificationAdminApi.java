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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.CandidateCertification;
import org.tbbtalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tbbtalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;
import org.tbbtalent.server.service.db.CandidateCertificationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-certification")
public class CandidateCertificationAdminApi {

    private final CandidateCertificationService candidateCertificationService;

    @Autowired
    public CandidateCertificationAdminApi(CandidateCertificationService candidateCertificationService) {
        this.candidateCertificationService = candidateCertificationService;
    }


    @GetMapping("{id}/list")
    public List<Map<String, Object>> get(@PathVariable("id") long id) {
        List<CandidateCertification> candidateCertifications = this.candidateCertificationService.list(id);
        return candidateCertificationDto().buildList(candidateCertifications);
    }

    @PostMapping()
    public Map<String, Object> create(@RequestBody CreateCandidateCertificationRequest request) throws UsernameTakenException {
        CandidateCertification candidateCertification = this.candidateCertificationService.createCandidateCertification(request);
        return candidateCertificationDto().build(candidateCertification);
    }

    @PutMapping()
    public Map<String, Object> update(@RequestBody UpdateCandidateCertificationRequest request) {
        CandidateCertification candidateCertification = this.candidateCertificationService.updateCandidateCertification(request);
        return candidateCertificationDto().build(candidateCertification);
    }

    @DeleteMapping("{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {
        this.candidateCertificationService.deleteCandidateCertification(id);
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
