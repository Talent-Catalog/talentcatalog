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

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.CandidateExam;
import org.tbbtalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tbbtalent.server.service.db.CandidateExamService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-exam")
@RequiredArgsConstructor
public class CandidateExamAdminApi implements IJoinedTableApi<CreateCandidateExamRequest,
                                                              CreateCandidateExamRequest,
                                                              CreateCandidateExamRequest> {
    private final CandidateExamService candidateExamService;
    private final CandidateService candidateService;

    /**
     * Creates a new candidate exam record from the data in the given
     * request. 
     * @param candidateId ID of candidate
     * @param request Request containing exam details
     * @return Created record - including database id of exam record
     * @throws NoSuchObjectException if the there is no Candidate record with 
     * that candidateId or no Nationality with the id given in the request  
     */
    @Override
    public @NotNull Map<String, Object> create(long candidateId, @Valid CreateCandidateExamRequest request)
            throws NoSuchObjectException {
        CandidateExam candidateExam = candidateExamService.createExam(candidateId, request);
        return candidateExamDto().build(candidateExam);
    }

    /**
     * Delete the candidate exam with the given id.
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because 
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    @Override
    public boolean delete(long id) throws EntityReferencedException, InvalidRequestException {
        return candidateService.deleteCandidateExam(id);
    }
    
    private DtoBuilder candidateExamDto() {
        return new DtoBuilder()
                .add("id")
                .add("exam")
                .add("otherExam")
                .add("score")
                .add("notes")
                ;
    }

}
