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

package org.tctalent.server.api.portal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.api.admin.IJoinedTableApi;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateDestination;
import org.tctalent.server.request.candidate.destination.CreateCandidateDestinationRequest;
import org.tctalent.server.request.candidate.destination.UpdateCandidateDestinationRequest;
import org.tctalent.server.service.db.CandidateDestinationService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/candidate-destination")
@RequiredArgsConstructor
public class CandidateDestinationPortalApi implements IJoinedTableApi<CreateCandidateDestinationRequest,
        CreateCandidateDestinationRequest, UpdateCandidateDestinationRequest> {

    private final CandidateDestinationService candidateDestinationService;
    private final CountryService countryService;

    /**
     * Creates a new candidate destination record from the data in the given
     * request.
     * @param candidateId ID of candidate
     * @param request Request containing destination details
     * @return Created record - including database id of destination record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no Nationality with the id given in the request
     */
    @Override
    public @NotNull Map<String, Object> create(long candidateId, @Valid CreateCandidateDestinationRequest request)
            throws NoSuchObjectException {
        CandidateDestination candidateDestination = candidateDestinationService.createDestination(candidateId, request);
        return candidateDestinationDto().build(candidateDestination);
    }

    @Override
    @PutMapping("{id}")
    public @NotNull Map<String, Object> update(@PathVariable long id, UpdateCandidateDestinationRequest request)
            throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        CandidateDestination candidateDestination = candidateDestinationService.updateDestination(id, request);
        return candidateDestinationDto().build(candidateDestination);
    }

    private DtoBuilder candidateDestinationDto() {
        return new DtoBuilder()
                .add("id")
                .add("country", countryService.selectBuilder())
                .add("interest")
                .add("notes")
                ;
    }
}
