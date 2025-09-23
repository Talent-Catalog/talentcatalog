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
import org.tctalent.server.model.db.CandidateTravelDocForm;
import org.tctalent.server.request.form.TravelDocFormData;

/**
 * Manages instances of forms
 *
 * @author John Cameron
 */
public interface TravelDocFormInstanceService {

    /**
     * Creates a form instance, or updates any existing instance, for the given candidate.
     * @param candidate Candidate accessing this form
     * @param request Contains data used to populate form
     * @return Updated form instance
     */
    @NonNull
    CandidateTravelDocForm createOrUpdateTravelDocForm(
        @NonNull Candidate candidate, @NonNull TravelDocFormData request);

    @NonNull
    Optional<CandidateTravelDocForm> getTravelDocForm(@NonNull Candidate candidate);
}
