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
import org.springframework.data.domain.Page;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tctalent.server.request.work.experience.SearchJobExperienceRequest;
import org.tctalent.server.request.work.experience.UpdateJobExperienceRequest;

public interface CandidateJobExperienceService {

    Page<CandidateJobExperience> searchCandidateJobExperience(SearchJobExperienceRequest request);

    /**
     * Computes the context string for a given job experience.
     * <p>
     * The context string is used for generating embeddings or other processing.
     * @param experience Job experience
     * @return Context string.
     */
    String computeExperienceContext(CandidateJobExperience experience);

    CandidateJobExperience createCandidateJobExperience(CreateJobExperienceRequest request);

    CandidateJobExperience updateCandidateJobExperience(UpdateJobExperienceRequest request);

    CandidateJobExperience updateCandidateJobExperience(Long candidateId, UpdateJobExperienceRequest request);

    /**
     * Updates the embeddings for a list of candidate job experiences.
     * <p>
     * This method is intended for batch processing of candidate job experiences to update their
     * embeddings.
     * @param experiences the list of candidate job experiences to update. If empty, that signals
     *                    that the batch processing is complete and the currently building model
     *                    should be set to the status READY.
     */
    void batchUpdateCandidateJobExperienceEmbeddings(List<CandidateJobExperience> experiences);

    void deleteCandidateJobExperience(Long id);
}
