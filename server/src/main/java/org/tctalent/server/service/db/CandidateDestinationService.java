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
import org.springframework.lang.Nullable;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDestination;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tctalent.server.request.candidate.destination.CreateCandidateDestinationRequest;
import org.tctalent.server.request.candidate.destination.UpdateCandidateDestinationRequest;

public interface CandidateDestinationService {

    /**
     * Creates a new candidate destination record from the data in the given
     * request.
     * @param candidateId ID of candidate
     * @param request Request containing destination details
     * @return Created record - including database id of destination record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no country with the id given in the request
     */
    CandidateDestination createDestination(
            long candidateId, CreateCandidateDestinationRequest request)
            throws NoSuchObjectException;

    /**
     * Update an existing candidate destination record with the data in the given
     * request.
     * @param id ID of candidate destination to be updated
     * @param request Request containing destination details
     * @return Created record - including database id of destination record
     * @throws NoSuchObjectException if the there is no destination record with that id
     */
    CandidateDestination updateDestination(long id,
            UpdateCandidateDestinationRequest request)
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

    /**
     * List of all destinations associated to candidate with given id.
     * @param candidateId ID of candidate whose destinations we want
     * @return list of candidate destinations
     */
    List<CandidateDestination> list(long candidateId);
}
