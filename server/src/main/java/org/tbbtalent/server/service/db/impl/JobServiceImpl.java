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
import java.time.OffsetDateTime;
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
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.exception.UnauthorisedActionException;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.SavedSearchType;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.JobSpecification;
import org.tbbtalent.server.repository.db.SalesforceJobOppRepository;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.job.JobInfoForSlackPost;
import org.tbbtalent.server.request.job.SearchJobRequest;
import org.tbbtalent.server.request.job.UpdateJobRequest;
import org.tbbtalent.server.request.link.UpdateLinkRequest;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.FileSystemService;
import org.tbbtalent.server.service.db.JobService;
import org.tbbtalent.server.service.db.SalesforceJobOppService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.SavedSearchService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.SalesforceHelper;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFile;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFolder;

@Service
public class JobServiceImpl implements JobService {
    private final AuthService authService;
    private final UserService userService;
    private final FileSystemService fileSystemService;
    private final GoogleDriveConfig googleDriveConfig;
    private final SalesforceJobOppRepository salesforceJobOppRepository;
    private final SalesforceJobOppService salesforceJobOppService;
    private final SavedListService savedListService;
    private final SavedSearchService savedSearchService;

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    public JobServiceImpl(
        AuthService authService, UserService userService, FileSystemService fileSystemService, GoogleDriveConfig googleDriveConfig,
        SalesforceJobOppRepository salesforceJobOppRepository, SalesforceJobOppService salesforceJobOppService, SavedListService savedListService,
        SavedSearchService savedSearchService) {
        this.authService = authService;
        this.userService = userService;
        this.fileSystemService = fileSystemService;
        this.googleDriveConfig = googleDriveConfig;
        this.salesforceJobOppRepository = salesforceJobOppRepository;
        this.salesforceJobOppService = salesforceJobOppService;
        this.savedListService = savedListService;
        this.savedSearchService = savedSearchService;
    }

    @Override
    public SalesforceJobOpp createJob(UpdateJobRequest request)
        throws EntityExistsException, SalesforceException {
        User loggedInUser = getLoggedInUser("create job");

        //Check if we already have a job for this Salesforce job opp.
        final String sfJoblink = request.getSfJoblink();
        String sfId = SalesforceHelper.extractIdFromSfUrl(sfJoblink);
        SalesforceJobOpp job = salesforceJobOppService.getJobOppById(sfId);
        if (job != null) {
            throw new EntityExistsException("job", job.getName() + " (" + job.getId() + ")" );
        }

        //Create job
        job = salesforceJobOppService.createJobOpp(sfId);
        if (job == null) {
            throw new InvalidRequestException("No such Salesforce opportunity: " + sfJoblink);
        }

        job.setAuditFields(loggedInUser);

        //Create submission list
        UpdateSavedListInfoRequest savedListInfoRequest = new UpdateSavedListInfoRequest();
        savedListInfoRequest.setRegisteredJob(true);
        savedListInfoRequest.setSfJoblink(sfJoblink);
        SavedList savedList = savedListService.createSavedList(savedListInfoRequest);

        job.setSubmissionList(savedList);
        return salesforceJobOppRepository.save(job);
    }

    private User getLoggedInUser(String operation) {
        User loggedInUser = authService.getLoggedInUser().orElseThrow(
            () -> new UnauthorisedActionException(operation)
        );
        return loggedInUser;
    }

