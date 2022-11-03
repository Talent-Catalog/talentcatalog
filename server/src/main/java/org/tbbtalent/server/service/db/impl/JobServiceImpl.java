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
import java.util.Set;
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
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.SavedSearchType;
import org.tbbtalent.server.repository.db.JobSpecification;
import org.tbbtalent.server.repository.db.SalesforceJobOppRepository;
import org.tbbtalent.server.request.job.SearchJobRequest;
import org.tbbtalent.server.request.job.UpdateJobRequest;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.service.db.JobService;
import org.tbbtalent.server.service.db.SalesforceJobOppService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.SavedSearchService;
import org.tbbtalent.server.util.SalesforceHelper;

@Service
public class JobServiceImpl implements JobService {
    private final SalesforceJobOppRepository salesforceJobOppRepository;
    private final SalesforceJobOppService salesforceJobOppService;
    private final SavedListService savedListService;
    private final SavedSearchService savedSearchService;

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    public JobServiceImpl(
        SalesforceJobOppRepository salesforceJobOppRepository, SalesforceJobOppService salesforceJobOppService, SavedListService savedListService,
        SavedSearchService savedSearchService) {
        this.salesforceJobOppRepository = salesforceJobOppRepository;
        this.salesforceJobOppService = salesforceJobOppService;
        this.savedListService = savedListService;
        this.savedSearchService = savedSearchService;
    }

    @Override
    public SalesforceJobOpp createJob(UpdateJobRequest request)
        throws InvalidRequestException, SalesforceException {
        //Check if we already have a job for this Salesforce job opp.
        final String sfJoblink = request.getSfJoblink();
        String sfId = SalesforceHelper.extractIdFromSfUrl(sfJoblink);
        SalesforceJobOpp job = salesforceJobOppService.getJobOppById(sfId);
        if (job == null) {
            //Create one if none exists
            job = salesforceJobOppService.createJobOpp(sfId);
            if (job == null) {
                throw new InvalidRequestException("No such Salesforce opportunity: " + sfJoblink);
            }
        }

        //Create submission list
        UpdateSavedListInfoRequest savedListInfoRequest = new UpdateSavedListInfoRequest();
        savedListInfoRequest.setRegisteredJob(true);
        savedListInfoRequest.setSfJoblink(sfJoblink);
        SavedList savedList = savedListService.createSavedList(savedListInfoRequest);

        job.setSubmissionList(savedList);
        return salesforceJobOppRepository.save(job);
    }

    @NonNull
    @Override
    public SalesforceJobOpp getJob(long id) throws NoSuchObjectException {
        return salesforceJobOppRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(SalesforceJobOpp.class, id));
    }

    @Override
    public Page<SalesforceJobOpp> searchJobs(SearchJobRequest request) {
        //Search jobs.
        //opportunities because there could be opps whose state has been changed on SF which
        //means that they could satisfy search request, but they won't be seen because they are
        //still in the cache with their old state.
        Page<SalesforceJobOpp> jobs = salesforceJobOppRepository.findAll(JobSpecification.buildSearchQuery(request),
            request.getPageRequest());

        return jobs;
    }

    @NonNull
    @Override
    public SalesforceJobOpp createSuggestedSearch(long id) throws NoSuchObjectException {
        SalesforceJobOpp job = getJob(id);

        Set<SavedSearch> searches = job.getSuggestedSearches();

        UpdateSavedSearchRequest request = new UpdateSavedSearchRequest();
        request.setSavedSearchType(SavedSearchType.job);
        final int searchNumber = searches.size() + 1;
        request.setName(job.getName() + "*-search" + searchNumber);
        request.setSfJoblink(SalesforceHelper.sfOppIdToLink(job.getSfId()));
        SavedSearch search = savedSearchService.createSavedSearch(request);

        searches.add(search);

        return salesforceJobOppRepository.save(job);
    }

    @NonNull
    @Override
    public SalesforceJobOpp removeSuggestedSearch(long id, long savedSearchId) {
        SalesforceJobOpp job = getJob(id);

        SavedSearch search = savedSearchService.getSavedSearch(savedSearchId);

        Set<SavedSearch> searches = job.getSuggestedSearches();
        searches.remove(search);

        savedSearchService.deleteSavedSearch(savedSearchId);

        return salesforceJobOppRepository.save(job);
    }

    @Override
    public List<SalesforceJobOpp> searchJobsUnpaged(SearchJobRequest request) {
        List<SalesforceJobOpp> jobs = salesforceJobOppRepository.findAll(JobSpecification.buildSearchQuery(request));
        return jobs;
    }

    /**
     * Look up job associated with given submission list
     * @param submissionList Submission list
     * @return Associated job, or null if none found
     */
    @Nullable
    private SalesforceJobOpp getJobBySubmissionList(SavedList submissionList) {
        return salesforceJobOppRepository.getJobBySubmissionList(submissionList);
    }

    @NonNull
    @Override
    public SalesforceJobOpp updateJob(long id, UpdateJobRequest request)
        throws NoSuchObjectException, SalesforceException {
        SalesforceJobOpp job = getJob(id);

        job.setSubmissionDueDate(request.getSubmissionDueDate());
        return salesforceJobOppRepository.save(job);
    }

    @Scheduled(cron = "0 0 1 * * ?", zone = "GMT")
    @Async
    @Override
    public void updateOpenJobs() {
        try {
            //Find all open Salesforce jobs
            SearchJobRequest request = new SearchJobRequest();
            request.setSfOppClosed(false);

            List<SalesforceJobOpp> jobs = searchJobsUnpaged(request);

            //Populate sfIds of jobs
            List<String> sfIds = jobs.stream()
                .map(SalesforceJobOpp::getSfId)
                .collect(Collectors.toList());

            //Now update them from Salesforce
            salesforceJobOppService.updateJobs(sfIds);
        } catch (Exception e) {
            log.error("JobService.updateOpenJobs failed", e);
        }
    }

}
