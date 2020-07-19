/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.Industry;
import org.tbbtalent.server.request.industry.CreateIndustryRequest;
import org.tbbtalent.server.request.industry.SearchIndustryRequest;
import org.tbbtalent.server.request.industry.UpdateIndustryRequest;

public interface IndustryService {

    List<Industry> listIndustries();

    Page<Industry> searchIndustries(SearchIndustryRequest request);

    Industry getIndustry(long id);

    Industry createIndustry(CreateIndustryRequest request) throws EntityExistsException;

    Industry updateIndustry(long id, UpdateIndustryRequest request) throws EntityExistsException ;

    boolean deleteIndustry(long id) throws EntityReferencedException;

}
