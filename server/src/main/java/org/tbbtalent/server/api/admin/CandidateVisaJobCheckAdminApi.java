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

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.CandidateVisaJobCheck;
import org.tbbtalent.server.request.candidate.visa.job.CreateCandidateVisaJobCheckRequest;
import org.tbbtalent.server.service.db.CandidateVisaJobCheckService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-visa-job")
public class CandidateVisaJobCheckAdminApi
        implements IJoinedTableApi<CreateCandidateVisaJobCheckRequest,
        CreateCandidateVisaJobCheckRequest, CreateCandidateVisaJobCheckRequest> {
    private final CandidateVisaJobCheckService candidateVisaJobCheckService;

    public CandidateVisaJobCheckAdminApi(
            CandidateVisaJobCheckService candidateVisaJobCheckService) {
        this.candidateVisaJobCheckService = candidateVisaJobCheckService;
    }

    /**
     * Creates a new candidate visa check record from the data in the given 
     * request. 
     * @param visaId ID of visa
     * @param request Request containing visa check details
     * @return Created record - including database id of visa check record
     * @throws NoSuchObjectException if the there is no Candidate record with 
     * that candidateId or no country with the id given in the request  
     */
    @Override
    public @NotNull Map<String, Object> create(
            long visaId, @Valid CreateCandidateVisaJobCheckRequest request)
            throws NoSuchObjectException {
        CandidateVisaJobCheck candidateVisaCheck = this.candidateVisaJobCheckService.createVisaJobCheck(visaId, request);
        return candidateVisaDto().build(candidateVisaCheck);
    }

    /**
     * Delete the candidate visa check with the given id.  
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because 
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    @Override
    public boolean delete(long id) 
            throws EntityReferencedException, InvalidRequestException {
        return candidateVisaJobCheckService.deleteVisaJobCheck(id);
    }
    
    private DtoBuilder candidateVisaDto() {
        return new DtoBuilder()
                .add("id")
                .add("country", countryDto())
                .add("eligibility")
                .add("assessmentNotes")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }
    
}
