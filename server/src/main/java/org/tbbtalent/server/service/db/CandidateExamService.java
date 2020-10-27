/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import org.springframework.lang.NonNull;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateExam;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.exam.CreateCandidateExamRequest;

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
     * Delete the candidate exam with the given id.
     * @param examId ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because 
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    boolean deleteExam(long examId)
            throws EntityReferencedException, InvalidRequestException;

    /**
     * Updates the candidate exam intake data associated with the given
     * nationality and given candidate.
     * @param candidate Candidate
     * @param data Partially populated CandidateIntakeData record. Null data
     *             fields are ignored. Only non null fields are updated.
     * @throws NoSuchObjectException if the there is no Nationality with the
     * given id or no CandidateExam record with the id given in the data
     */
    void updateIntakeData(
            @NonNull Candidate candidate,
            CandidateIntakeDataUpdate data) throws NoSuchObjectException;
}
