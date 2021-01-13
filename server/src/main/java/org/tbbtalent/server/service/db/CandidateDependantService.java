/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
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
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    boolean deleteDependant(long dependantId)
            throws EntityReferencedException, InvalidRequestException;

    /**
     * Updates the candidate dependant intake data associated with the given
     * nationality and given candidate.
     * @param countryId ID of nationality - If null this method does nothing
     * @param candidate Candidate
     * @param data Partially populated CandidateIntakeData record. Null data
     *             fields are ignored. Only non null fields are updated.
     * @throws NoSuchObjectException if the there is no Nationality with the
     * given id or no CandidateDependant record with the id given in the data
     */
    void updateIntakeData(@NonNull Candidate candidate, CandidateIntakeDataUpdate data)
            throws NoSuchObjectException;
}
