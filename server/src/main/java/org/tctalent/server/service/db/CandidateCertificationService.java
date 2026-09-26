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
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tctalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;

public interface CandidateCertificationService {

    List<CandidateCertification> list(long id);

    CandidateCertification createCandidateCertification(CreateCandidateCertificationRequest request);

    CandidateCertification updateCandidateCertification(UpdateCandidateCertificationRequest request);

    /**
     * Deletes the certification with the given id.
     * <p/>
     * Users with admin privileges may delete any candidate's certification. Other users - ie
     * candidates using the candidate portal - may only delete their own.
     * @param id Id of the certification to delete
     * @throws org.tctalent.server.exception.InvalidSessionException if no user is logged in, or
     * a non admin user has no associated candidate
     * @throws org.tctalent.server.exception.NoSuchObjectException if no certification exists
     * with the given id
     * @throws org.tctalent.server.exception.InvalidCredentialsException if a non admin user
     * attempts to delete another candidate's certification
     */
    void deleteCandidateCertification(Long id);

}
