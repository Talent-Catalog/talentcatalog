/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import java.util.Collection;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.springframework.lang.NonNull;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.SalesforceJobOpp;

/**
 * Interface between local job related entities on our database and their counterparts
 * on Salesforce.
 *
 * @author John Cameron
 */
public interface SalesforceJobOppService {

    /**
     * Creates a SalesforceJobOpp associated with Salesforce record with given id
     * and populates it with data taken from Salesforce.
     * @param sfId Salesforce opportunity record id
     * @return SalesforceJobOpp cache record
     * @throws SalesforceException if there are issues contacting Salesforce
     * @throws InvalidRequestException if there is no opportunity with that id.
     */
    @NotNull
    SalesforceJobOpp createJobOpp(String sfId) throws InvalidRequestException, SalesforceException;

    /**
     * Looks up SalesforceJobOp from id, creating one if needed.
     * @param sfId id of salesforce job opportunity
     * @return SalesforceJobOpp associated with id, or null if id is null
     */
    @Nullable
    SalesforceJobOpp getOrCreateJobOppFromId(String sfId) throws InvalidRequestException;

    /**
     * Looks up SalesforceJobOp from link, creating one if needed.
     * @param sfJoblink Link (url) to salesforce job opportunity
     * @return Null if link is null, otherwise SalesforceJobOpp associated with link
     * @throws InvalidRequestException if there is no opportunity with that link
     */
    @Nullable
    SalesforceJobOpp getOrCreateJobOppFromLink(String sfJoblink) throws InvalidRequestException;

    /**
     * Get the Job opportunity with the given id.
     * @param jobId ID of job to get
     * @return job opp
     * @throws NoSuchObjectException if there is no job opp with this id.
     */
    @NonNull
    SalesforceJobOpp getJobOpp(long jobId) throws NoSuchObjectException;

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
     * Updates existing job opportunity from Salesforce
     * @param sfJobOpp Job opportunity
     * @return Updated job opportunity
     */
    SalesforceJobOpp updateJob(SalesforceJobOpp sfJobOpp);

    /**
     * Updates existing Salesforce cache records (SalesforceJobOpp's) corresponding to the given
     * Salesforce ids.
     * <p/>
     * It will also update records for job opportunities which are open on SF
     * and which are locally cached, even if their ids are not in the given list of ids.
     * This is useful for job opportunities which have been closed and then are reopened on SF.
     * Typically, we only pass in ids of local job opps that are open in order to limit the total
     * number of ids. Otherwise, the total number of ids being passed in will grow infinitely
     * over time (and might eventually crash the SF link by making the SF query too long).
     * This way the id's can be limited to open job opps only - ignoring closed opps. This
     * approach ensures that closed opps can be reopened as a result of this update.
     *
     * @param sfIds Salesforce ids of cache records to be updated from Salesforce
     * @throws SalesforceException if there are issues contacting Salesforce
     */
    void updateJobs(Collection<String> sfIds) throws SalesforceException;
}
