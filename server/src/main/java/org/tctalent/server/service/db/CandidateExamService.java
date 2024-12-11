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
import jakarta.validation.constraints.NotNull;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;
import org.tctalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tctalent.server.request.candidate.exam.UpdateCandidateExamRequest;
import org.tctalent.server.request.candidate.exam.UpdateCandidateExamsRequest;

public interface CandidateExamService {

    /**
     * Creates a new candidate exam record from the data in the given
     * request.
     * @param candidateId ID of candidate
     * @param request Request containing exam details
     * @return Created record - including database id of exam record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no Nationality with the id given in the request
     */

    CandidateExam createExam(
            long candidateId, CreateCandidateExamRequest request)
            throws NoSuchObjectException;

    /**
     * Updates candidate exam records from the data in the given request.
     * @param request Request containing exam updates
     * @return List of updated candidate exam records
     */

    @NotNull
    CandidateExam updateCandidateExam(UpdateCandidateExamRequest request);

    /**
     * List of CandidateExam associated to candidate with given candidate id.
     * @param id ID of candidate whose CandidateExam we want
     * @return list of candidate CandidateExam - empty if the candidate id is unknown
     */
    @NotNull
    List<CandidateExam> list(long id);

}
