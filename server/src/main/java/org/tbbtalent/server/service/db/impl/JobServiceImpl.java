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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.configuration.GoogleDriveConfig;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.SavedSearchType;
import org.tbbtalent.server.repository.db.JobSpecification;
import org.tbbtalent.server.repository.db.SalesforceJobOppRepository;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.job.SearchJobRequest;
import org.tbbtalent.server.request.job.UpdateJobRequest;
import org.tbbtalent.server.request.link.UpdateLinkRequest;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.service.db.FileSystemService;
import org.tbbtalent.server.service.db.JobService;
import org.tbbtalent.server.service.db.SalesforceJobOppService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.SavedSearchService;
import org.tbbtalent.server.util.SalesforceHelper;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFile;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFolder;

@Service
public class JobServiceImpl implements JobService {
    private final FileSystemService fileSystemService;
    private final GoogleDriveConfig googleDriveConfig;
    private final SalesforceJobOppRepository salesforceJobOppRepository;
    private final SalesforceJobOppService salesforceJobOppService;
    private final SavedListService savedListService;
    private final SavedSearchService savedSearchService;

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    public JobServiceImpl(
        FileSystemService fileSystemService, GoogleDriveConfig googleDriveConfig,
        SalesforceJobOppRepository salesforceJobOppRepository, SalesforceJobOppService salesforceJobOppService, SavedListService savedListService,
        SavedSearchService savedSearchService) {
        this.fileSystemService = fileSystemService;
        this.googleDriveConfig = googleDriveConfig;
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
    public SalesforceJobOpp createSuggestedSearch(long id, String suffix) throws NoSuchObjectException {
        SalesforceJobOpp job = getJob(id);

        UpdateSavedSearchRequest request = new UpdateSavedSearchRequest();
        request.setSavedSearchType(SavedSearchType.job);
        request.setName(job.getName() + "*-" + suffix);
        request.setSfJoblink(SalesforceHelper.sfOppIdToLink(job.getSfId()));
        SavedList exclusionList = job.getExclusionList();
        if (exclusionList != null) {
            //Add job exclusion list to suggested search
            SearchCandidateRequest searchCandidateRequest = new SearchCandidateRequest();
            searchCandidateRequest.setExclusionListId(exclusionList.getId());
            request.setSearchCandidateRequest(searchCandidateRequest);;
        }
        SavedSearch search = savedSearchService.createSavedSearch(request);

        Set<SavedSearch> searches = job.getSuggestedSearches();
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

    @Override
    @NonNull
    public SalesforceJobOpp updateJdLink(long id, UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = getJob(id);
        if (job.getSubmissionList() == null) {
            throw new InvalidRequestException("Job " + id + " does not have submission list");
        }
        setJobJdLink(job, updateLinkRequest.getName(), updateLinkRequest.getUrl());
        return job;
    }

    @Override
    @NonNull
    public SalesforceJobOpp updateJoiLink(long id, UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException {
        SalesforceJobOpp job = getJob(id);
        if (job.getSubmissionList() == null) {
            throw new InvalidRequestException("Job " + id + " does not have submission list");
        }
        setJobJoiLink(job, updateLinkRequest.getName(), updateLinkRequest.getUrl());
        return job;
    }

    @NonNull
    @Override
    public SalesforceJobOpp updateJob(long id, UpdateJobRequest request)
        throws NoSuchObjectException, SalesforceException {
        SalesforceJobOpp job = getJob(id);
        job.setSubmissionDueDate(request.getSubmissionDueDate());
        return salesforceJobOppRepository.save(job);
    }

    @NonNull
    @Override
    public SalesforceJobOpp updateJobSummary(long id, String summary) throws NoSuchObjectException {
        SalesforceJobOpp job = getJob(id);
        job.setJobSummary(summary);
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

    private void setJobJdLink(SalesforceJobOpp job, String name, String url) {
        SavedList submissionList = job.getSubmissionList();
        submissionList.setFileJdLink(url);
        submissionList.setFileJdName(name);
        savedListService.saveIt(submissionList);
    }

    private void setJobJoiLink(SalesforceJobOpp job, String name, String url) {
        SavedList submissionList = job.getSubmissionList();
        submissionList.setFileJoiLink(url);
        submissionList.setFileJoiName(name);
        savedListService.saveIt(submissionList);
    }

    private GoogleFileSystemFile uploadFile(String folderLink, String fileName,
        MultipartFile file) throws IOException {

        //Save to a temporary file
        InputStream is = file.getInputStream();
        File tempFile = File.createTempFile("job", ".tmp");
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = is.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

        final GoogleFileSystemDrive listFoldersDrive = googleDriveConfig.getListFoldersDrive();
        final GoogleFileSystemFolder parentFolder = new GoogleFileSystemFolder(folderLink);

        //Upload the file to its folder, with the correct name (not the temp
        //file name).
        GoogleFileSystemFile uploadedFile =
            fileSystemService.uploadFile(listFoldersDrive, parentFolder, fileName, tempFile);

        //Delete tempfile
        if (!tempFile.delete()) {
            log.error("Failed to delete temporary file " + tempFile);
        }

        return uploadedFile;
    }

    private GoogleFileSystemFile uploadJobFile(SalesforceJobOpp job, MultipartFile file)
        throws IOException {
        SavedList submissionList = job.getSubmissionList();

        String jdFolderLink = submissionList.getFolderjdlink();

        //Name of file being uploaded (this is the name it had on the
        //originating computer).
        String fileName = file.getOriginalFilename();

        return uploadFile(jdFolderLink, fileName, file);
    }

    @Override
    public SalesforceJobOpp uploadJd(long id, MultipartFile file)
        throws InvalidRequestException, NoSuchObjectException, IOException {

        SalesforceJobOpp job = getJob(id);
        if (job.getSubmissionList() == null) {
            throw new InvalidRequestException("Job " + id + " does not have submission list");
        }
        GoogleFileSystemFile uploadedFile = uploadJobFile(job, file);
        setJobJdLink(job, uploadedFile.getName(), uploadedFile.getUrl());
        return job;
    }

    @Override
    public SalesforceJobOpp uploadJoi(long id, MultipartFile file)
        throws InvalidRequestException, NoSuchObjectException, IOException {

        SalesforceJobOpp job = getJob(id);
        if (job.getSubmissionList() == null) {
            throw new InvalidRequestException("Job " + id + " does not have submission list");
        }
        GoogleFileSystemFile uploadedFile = uploadJobFile(job, file);
        setJobJoiLink(job, uploadedFile.getName(), uploadedFile.getUrl());
        return job;
    }
}
