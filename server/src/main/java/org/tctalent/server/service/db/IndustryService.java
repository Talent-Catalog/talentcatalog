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
import org.tctalent.server.model.db.Industry;
import org.tctalent.server.request.industry.CreateIndustryRequest;
import org.tctalent.server.request.industry.SearchIndustryRequest;
import org.tctalent.server.request.industry.UpdateIndustryRequest;

public interface IndustryService {

    List<Industry> listIndustries();

    Page<Industry> searchIndustries(SearchIndustryRequest request);

    Industry getIndustry(long id);

    Industry createIndustry(CreateIndustryRequest request) throws EntityExistsException;

    Industry updateIndustry(long id, UpdateIndustryRequest request) throws EntityExistsException ;

    boolean deleteIndustry(long id) throws EntityReferencedException;

}
