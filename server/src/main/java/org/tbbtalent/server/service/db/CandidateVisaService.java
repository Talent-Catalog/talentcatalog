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
import org.tbbtalent.server.model.db.CandidateVisa;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.visa.CreateCandidateVisaRequest;

public interface CandidateVisaService {

    /**
     * Creates a new candidate visa record from the data in the given 
     * request. 
     * @param candidateId ID of candidate
     * @param request Request containing visa check details
     * @return Created record - including database id of visa check record
     * @throws NoSuchObjectException if the there is no Candidate record with 
     * that candidateId or no country with the id given in the request  
     */
    CandidateVisa createVisa(
            long candidateId, CreateCandidateVisaRequest request)
            throws NoSuchObjectException;

    /**
     * Delete the candidate visa with the given id.  
     * @param visaId ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because 
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    boolean deleteVisa(long visaId)
            throws EntityReferencedException, InvalidRequestException;

    /**
     * Updates the candidate visa intake data associated with the given 
     * country and given candidate.
     * @param countryId ID of country - If null this method does nothing 
     * @param candidate Candidate
     * @param data Partially populated CandidateIntakeData record. Null data
     *             fields are ignored. Only non null fields are updated.
     * @throws NoSuchObjectException if the there is no country with the
     * given id or no CandidateVisa record with the id given in the data  
     */
    void updateIntakeData(
            @Nullable Long countryId, @NonNull Candidate candidate, 
            CandidateIntakeDataUpdate data) throws NoSuchObjectException;
}
