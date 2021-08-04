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

package org.tbbtalent.server.service.db;

import org.springframework.lang.NonNull;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateDependant;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.dependant.CreateCandidateDependantRequest;

public interface CandidateDependantService {

    /**
     * Creates a new candidate dependant record from the data in the given
     * request.
     * @param candidateId ID of candidate
     * @param request Request containing dependant details
     * @return Created record - including database id of dependant record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no Nationality with the id given in the request
     */
    CandidateDependant createDependant(
            long candidateId, CreateCandidateDependantRequest request)
            throws NoSuchObjectException;

    /**
     * Delete the candidate dependant with the given id.
     * @param dependantId ID of record to be deleted
     * @return Candidate owner of the dependant object, need it to save numberDependants on candidate object.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    Candidate deleteDependant(long dependantId)
            throws EntityReferencedException, InvalidRequestException;

    /**
     * Updates the candidate dependant intake data associated with the given
     * nationality and given candidate.
     * @param candidate Candidate
     * @param data Partially populated CandidateIntakeData record. Null data
     *             fields are ignored. Only non null fields are updated.
     * @throws NoSuchObjectException if the there is no Nationality with the
     * given id or no CandidateDependant record with the id given in the data
     */
    void updateIntakeData(@NonNull Candidate candidate, CandidateIntakeDataUpdate data)
            throws NoSuchObjectException;
}
