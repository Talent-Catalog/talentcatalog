/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
import org.springframework.lang.Nullable;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateDependant;
import org.tctalent.server.model.db.CandidateVisaJobCheck;
import org.tctalent.server.request.candidate.visa.CandidateVisaCheckData;
import org.tctalent.server.request.candidate.visa.job.CreateCandidateVisaJobCheckRequest;

public interface CandidateVisaJobCheckService {
    /**
     * Gets the candidate visa check record from the given id.
     * @param visaJobId ID of visa
     * @return Desired record
     * @throws NoSuchObjectException if the there is no visa job check record with that id
     */
    CandidateVisaJobCheck getVisaJobCheck(long visaJobId) throws NoSuchObjectException;

    /**
     * Gets the candidate visa job check record from the given ids.
     * @param candidateId ID of candidate
     * @param jobOppId ID of job opportunity
     * @return Desired record
     * @throws NoSuchObjectException if the there is no candidate visa job check record with
     * for that candidate and job opportunity
     */
    // TODO decide if we keep this
    CandidateVisaJobCheck getVisaJobCheck(Long candidateId, Long jobOppId)
        throws NoSuchObjectException;

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
     * Updates the candidate visa job data associated with the ID provided.
     * @param visaJobId ID of visa job entity - If null this method does nothing
     * @param data Partially populated CandidateVisaCheckData object. Null data
     *             fields are ignored. Only non null fields are updated.
     * @throws NoSuchObjectException if the there is no CandidateVisaJobCheck record with the given ID.
     */
    void updateIntakeData(
            @Nullable Long visaJobId, CandidateVisaCheckData data) throws NoSuchObjectException;

    /**
     * Gets a list of CandidateDependant objects listed as relocating in a given candidate visa job check.
     * Useful if more processing needs to be done on this data which is otherwise in the form of
     * a comma-separated string.
     * @param visaJobCheck instance of {@link CandidateVisaJobCheck}
     * @return list of candidate dependants
     * @throws NoSuchObjectException if there are no dependants on this visa job check.
     */
    // TODO decide if we need this and its repo method - pass the visaJoBCheck if keeping
    List<CandidateDependant> getRelocatingDependants(CandidateVisaJobCheck visaJobCheck)
        throws NoSuchObjectException;

}
