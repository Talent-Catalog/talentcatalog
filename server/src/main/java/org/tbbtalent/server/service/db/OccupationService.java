/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.Occupation;
import org.tbbtalent.server.request.occupation.CreateOccupationRequest;
import org.tbbtalent.server.request.occupation.SearchOccupationRequest;
import org.tbbtalent.server.request.occupation.UpdateOccupationRequest;

public interface OccupationService {

    List<Occupation> listOccupations();

    Page<Occupation> searchOccupations(SearchOccupationRequest request);

    Occupation getOccupation(long id);

    Occupation createOccupation(CreateOccupationRequest request) throws EntityExistsException;

    Occupation updateOccupation(long id, UpdateOccupationRequest request) throws EntityExistsException ;

    boolean deleteOccupation(long id) throws EntityReferencedException;

}
