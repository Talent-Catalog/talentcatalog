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

import org.springframework.lang.Nullable;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.CandidateVisaJobCheck;
import org.tbbtalent.server.request.candidate.visa.CandidateVisaCheckData;
import org.tbbtalent.server.request.candidate.visa.job.CreateCandidateVisaJobCheckRequest;

public interface CandidateVisaJobCheckService {
    /**
     * Gets the candidate visa check record from the given id.
     * @param visaJobId ID of visa
     * @return Desired record
     * @throws NoSuchObjectException if the there is no visa job check record with that id
     */
    CandidateVisaJobCheck getVisaJobCheck(long visaJobId) throws NoSuchObjectException;

    /**
     * Creates a new candidate visa check record from the data in the given
     * request.
     * @param visaId ID of visa
     * @param request Request containing visa job check details
     * @return Created record - including database id of visa job check record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no country with the id given in the request
     */
    CandidateVisaJobCheck createVisaJobCheck(
            long visaId, CreateCandidateVisaJobCheckRequest request)
            throws NoSuchObjectException;

    /**
     * Delete the candidate visa job check with the given id.
     * @param visaId ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    boolean deleteVisaJobCheck(long visaId)
            throws EntityReferencedException, InvalidRequestException;

    /**
     * Updates the candidate visa intake data associated with the given
     * country and given candidate.
     * @param visaJobId ID of visa job entity - If null this method does nothing
     * @param candidate Candidate
     * @param data Partially populated CandidateIntakeData record. Null data
     *             fields are ignored. Only non null fields are updated.
     * @throws NoSuchObjectException if the there is no country with the
     * given id or no CandidateVisa record with the id given in the data
     */
    void updateIntakeData(
            @Nullable Long visaJobId, CandidateVisaCheckData data) throws NoSuchObjectException;
}
