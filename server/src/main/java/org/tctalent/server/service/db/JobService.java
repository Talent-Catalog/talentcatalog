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

import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.request.job.JobInfoForSlackPost;
import org.tctalent.server.request.job.JobIntakeData;
import org.tctalent.server.request.job.SearchJobRequest;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.request.link.UpdateLinkRequest;

import java.io.IOException;
import java.util.List;

/**
 * Service for managing {@link SalesforceJobOpp}
 *
 * @author John Cameron
 */
public interface JobService {

    /**
     * Creates a new job matching a job opportunity on Salesforce
     * @param request Request which includes a link to the associated Salesforce job opportunity
     * @return Created job
     * @throws EntityExistsException if there is already a job associated with the requested
     * Salesforce job opportunity.
     * @throws SalesforceException if there are issues contacting Salesforce
     */
    SalesforceJobOpp createJob(UpdateJobRequest request)
        throws EntityExistsException, SalesforceException;

    /**
     * Creates or updates a job associated with the given employer.
     * If the request contains a non-null sfId that is used to identify an existing job which is
     * to be updated. Otherwise, a new job is created.
     *
     * @param employer The employer associated with the job
     * @param request Used to create/update job opportunity.
     * @throws SalesforceException If there are errors relating to keys and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    SalesforceJobOpp createUpdateJob(@NonNull Employer employer, @NonNull UpdateJobRequest request)
        throws SalesforceException, WebClientException;

    /**
     * Get the Job with the given id.
     * @param id Id of job to get
     * @return Job
     * @throws NoSuchObjectException if there is no Job with this id or the job contains a
     * bad accountId.
     */
    @NonNull
    SalesforceJobOpp getJob(long id) throws NoSuchObjectException;

    /**
     * Creates a suggested saved search for the job with the given id.
     *
     * @param id     Job id
     * @param suffix Unique suffix for search name (first part of name is job name)
     * @return Updated job which will have the new search added to its suggested searches
     * @throws NoSuchObjectException If no job with that id exists
     */
    @NonNull
    SalesforceJobOpp createSuggestedSearch(long id, String suffix) throws NoSuchObjectException;

    /**
     * Extracts job related information that is used to post to Slack.
     * @param id Job id
     * @param tcJobLink Link to job on TC
     * @return Job information
     * @throws NoSuchObjectException If no job with that id exists
     */
    @NonNull
    JobInfoForSlackPost extractJobInfoForSlack(long id, String tcJobLink) throws NoSuchObjectException;

    /**
     * Add employer link for all jobs, creating employer as needed
     */
    void createEmployerForAllJobs();

    /**
     * Loads ALL old jobs which had candidates, together with their candidate opps - creating TC
     * jobs and cases as needed.
     */
    void loadJobOppsAndCandidateOpps();

    /**
     * Marks job as published by the current user.
     * <p/>
     * <ul>
     *     <li>The job will become visible to everyone else.</li>
     *     <li>The job's stage will be updated to candidateSearch, if it is currently prior to
     *     that stage</li>
     *     <li>Any non empty submissionList will be copied to the suggestedList</li>
     * </ul>
     *
     * @param id ID of job
     * @return Updated job
     * @throws NoSuchObjectException if there is no Job with this id.
     */
    @NonNull
    SalesforceJobOpp publishJob(long id) throws NoSuchObjectException;

    /**
     * Removes the given search from the suggested searches for the given job.
     * @param id Job id
     * @param savedSearchId Id of saved search to be removed
     * @return Updated job which will have the search removed from its selected searches
     * @throws NoSuchObjectException if either there is no job or no search corresponding to the
     * given ids.
     */
    @NonNull
    SalesforceJobOpp removeSuggestedSearch(long id, long savedSearchId) throws NoSuchObjectException;

    /**
     * Returns the ids of chats not fully read by the currently logged in user, which are
     * associated with job opportunities returned from the given search request.
     * @param request - Search Request
     * @return Ids of unread chats
     */
    List<Long> findUnreadChatsInOpps(SearchJobRequest request);

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
     * Updates the intake data associated with the given job.
     * If there is no intake data associated with the job it will create the Job Opp Intake and associate it with the job.
     * Or if the Job Opp Intake already exists it will update the existing record.
     * @param id ID of job
     * @param data Partially populated JobIntakeData record. Null data
     *             fields are ignored. Only non-null fields are updated.
     * @throws NoSuchObjectException if no job is found with that id
     */
    void updateIntakeData(long id, JobIntakeData data) throws NoSuchObjectException;

    /**
     * Updates the job with the given id with data contained in the given request.
      * @param id Id of job to be updated
     * @param request Requested updated data
     * @return Updated job
     * @throws NoSuchObjectException if there is no Job with this id.
     * @throws SalesforceException if there are issues contacting Salesforce
     */
    @NonNull
    SalesforceJobOpp updateJob(long id, UpdateJobRequest request)
        throws NoSuchObjectException, SalesforceException;

