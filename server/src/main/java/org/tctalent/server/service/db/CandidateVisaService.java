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
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.request.candidate.visa.CandidateVisaCheckData;
import org.tctalent.server.request.candidate.visa.CreateCandidateVisaCheckRequest;

public interface CandidateVisaService {

    /**
     * Gets the candidate visa check record from the given id.
     * @param visaId ID of visa
     * @return Desired record
     * @throws NoSuchObjectException if the there is no visa job check record with that id
     */
    CandidateVisaCheck getVisaCheck(long visaId) throws NoSuchObjectException;

    /**
     * Lists all the candidate's visa check records from the given candidate id.
     * @param candidateId ID of candidate whose visa checks we want to list
     * @return List of desired records. List can be empty if no visa checks associated.
     */
    List<CandidateVisaCheck> listCandidateVisaChecks(long candidateId);

    /**
     * Creates a new candidate visa check record from the data in the given
     * request.
     * @param candidateId ID of candidate
     * @param request Request containing visa check details
     * @return Created record - including database id of visa check record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no country with the id given in the request
     */
    CandidateVisaCheck createVisaCheck(
            long candidateId, CreateCandidateVisaCheckRequest request)
            throws NoSuchObjectException;

    /**
     * Delete the candidate visa check with the given id.
     * @param visaId ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    boolean deleteVisaCheck(long visaId)
            throws EntityReferencedException, InvalidRequestException;

    /**
     * Updates the candidate visa intake data of the visa intake with the given ID. This includes updating
     * the associated visa job check data.
     * @param visaId ID of visa entity - If null this method does nothing
     * @param data Partially populated visa check data which includes visa job check data. Null data
     *             fields are ignored. Only non null fields are updated.
     * @throws NoSuchObjectException if the there is no visa check with the given id
     */
    void updateIntakeData(
            @Nullable Long visaId, @NonNull CandidateVisaCheckData data) throws NoSuchObjectException;
}
