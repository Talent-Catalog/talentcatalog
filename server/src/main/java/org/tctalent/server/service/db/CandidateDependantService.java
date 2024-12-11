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

package org.tctalent.server.service.db;

import java.util.List;
import org.springframework.lang.NonNull;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDependant;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tctalent.server.request.candidate.dependant.CreateCandidateDependantRequest;

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
     * List of all dependants associated to candidate with given id.
     * @param candidateId ID of candidate whose dependants we want
     * @return list of candidate dependants
     */
    List<CandidateDependant> list(long candidateId);

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

    /**
     * Retrieves the dependant object for the given id.
     * @param id id of requested dependant
     * @return CandidateDependant
     * @throws NoSuchObjectException if no dependant with that id exists
     */
    CandidateDependant getDependant(long id)
        throws NoSuchObjectException;
}
