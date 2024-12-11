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
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.SavedList;

/**
 * Bridge between low level SalesforceService data structures, and high level TC data structures
 * - such as SavedList's.
 *
 * @author John Cameron
 */
public interface SalesforceBridgeService {
    /**
     * Create a list of candidates that the given employer has already seen. This information is
     * retrieved from Salesforce by querying the candidate opportunities associated with that
     * employer.
     *
     * @param listName Name of returned list
     * @param accountId Salesforce account id associated with employer
     * @return List of candidates the employer has already seen
     * @throws NoSuchObjectException   if there is no account (employer) with that id.
     * @throws SalesforceException     if Salesforce had a problem with the data
     * @throws InvalidRequestException If there are security errors.
     */
    @NonNull
    SavedList findSeenCandidates(String listName, String accountId)
        throws NoSuchObjectException, SalesforceException, InvalidRequestException;
}
