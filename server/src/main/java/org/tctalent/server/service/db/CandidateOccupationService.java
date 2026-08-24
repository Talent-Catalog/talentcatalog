/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db;

import java.util.List;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.request.candidate.occupation.CreateCandidateOccupationRequest;
import org.tctalent.server.request.candidate.occupation.UpdateCandidateOccupationRequest;
import org.tctalent.server.request.candidate.occupation.UpdateCandidateOccupationsRequest;

public interface CandidateOccupationService {

    CandidateOccupation createCandidateOccupation(CreateCandidateOccupationRequest request);

    void deleteCandidateOccupation(Long id);

    List<CandidateOccupation> listMyOccupations();

    List<CandidateOccupation> listCandidateOccupations(Long candidateId);

    /* Lists all occupations selected by candidates */
    List<Occupation> listOccupations();

    List<CandidateOccupation> updateCandidateOccupations(UpdateCandidateOccupationsRequest request);

    /**
     * Updates the given candidate occupation's details. If the request's {@code principal} flag is
     * true, also marks it as its candidate's principal occupation, switching it from whichever
     * occupation was previously principal (if any) and recording the change as a candidate note.
     * A no-op with no note created if the occupation is already principal.
     *
     * @param request Update request, optionally flagged as principal
     * @return The updated candidate occupation
     */
    CandidateOccupation updateCandidateOccupation(UpdateCandidateOccupationRequest request);
}
