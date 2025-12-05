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

import org.springframework.lang.NonNull;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateForm;

public interface CandidateFormService {

    /**
     * Get the CandidateForm with the given id.
     * @param id ID of CandidateForm
     * @return CandidateForm
     * @throws NoSuchObjectException if there is no CandidateForm with this id.
     */
    @NonNull
    CandidateForm get(long id) throws NoSuchObjectException;

    /**
     * Get the CandidateForm with the given name.
     * @param name Name of CandidateForm
     * @return CandidateForm
     * @throws NoSuchObjectException if there is no CandidateForm with that name.
     */
    @NonNull
    CandidateForm getByName(String name) throws NoSuchObjectException;

}
