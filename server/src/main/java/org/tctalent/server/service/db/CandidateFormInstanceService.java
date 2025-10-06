/*
 * Copyright (c) 2025 Talent Catalog.
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

import java.util.Optional;
import org.springframework.lang.NonNull;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.MyFirstForm;
import org.tctalent.server.request.form.MyFirstFormData;

/**
 * Manages instances of forms
 *
 * @author John Cameron
 */
public interface CandidateFormInstanceService {

    /**
     * Creates a form instance, or updates any existing instance, for the given candidate.
     * @param candidate Candidate accessing this form
     * @param request Contains data used to populate form
     * @return Updated form instance
     */
    @NonNull
    MyFirstForm createOrUpdateMyFirstForm(
        @NonNull Candidate candidate, @NonNull MyFirstFormData request);

    @NonNull
    Optional<MyFirstForm> getMyFirstForm(@NonNull Candidate candidate);

    /**
     * Copies non-null fields in the given pendingCandidate to the given candidate.
     * <p/>
     * This includes CandidateProperties and other linked candidate entities.
     * @param pendingCandidate Temporary candidate entity containing values set on a
     *                         Candidate form subclass of CandidateFormInstance
     * @param candidate Target candidate (typically a live entity) which is populated
     */
    void populateCandidateFromPending(
        @NonNull Candidate pendingCandidate, @NonNull Candidate candidate);

}
