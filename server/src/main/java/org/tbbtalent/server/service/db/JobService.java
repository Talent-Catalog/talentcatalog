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
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.request.job.SearchJobRequest;
import org.tbbtalent.server.request.job.UpdateJobRequest;

/**
 * Service for managing {@link SalesforceJobOpp}
 *
 * @author John Cameron
 */
public interface JobService {

    //TODO JC This text needs to change
    /**
     * Registered a new job matching a job opportunity on Salesforce
     * @param request Request which includes a link to the associated Salesforce job opportunity
     * @return Created job
     * @throws InvalidRequestException if there is already a job associated with the requested
     * Salesforce job opportunity.
     * @throws SalesforceException if there are issues contacting Salesforce
     */
    SalesforceJobOpp createJob(UpdateJobRequest request)
        throws InvalidRequestException, SalesforceException;

    /**
     * Get the Job with the given tcJobId.
     * @param tcJobId tcJobId of job to get
     * @return Job
     * @throws NoSuchObjectException if there is no Job with this id.
     */
    @NonNull
    SalesforceJobOpp getJob(long tcJobId) throws NoSuchObjectException;

    /**
     * Get all jobs matching the given search request
     * @param request - Search Request (paging info is ignored)
     * @return Jobs matching the request
     */
    List<SalesforceJobOpp> searchJobsUnpaged(SearchJobRequest request);

    /**
     * Get jobs from a paged search request
     * @param request - Paged Search Request
     * @return Page of jobs
     */
    Page<SalesforceJobOpp> searchJobs(SearchJobRequest request);

    /**
     * Updates the job with the given TCJobId with data contained in the given request.
      * @param tcJobId Id of job to be updated
     * @param request Requested updated data
     * @return Updated job
     * @throws NoSuchObjectException if there is no Job with this id.
     * @throws SalesforceException if there are issues contacting Salesforce
     */
    @NonNull
    SalesforceJobOpp updateJob(long tcJobId, UpdateJobRequest request)
        throws NoSuchObjectException, SalesforceException;

    /**
     * Updates all open Jobs from their corresponding records on Salesforce
     */
    void updateOpenJobs();

}
