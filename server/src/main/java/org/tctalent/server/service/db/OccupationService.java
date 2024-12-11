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
import org.springframework.data.domain.Page;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.request.occupation.CreateOccupationRequest;
import org.tctalent.server.request.occupation.SearchOccupationRequest;
import org.tctalent.server.request.occupation.UpdateOccupationRequest;
import org.tctalent.server.util.dto.DtoBuilder;

public interface OccupationService {

    List<Occupation> listOccupations();

    Page<Occupation> searchOccupations(SearchOccupationRequest request);

    Occupation getOccupation(long id);

    Occupation createOccupation(CreateOccupationRequest request) throws EntityExistsException;

    Occupation updateOccupation(long id, UpdateOccupationRequest request) throws EntityExistsException ;

    boolean deleteOccupation(long id) throws EntityReferencedException;

    DtoBuilder selectBuilder();

}
