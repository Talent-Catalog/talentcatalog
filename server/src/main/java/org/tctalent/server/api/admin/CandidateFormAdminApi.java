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

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateForm;
import org.tctalent.server.request.form.SearchCandidateFormRequest;
import org.tctalent.server.request.form.UpdateCandidateFormRequest;
import org.tctalent.server.service.db.CandidateFormService;

//TODO JC Convert to new version of ITableAPI which specifies non map returns
@RestController()
@RequestMapping("/api/admin/candidate-form")
@RequiredArgsConstructor
public class CandidateFormAdminApi {

    private final CandidateFormService candidateFormService;

    @NotNull
    public ResponseEntity<CandidateForm> create(UpdateCandidateFormRequest request) throws EntityExistsException {
        CandidateForm candidateForm = candidateFormService.createCandidateForm(request);
        return ResponseEntity.ok(candidateForm);
    }

    @NotNull
    public ResponseEntity<List<CandidateForm>> search(SearchCandidateFormRequest request) {
        List<CandidateForm> candidateForms = candidateFormService.search(request);
        return ResponseEntity.ok(candidateForms);
    }

    @NotNull
    public ResponseEntity<Page<CandidateForm>> searchPaged(SearchCandidateFormRequest request) {
        Page<CandidateForm> candidateForms = candidateFormService.searchPaged(request);
        return ResponseEntity.ok(candidateForms);
    }

    @NotNull
    public ResponseEntity<CandidateForm> update(long id, UpdateCandidateFormRequest request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        CandidateForm candidateForm = candidateFormService.updateCandidateForm(id, request);
        return ResponseEntity.ok(candidateForm);
    }
}
