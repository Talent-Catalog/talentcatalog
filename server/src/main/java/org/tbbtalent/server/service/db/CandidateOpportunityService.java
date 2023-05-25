/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

import org.tbbtalent.server.exception.SalesforceException;

public interface CandidateOpportunityService {

    /**
     * Creates or updates CandidateOpportunities associated with given jobs from Salesforce.
     * @param jobOpportunityIds IDs of Jobs whose associated CandidateOpportunities should be
     *                          updated from Salesforce
     * @throws SalesforceException if there are issues contacting Salesforce
     */
    void loadCandidateOpportunities(String... jobOpportunityIds) throws SalesforceException;

}
