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
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateCitizenship;
import org.tctalent.server.request.candidate.citizenship.CreateCandidateCitizenshipRequest;
import org.tctalent.server.service.db.CandidateCitizenshipService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate-citizenship")
@RequiredArgsConstructor
public class CandidateCitizenshipAdminApi implements IJoinedTableApi<CreateCandidateCitizenshipRequest,
        CreateCandidateCitizenshipRequest, CreateCandidateCitizenshipRequest> {

    private final CountryService countryService;
    private final CandidateCitizenshipService candidateCitizenshipService;

    /**
     * Creates a new candidate citizenship record from the data in the given
     * request.
     * @param candidateId ID of candidate
     * @param request Request containing citizenship details
     * @return Created record - including database id of citizenship record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no Nationality with the id given in the request
     */
    @Override
    public @NotNull Map<String, Object> create(long candidateId, @Valid CreateCandidateCitizenshipRequest request)
            throws NoSuchObjectException {
        CandidateCitizenship candidateCitizenship = candidateCitizenshipService.createCitizenship(candidateId, request);
        return candidateCitizenshipDto().build(candidateCitizenship);
    }

    /**
     * Delete the candidate citizenship with the given id.
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    @Override
    public boolean delete(long id) throws EntityReferencedException, InvalidRequestException {
        return candidateCitizenshipService.deleteCitizenship(id);
    }

    private DtoBuilder candidateCitizenshipDto() {
        return new DtoBuilder()
                .add("id")
                .add("nationality", countryService.selectBuilder())
                .add("hasPassport")
                .add("notes")
                ;
    }
}
