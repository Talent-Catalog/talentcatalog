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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tctalent.server.request.candidate.exam.SearchCandidateExamRequest;
import org.tctalent.server.request.candidate.exam.UpdateCandidateExamRequest;
import org.tctalent.server.service.db.CandidateExamService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.util.dto.DtoBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-exam")
@RequiredArgsConstructor
public class CandidateExamAdminApi implements IJoinedTableApi<SearchCandidateExamRequest,
                                                              CreateCandidateExamRequest,
    UpdateCandidateExamRequest> {

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
     * Retrieves all candidate exam records associated with the given candidate ID.
     * @param parentId ID of the candidate whose exam records are to be listed.
     * @return A list of candidate exam records associated with the specified candidate ID.
     */
    @Override
    public @NotNull List<Map<String, Object>> list(long parentId) {
        List<CandidateExam> candidateExams = candidateExamService.list(parentId);
        return candidateExamDto().buildList(candidateExams);
    }

    /**
     * Updates the candidate exam record with the specified ID using the data in the request.
     * @param id ID of the candidate exam record to be updated.
     * @param request Request containing the updated details for the candidate exam.
     *                Fields that are not specified in the request (i.e., are null)
     *                will not be changed. Therefore, fields cannot be set to null.
     * @return The updated candidate exam record.
     * @throws EntityExistsException if the updated exam record would clash with an
     * existing record (e.g., with the same name).
     * @throws InvalidRequestException if the request is not authorized to update this candidate exam record.
     * @throws NoSuchObjectException if no candidate exam record exists with the specified ID.
     */
    @Override
    public @NotNull Map<String, Object> update(long id, UpdateCandidateExamRequest request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {

        CandidateExam candidateExam = candidateExamService.updateCandidateExam(request);
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
                .add("year")
                .add("score")
                .add("notes")
                ;
    }

}
