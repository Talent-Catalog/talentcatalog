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

package org.tbbtalent.server.service.db.impl;

import java.util.List;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.NotImplementedException;
import org.tbbtalent.server.model.db.Job;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.repository.db.JobRepository;
import org.tbbtalent.server.repository.db.JobSpecification;
import org.tbbtalent.server.request.job.SearchJobRequest;
import org.tbbtalent.server.request.job.UpdateJobRequest;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
import org.tbbtalent.server.service.db.JobService;
import org.tbbtalent.server.service.db.SalesforceJobOppService;
import org.tbbtalent.server.service.db.SavedListService;

@Service
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final SalesforceJobOppService salesforceJobOppService;
    private final SavedListService savedListService;

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    public JobServiceImpl(JobRepository jobRepository, SalesforceJobOppService salesforceJobOppService, SavedListService savedListService) {
        this.jobRepository = jobRepository;
        this.salesforceJobOppService = salesforceJobOppService;
        this.savedListService = savedListService;
    }

    @Override
    public Job createJob(UpdateJobRequest request) {
        //TODO JC createJob not implemented in JobServiceImpl
        throw new NotImplementedException("JobServiceImpl", "createJob");
    }

    @NonNull
    @Override
    public Job getJob(long jobId) throws NoSuchObjectException {
        return jobRepository.findById(jobId)
            .orElseThrow(() -> new NoSuchObjectException(Job.class, jobId));
    }

    @Override
    public Page<Job> searchJobs(SearchJobRequest request) {

        final SearchSavedListRequest savedListRequest = new SearchSavedListRequest();
        savedListRequest.setRegisteredJob(true);
        List<SavedList> jobLists = savedListService.listSavedLists(savedListRequest);

        //Registered jobs should have a corresponding Job object - create if necessary.
        //TODO JC Note that this will be an increasing overhead as number of old registered job lists grows
        int nJobsCreated = 0;
        int nSfJobOppsCreated = 0;
        for (SavedList jobList : jobLists) {
            final String url = jobList.getSfJoblink();
            if (url != null) {
                Job job = getJobBySubmissionList(jobList);
                if (job == null) {
                    //Create job
                    job = new Job();
                    job.setSubmissionList(jobList);

                    //Search for SalesforceJobOpp from sfJoblink
                    SalesforceJobOpp jobOpp = salesforceJobOppService.getJobOppByUrl(url);
                    if (jobOpp == null) {
                        //Create dummy expired one - will be updated later
                        jobOpp = salesforceJobOppService.createExpiringOpp(url);
                        nSfJobOppsCreated++;
                    }
                    job.setSfJobOpp(jobOpp);

                    jobRepository.save(job);
                    nJobsCreated++;
                }
            }
        }
        log.info("Created " + nJobsCreated + " jobs and " +
            nSfJobOppsCreated + " sfJobOpps from registered job lists");

        //We want to make sure that our cache of Salesforce Job Opportunity details corresponding
        //to these job lists are up to date.
        //Get any Salesforce joblinks in the lists - urls of Salesforce job opportunity records
        List<String> sfJoblinks = savedListService.collectSfJoblinks(jobLists);
        //...and extract the corresponding Salesforce ids
        List<String> sfIds = SalesforceServiceImpl.extractIdFromSfUrl(sfJoblinks);
        //Update the cache of Salesforce job opportunities
        salesforceJobOppService.update(sfIds);

        //Now just execute normal query on Jobs.
        //We now know that any Job salesforceJobOpp field reference will be up to date

        Page<Job> jobs = jobRepository.findAll(JobSpecification.buildSearchQuery(request),
            request.getPageRequest());

        return jobs;
    }

    /**
     * Look up job associated with given submission list
     * @param submissionList Submission list
     * @return Associated job, or null if none found
     */
    @Nullable
    private Job getJobBySubmissionList(SavedList submissionList) {
        return jobRepository.getJobBySubmissionList(submissionList);
    }
}
