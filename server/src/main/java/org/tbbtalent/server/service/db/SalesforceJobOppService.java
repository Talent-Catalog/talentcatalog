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

import java.util.List;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.SalesforceJobOpp;

/**
 * Access the cache of Salesforce records - in the form of SalesforceJobOpp's.
 *
 * @author John Cameron
 */
public interface SalesforceJobOppService {

    /**
     * Look up the SalesforceJobOpp associated with the Salesforce opportunity record with the
     * given id.
     * @param sfId Salesforce opportunity record id
     * @return SalesforceJobOpp cache record, or null if none found
     */
    @Nullable
    SalesforceJobOpp getJobOppById(String sfId);

    /**
     * Look up the SalesforceJobOpp associated with the Salesforce opportunity record with the
     * given url.
     * <p/>
     * Note that this is a convenience method which just calls {@link #getJobOppById} after
     * extracting the id from the url.
     * @param sfUrl Salesforce opportunity record url
     * @return SalesforceJobOpp cache record, or null if none found
     */
    @Nullable
    SalesforceJobOpp getJobOppByUrl(String sfUrl);

    /**
     * Updates existing Salesforce cache records (SalesforceJobOpp's) corresponding to the given
     * Salesforce ids.
     * <p/>
     * Note that this does NOT create new SalesforceJobOpp's - that should be done before calling
     * this if we have a brand new id. That is because a Job also needs to be created to go with the
     * SalesforceJobOpp
     * @param sfIds Salesforce ids of cache records to be updated from Salesforce
     */
    void update(List<String> sfIds) throws SalesforceException;

    /**
     * Creates an empty SalesforceJobOpp associated with Salesforce record with given url configured
     * to expire immediately - at which time it will be populated with data taken from Salesforce.
     * @param url Url of Salesforce job opportunity record
     * @return SalesforceJobOpp cache record
     */
    @NotNull
    SalesforceJobOpp createExpiringOpp(String url);
}
