/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateDestination;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.destination.CreateCandidateDestinationRequest;

public interface CandidateDestinationService {

    /**
     * Creates a new candidate destination record from the data in the given
     * request.
     * @param candidateId ID of candidate
     * @param request Request containing destination details
     * @return Created record - including database id of destination record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no Nationality with the id given in the request
     */
    CandidateDestination createDestination(
            long candidateId, CreateCandidateDestinationRequest request)
            throws NoSuchObjectException;

    /**
     * Delete the candidate destination with the given id.
     * @param destinationId ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    boolean deleteDestination(long destinationId)
            throws EntityReferencedException, InvalidRequestException;

    /**
     * Updates the candidate destination intake data associated with the given
     * nationality and given candidate.
     * @param countryId ID of nationality - If null this method does nothing
     * @param candidate Candidate
     * @param data Partially populated CandidateIntakeData record. Null data
     *             fields are ignored. Only non null fields are updated.
     * @throws NoSuchObjectException if the there is no Nationality with the
     * given id or no CandidateDestination record with the id given in the data
     */
    void updateIntakeData(
            @Nullable Long countryId, @NonNull Candidate candidate,
            CandidateIntakeDataUpdate data) throws NoSuchObjectException;
}
