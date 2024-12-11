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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.task.TaskAssignment;

public interface CandidatePropertyService {

    /**
     * Creates, or updates if the property already exists, the property with the given name of
     * the given candidate.
     * <p/>
     * A candidate can only have one property with a given name.
     * <p/>
     * A property may be associated with a task assignment - eg where the value of the property
     * is the answer to an assigned question task, and the name is the associated question.
     *
     * @param candidate Candidate whose property is being created or updated
     * @param name Name of the property being created/updated
     * @param value Value of property - can be null. Properties can have null values.
     * @param taskAssignment Task assigment associated with property, if any. Null if none.
     * @return Created/updated property
     */
    CandidateProperty createOrUpdateProperty(@NonNull Candidate candidate,
        @NonNull String name, @Nullable String value, @Nullable TaskAssignment taskAssignment);

    /**
     * Delete the candidate property with the given name.
     * Does nothing if there is no candidate property with that name.
     *
     * @param candidate Candidate whose property is being deleted
     * @param name Name of property to delete
     */
    void deleteProperty(@NonNull Candidate candidate, @NonNull String name);

    /**
     * Finds the property with the given name of the given candidate if one exists.
     *
     * @param candidate Candidate whose property we are looking for
     * @param name Name of the property we are looking for
     * @return Requested property, null if none exists
     */
    @Nullable
    CandidateProperty findProperty(@NonNull Candidate candidate, @NonNull String name);

}