    @NonNull
    @Override
    public SalesforceJobOpp getJob(long id) throws NoSuchObjectException {
        return salesforceJobOppRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(SalesforceJobOpp.class, id));
    }

    @Override
    public Page<SalesforceJobOpp> searchJobs(SearchJobRequest request) {
        User loggedInUser = userService.getLoggedInUser();
        Page<SalesforceJobOpp> jobs = salesforceJobOppRepository.findAll(
            JobSpecification.buildSearchQuery(request, loggedInUser),
            request.getPageRequest());

        return jobs;
    }

    @NonNull
    @Override
    public SalesforceJobOpp createSuggestedSearch(long id, String suffix) throws NoSuchObjectException {
        User loggedInUser = getLoggedInUser("create search");

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
            request.setSearchCandidateRequest(searchCandidateRequest);
        }
        SavedSearch search = savedSearchService.createSavedSearch(request);

        Set<SavedSearch> searches = job.getSuggestedSearches();
        searches.add(search);

        job.setAuditFields(loggedInUser);

        return salesforceJobOppRepository.save(job);
    }

    @NonNull
    @Override
    public JobInfoForSlackPost extractJobInfoForSlack(long id, String tcJobLink) throws NoSuchObjectException {
        final SalesforceJobOpp job = getJob(id);

        JobInfoForSlackPost jobInfo = new JobInfoForSlackPost();
        jobInfo.setJobName(job.getName());

        User contact = job.getContactUser();
        if (contact == null) {
            contact = job.getPublishedBy();
        }
        if (contact == null) {
            contact = job.getCreatedBy();
        }
        jobInfo.setContact(contact);

        final String jobSummary = job.getJobSummary();
        jobInfo.setJobSummary(jobSummary == null ? "No summary supplied" : jobSummary);

        jobInfo.setSfJobLink(SalesforceHelper.sfOppIdToLink(job.getSfId()));

        jobInfo.setTcJobLink(tcJobLink);

        return jobInfo;
    }

    @Override
    public @NonNull SalesforceJobOpp publishJob(long id) throws NoSuchObjectException {
        SalesforceJobOpp job = getJob(id);
        User loggedInUser = authService.getLoggedInUser().orElseThrow(
            () -> new UnauthorisedActionException("publish job")
        );
        job.setPublishedBy(loggedInUser);
        job.setPublishedDate(OffsetDateTime.now());
        return salesforceJobOppRepository.save(job);
    }

    @NonNull
    @Override
    public SalesforceJobOpp removeSuggestedSearch(long id, long savedSearchId) {
        User loggedInUser = getLoggedInUser("remove search");

        SalesforceJobOpp job = getJob(id);

        SavedSearch search = savedSearchService.getSavedSearch(savedSearchId);

        Set<SavedSearch> searches = job.getSuggestedSearches();
        searches.remove(search);

        savedSearchService.deleteSavedSearch(savedSearchId);

        job.setAuditFields(loggedInUser);

        return salesforceJobOppRepository.save(job);
    }

    @Override
    public List<SalesforceJobOpp> searchJobsUnpaged(SearchJobRequest request) {
        User loggedInUser = userService.getLoggedInUser();
        List<SalesforceJobOpp> jobs = salesforceJobOppRepository.findAll(
            JobSpecification.buildSearchQuery(request, loggedInUser));
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
        User loggedInUser = getLoggedInUser("update jd");

        SalesforceJobOpp job = getJob(id);
        if (job.getSubmissionList() == null) {
            throw new InvalidRequestException("Job " + id + " does not have submission list");
        }
        setJobJdLink(job, updateLinkRequest.getName(), updateLinkRequest.getUrl());
        job.setAuditFields(loggedInUser);

        return salesforceJobOppRepository.save(job);
    }

    @Override
    @NonNull
    public SalesforceJobOpp updateJoiLink(long id, UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException {
        User loggedInUser = getLoggedInUser("update joi");

        SalesforceJobOpp job = getJob(id);
        if (job.getSubmissionList() == null) {
            throw new InvalidRequestException("Job " + id + " does not have submission list");
        }
        setJobJoiLink(job, updateLinkRequest.getName(), updateLinkRequest.getUrl());

        job.setAuditFields(loggedInUser);

        return salesforceJobOppRepository.save(job);
    }

    @NonNull
    @Override
    public SalesforceJobOpp updateJob(long id, UpdateJobRequest request)
        throws NoSuchObjectException, SalesforceException {
        User loggedInUser = getLoggedInUser("update job");
        SalesforceJobOpp job = getJob(id);
        job.setSubmissionDueDate(request.getSubmissionDueDate());
        job.setAuditFields(loggedInUser);
        return salesforceJobOppRepository.save(job);
    }

    @NonNull
    @Override
    public SalesforceJobOpp updateJobSummary(long id, String summary) throws NoSuchObjectException {
        User loggedInUser = getLoggedInUser("update job");
        SalesforceJobOpp job = getJob(id);
        job.setJobSummary(summary);
        job.setAuditFields(loggedInUser);
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

    @Override
    @NonNull
    public SalesforceJobOpp updateStarred(long id, boolean starred) throws NoSuchObjectException {
        User loggedInUser = userService.getLoggedInUser();
        SalesforceJobOpp job = getJob(id);
        if (starred) {
            job.addStarringUser(loggedInUser);
        } else {
            job.removeStarringUser(loggedInUser);
        }
        return salesforceJobOppRepository.save(job);
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
        job.setAuditFields(authService.getLoggedInUser().orElse(null));
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
