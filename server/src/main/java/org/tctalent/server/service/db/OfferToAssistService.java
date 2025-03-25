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

import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.OfferToAssist;
import org.tctalent.server.request.KeywordPagedSearchRequest;
import org.tctalent.server.request.OfferToAssistRequest;

/**
 * Service for managing {@link OfferToAssist}'s.
 *
 * @author John Cameron
 */
public interface OfferToAssistService {

    /**
     * Creates an OfferToAssist for certain candidates from the given request.
     *
     * @param request Request to create an OfferToAssist.
     * @return Created OfferToAssist
     * @throws NoSuchObjectException if there is no such candidate with a given id.
     */
    @NonNull
    OfferToAssist createOfferToAssist(OfferToAssistRequest request)
        throws NoSuchObjectException;

    /**
     * Searches the offers to assist with an optional keyword filter.
     * @param request Request with page request and keyword if exists
     * @return Page of offers to assist
     */
    Page<OfferToAssist> searchOffersToAssist(KeywordPagedSearchRequest request);
}
