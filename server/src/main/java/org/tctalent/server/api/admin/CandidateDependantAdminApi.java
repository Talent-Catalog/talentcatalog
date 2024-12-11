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

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDependant;
import org.tctalent.server.request.candidate.dependant.CreateCandidateDependantRequest;
import org.tctalent.server.service.db.CandidateDependantService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate-dependant")
@RequiredArgsConstructor
public class CandidateDependantAdminApi
        implements IJoinedTableApi<CreateCandidateDependantRequest,
        CreateCandidateDependantRequest,CreateCandidateDependantRequest> {

    private final CandidateDependantService candidateDependantService;
    private final CandidateService candidateService;

    /**
     * Creates a new candidate dependant record from the data in the given
     * request.
     * @param candidateId ID of candidate
     * @param request Request containing dependant details
     * @return Created record - including database id of dependant record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no Nationality with the id given in the request
     */
    @Override
    public @NotNull Map<String, Object> create(
            long candidateId, @Valid CreateCandidateDependantRequest request)
            throws NoSuchObjectException {
        CandidateDependant candidateDependant = candidateDependantService.createDependant(candidateId, request);
        // Need to save the Candidate as we set the number of dependants value on the candidate object when creating dependant.
        candidateService.save(candidateDependant.getCandidate(), true);
        return candidateDependantDto().build(candidateDependant);
    }

    /**
     * Delete the candidate dependant with the given id.
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    @Override
    public boolean delete(long id)
            throws EntityReferencedException, InvalidRequestException {
        Candidate ownerOfDependant = candidateDependantService.deleteDependant(id);
        // Need to save the Candidate as we set the number of dependants value on the candidate object when deleting dependant.
        candidateService.save(ownerOfDependant, true);
        return true;
    }

    @Override
    public @NotNull List<Map<String, Object>> list(long candidateId) {
        List<CandidateDependant> candidateDependants = candidateDependantService.list(candidateId);
        return candidateDependantDto().buildList(candidateDependants);
    }

    private DtoBuilder candidateDependantDto() {
        return new DtoBuilder()
                .add("id")
                .add("relation")
                .add("relationOther")
                .add("dob")
                .add("healthConcern")
                .add("healthNotes")
                .add("name")
                .add("registered")
                .add("registeredNumber")
                .add("registeredNotes")
                ;
    }


}
