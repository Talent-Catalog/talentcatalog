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

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.NotImplementedException;
import org.tbbtalent.server.model.db.Job;
import org.tbbtalent.server.model.db.JobOpportunityStage;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.request.job.SearchJobRequest;
import org.tbbtalent.server.request.job.UpdateJobRequest;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
import org.tbbtalent.server.service.db.JobService;
import org.tbbtalent.server.service.db.SavedListService;

@Service
public class JobServiceImpl implements JobService {
    private final SavedListService savedListService;

    public JobServiceImpl(SavedListService savedListService) {
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
        //TODO JC getJob not implemented in JobServiceImpl
        throw new NotImplementedException("JobServiceImpl", "getJob");
    }

    @Override
    public Page<Job> searchJobs(SearchJobRequest request) {
        //TODO JC Dummy search response

        final SearchSavedListRequest savedListRequest = new SearchSavedListRequest();
        savedListRequest.setRegisteredJob(true);
        List<SavedList> jobLists = savedListService.searchSavedLists(savedListRequest).getContent();

        List<Job> jobs = new ArrayList<>();
        Job job;
        SalesforceJobOpp jobOpp;

        jobOpp = new SalesforceJobOpp();
        jobOpp.setStage(JobOpportunityStage.candidateSearch);
        jobOpp.setEmployer("Test Account");
        jobOpp.setCountry("Australia");
        jobOpp.setName("Test Account-Role");

        job = new Job();
        job.setSfJobOpp(jobOpp);
        job.setSubmissionList(jobLists.get(0));
        jobs.add(job);

        return new PageImpl<Job>(jobs);
    }
}