    /**
     * Updates the summary of the job with the given id
     * @param id Id of job to be updated
     * @param summary JOb summary
     * @return Updated job
     * @throws NoSuchObjectException if there is no Job with this id.
     */
    @NonNull
    SalesforceJobOpp updateJobSummary(long id, String summary) throws NoSuchObjectException;

    /**
     * Updates the Job Description doc link of the job with the given id
     * @param id ID of job
     * @param updateLinkRequest Details of link (name and url)
     * @return Updated job
     * @throws InvalidRequestException if the job does not have a submission list
     * @throws NoSuchObjectException if there is no Job with this id.
     */
    @NonNull
    SalesforceJobOpp updateJdLink(long id, UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException;

    /**
     * Updates the Job opportunity intake link of the job with the given id
     * @param id ID of job
     * @param updateLinkRequest Details of link (name and url)
     * @return Updated job
     * @throws InvalidRequestException if the job does not have a submission list
     * @throws NoSuchObjectException if there is no Job with this id.
     */
    @NonNull
    SalesforceJobOpp updateJoiLink(long id, UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException;

    /**
     * Updates the interview guidance file link of the job with the given id
     * @param id ID of job
     * @param updateLinkRequest Details of link (name and url)
     * @return Updated job
     * @throws InvalidRequestException if the job does not have a submission list
     * @throws NoSuchObjectException if there is no Job with this id.
     */
    @NonNull
    SalesforceJobOpp updateInterviewGuidanceLink(long id, UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException;

    /**
     * Updates the signed MOU file link of the job with the given id
     * @param id ID of job
     * @param updateLinkRequest Details of link (name and url)
     * @return Updated job
     * @throws InvalidRequestException if the job does not have a submission list
     * @throws NoSuchObjectException if there is no Job with this id.
     */
    @NonNull
    SalesforceJobOpp updateMouLink(long id, UpdateLinkRequest updateLinkRequest)
            throws InvalidRequestException, NoSuchObjectException;

    /**
     * Updates whether or not the job wth the given id is starred by the current user
     * @param id ID of job
     * @param starred True if job should be starred, false if not
     * @return Updated job
     * @throws NoSuchObjectException if there is no Job with this id.
     */
    @NonNull
    SalesforceJobOpp updateStarred(long id, boolean starred)throws NoSuchObjectException;

    /**
     * Updates all open Jobs from their corresponding records on Salesforce
     */
    void updateOpenJobs();

    /**
     * Uploads the given file to the JobDescription subfolder of the folder associated with the
     * submission list of the job with the given id.
     * <p/>
     * Updates the JD name and link fields of the submission list. The link will be the url of the
     * uploaded file on Google.
     * @param id ID of job
     * @param file File containing the job description
     * @return Updated job
     * @throws NoSuchObjectException if there is no Job with this id.
     * @throws IOException           if there is a problem uploading the file.
     * @throws InvalidRequestException if the job does not have a submission list
     */
    SalesforceJobOpp uploadJd(long id, MultipartFile file)
        throws InvalidRequestException, NoSuchObjectException, IOException;

    /**
     * Uploads the given file to the JobDescription subfolder of the folder associated with the
     * submission list of the job with the given id.
     * <p/>
     * Updates the JOI name and link fields of the submission list. The link will be the url of the
     * uploaded file on Google.
     * @param id ID of job
     * @param file File containing the job opportunity intake
     * @return Updated job
     * @throws NoSuchObjectException if there is no Job with this id.
     * @throws IOException           if there is a problem uploading the file.
     * @throws InvalidRequestException if the job does not have a submission list
     */
    SalesforceJobOpp uploadJoi(long id, MultipartFile file)
        throws InvalidRequestException, NoSuchObjectException, IOException;

    /**
     * Uploads the given file to the JobDescription subfolder of the folder associated with the
     * submission list of the job with the given id.
     * <p/>
     * Updates the interview guidance name and link fields of the submission list. The link will be the url of the
     * uploaded file on Google.
     * @param id ID of job
     * @param file File containing the interview guidance
     * @return Updated job
     * @throws NoSuchObjectException if there is no Job with this id.
     * @throws IOException           if there is a problem uploading the file.
     * @throws InvalidRequestException if the job does not have a submission list
     */
    SalesforceJobOpp uploadInterviewGuidance(long id, MultipartFile file)
            throws InvalidRequestException, NoSuchObjectException, IOException;

    /**
     * Uploads the signed MOU to the JobDescription subfolder of the folder associated with the
     * submission list of the job with the given id.
     * <p/>
     * @param id ID of job
     * @param file File containing the signed MOU
     * @return Updated job
     * @throws NoSuchObjectException if there is no Job with this id.
     * @throws IOException           if there is a problem uploading the file.
     * @throws InvalidRequestException if the job does not have a submission list
     */
    SalesforceJobOpp uploadMou(long id, MultipartFile file)
            throws InvalidRequestException, NoSuchObjectException, IOException;
}
