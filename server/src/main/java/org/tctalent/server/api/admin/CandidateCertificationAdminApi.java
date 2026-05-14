/*
 * Copyright (c) 2024 Talent Catalog.
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
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tctalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;
import org.tctalent.server.service.db.CandidateCertificationService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate-certification")
@RequiredArgsConstructor
public class CandidateCertificationAdminApi {

    private final CandidateCertificationService candidateCertificationService;

    @GetMapping("{id}/list")
    public List<Map<String, Object>> get(@PathVariable("id") long id) {
        List<CandidateCertification> candidateCertifications = candidateCertificationService.list(id);
        return candidateCertificationDto().buildList(candidateCertifications);
    }

    @PostMapping()
    public Map<String, Object> create(@RequestBody CreateCandidateCertificationRequest request) throws UsernameTakenException {
        CandidateCertification candidateCertification = candidateCertificationService.createCandidateCertification(request);
        return candidateCertificationDto().build(candidateCertification);
    }

    @PutMapping()
    public Map<String, Object> update(@RequestBody UpdateCandidateCertificationRequest request) {
        CandidateCertification candidateCertification = candidateCertificationService.updateCandidateCertification(request);
        return candidateCertificationDto().build(candidateCertification);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
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
