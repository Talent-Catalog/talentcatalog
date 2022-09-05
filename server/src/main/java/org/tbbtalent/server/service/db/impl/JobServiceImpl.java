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
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.Job;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.repository.db.JobRepository;
import org.tbbtalent.server.repository.db.JobSpecification;
import org.tbbtalent.server.request.job.SearchJobRequest;
import org.tbbtalent.server.request.job.UpdateJobRequest;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
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
    public Job createJob(UpdateJobRequest request)
        throws InvalidRequestException, SalesforceException {
        //Only one job can be associated with a Salesforce job opportunity
        //Check if we already have a job for this Salesforce job opp.
        final String sfJoblink = request.getSfJoblink();
        String sfId = SalesforceServiceImpl.extractIdFromSfUrl(sfJoblink);
        Job job = jobRepository.findBySfId(sfId);
        if (job != null) {
            throw new InvalidRequestException(
                "Salesforce job opportunity " + sfJoblink +
                    " is already associated with job " + job.getId() + " (" + job.getName() + ")");
        }

        //Search for existing SalesforceJobOpp associated with this Salesforce record
        SalesforceJobOpp jobOpp = salesforceJobOppService.getJobOppById(sfId);
        if (jobOpp == null) {
            //Create one if none exists
            jobOpp = salesforceJobOppService.createJobOpp(sfId);
            if (jobOpp == null) {
                throw new InvalidRequestException("No such Salesforce opportunity: " + sfJoblink);
            }
        }

        //Create job
        job = new Job();

        //Create submission list
        UpdateSavedListInfoRequest savedListInfoRequest = new UpdateSavedListInfoRequest();
        savedListInfoRequest.setRegisteredJob(true);
        savedListInfoRequest.setSfJoblink(sfJoblink);
        SavedList savedList = savedListService.createSavedList(savedListInfoRequest);

        job.setSubmissionList(savedList);
        return jobRepository.save(job);
    }

    @NonNull
    @Override
    public Job getJob(long jobId) throws NoSuchObjectException {
        return jobRepository.findById(jobId)
            .orElseThrow(() -> new NoSuchObjectException(Job.class, jobId));
    }

    @Override
    public Page<Job> searchJobs(SearchJobRequest request) {
        //Search jobs.
        //opportunities because there could be opps whose state has been changed on SF which
        //means that they could satisfy search request, but they won't be seen because they are
        //still in the cache with their old state.
        Page<Job> jobs = jobRepository.findAll(JobSpecification.buildSearchQuery(request),
            request.getPageRequest());

        return jobs;
    }

    @Override
    public List<Job> searchJobsUnpaged(SearchJobRequest request) {
        List<Job> jobs = jobRepository.findAll(JobSpecification.buildSearchQuery(request));
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


    @Scheduled(cron = "0 0 1 * * ?", zone = "GMT")
    @Async
    @Override
    public void updateOpenJobs() {
        try {
            //Find all open Salesforce jobs
            SearchJobRequest request = new SearchJobRequest();
            request.setSfOppClosed(false);

            List<Job> jobs = searchJobsUnpaged(request);

            //Populate sfIds of jobs
            List<String> sfIds = jobs.stream()
                .filter(j -> j.getSfJobOpp() != null)
                .map(j -> j.getSfJobOpp().getId())
                .collect(Collectors.toList());

            //Now update them from Salesforce
            salesforceJobOppService.updateJobs(sfIds);
        } catch (Exception e) {
            log.error("JobService.updateOpenJobs failed", e);
        }
    }

}
