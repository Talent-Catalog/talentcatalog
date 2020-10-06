/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.request.candidate.CandidateIntakeData;

public interface CandidateCitizenshipService {

    /**
     * Updates the candidate citizenship intake data associated with the given 
     * nationality and given candidate.
     * @param nationalityId ID of nationality - If null this method does nothing 
     * @param candidate Candidate
     * @param data Partially populated CandidateIntakeData record. Null data
     *             fields are ignored. Only non null fields are updated.
     * @throws NoSuchObjectException if the there is no Nationality with the
     * given id.
     */
    void updateIntakeData(
            @Nullable Long nationalityId, @NonNull Candidate candidate, 
            CandidateIntakeData data) throws NoSuchObjectException;

}
