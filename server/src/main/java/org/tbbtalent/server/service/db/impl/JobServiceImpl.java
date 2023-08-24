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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
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
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateOpportunity;
import org.tbbtalent.server.model.db.CandidateOpportunityStage;
import org.tbbtalent.server.model.db.JobOppIntake;
import org.tbbtalent.server.model.db.JobOpportunityStage;
import org.tbbtalent.server.model.db.PartnerImpl;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.SavedSearchType;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.JobSpecification;
import org.tbbtalent.server.repository.db.SalesforceJobOppRepository;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tbbtalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tbbtalent.server.request.job.JobInfoForSlackPost;
import org.tbbtalent.server.request.job.JobIntakeData;
import org.tbbtalent.server.request.job.SearchJobRequest;
import org.tbbtalent.server.request.job.UpdateJobRequest;
import org.tbbtalent.server.request.link.UpdateLinkRequest;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.CandidateOpportunityService;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.FileSystemService;
import org.tbbtalent.server.service.db.JobOppIntakeService;
import org.tbbtalent.server.service.db.JobService;
import org.tbbtalent.server.service.db.SalesforceBridgeService;
import org.tbbtalent.server.service.db.SalesforceJobOppService;
import org.tbbtalent.server.service.db.SalesforceService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.SavedSearchService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.SalesforceHelper;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFile;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFolder;

@Service
public class JobServiceImpl implements JobService {

    /**
     * This is initialized (in {@link #initialiseClosingCandidateStageLogic()} with the logic which
     * drives selecting the appropriate candidate opp closing stage to be selected when the
     * associated job opp is closed with the given stage.
     */
    private final EnumMap<JobOpportunityStage, EnumMap<CandidateOpportunityStage, CandidateOpportunityStage>>
        closingStageLogic = new EnumMap<>(JobOpportunityStage.class);

    private final static String EXCLUSION_LIST_SUFFIX = "Exclude";

    private final static DateTimeFormatter nextStepDateFormat = DateTimeFormatter.ofPattern("ddMMMyy", Locale.ENGLISH);
    private final AuthService authService;
    private final CandidateOpportunityService candidateOpportunityService;
    private final CandidateSavedListService candidateSavedListService;
    private final UserService userService;
    private final FileSystemService fileSystemService;
    private final GoogleDriveConfig googleDriveConfig;
    private final SalesforceBridgeService salesforceBridgeService;
    private final SalesforceService salesforceService;
    private final SalesforceJobOppRepository salesforceJobOppRepository;
    private final SalesforceJobOppService salesforceJobOppService;
    private final SavedListService savedListService;
    private final SavedSearchService savedSearchService;
    private final JobOppIntakeService jobOppIntakeService;

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    public JobServiceImpl(
            AuthService authService, CandidateOpportunityService candidateOpportunityService,
        CandidateSavedListService candidateSavedListService, UserService userService, FileSystemService fileSystemService, GoogleDriveConfig googleDriveConfig,
            SalesforceBridgeService salesforceBridgeService, SalesforceService salesforceService,
            SalesforceJobOppRepository salesforceJobOppRepository, SalesforceJobOppService salesforceJobOppService, SavedListService savedListService,
            SavedSearchService savedSearchService, JobOppIntakeService jobOppIntakeService) {
        this.authService = authService;
        this.candidateOpportunityService = candidateOpportunityService;
        this.candidateSavedListService = candidateSavedListService;
        this.userService = userService;
        this.fileSystemService = fileSystemService;
        this.googleDriveConfig = googleDriveConfig;
        this.salesforceBridgeService = salesforceBridgeService;
        this.salesforceService = salesforceService;
        this.salesforceJobOppRepository = salesforceJobOppRepository;
        this.salesforceJobOppService = salesforceJobOppService;
        this.savedListService = savedListService;
        this.savedSearchService = savedSearchService;
        this.jobOppIntakeService = jobOppIntakeService;

        initialiseClosingCandidateStageLogic();
    }

    /**
     * Updates the closing logic to say tha when a job is closed in the given stage, then any
     * candidate opp associated with that job, which is currently in the given currentCandidateStage
     * should be closed with the given closedCandidateStage.
     * @param closedJobStage Stage of closed job
     * @param currentCandidateStage Current stage of candidate opp
     * @param closedCandidateStage Stage that the candidate opp should be closed as
     */
    private void addClosingLogic(JobOpportunityStage closedJobStage,
        CandidateOpportunityStage currentCandidateStage,
        CandidateOpportunityStage closedCandidateStage) {

        EnumMap<CandidateOpportunityStage, CandidateOpportunityStage> oppCurrentToCloseMap
            = closingStageLogic.get(closedJobStage);
        if (oppCurrentToCloseMap == null) {
            oppCurrentToCloseMap = new EnumMap<>(CandidateOpportunityStage.class);
            closingStageLogic.put(closedJobStage, oppCurrentToCloseMap);
        }
        oppCurrentToCloseMap.put(currentCandidateStage, closedCandidateStage);

    }

    /**
     * Constructs the closing logic in {@link #closingStageLogic}.
     */
    private void initialiseClosingCandidateStageLogic() {

        //Candidates who have not got to an employed stage before job is closed for any reason
        //are closed with notFitForRole
        Arrays.stream(CandidateOpportunityStage.values())
            .filter(s -> !s.isEmployed() && !s.isClosed())
                .forEach(s -> {
                    addClosingLogic(JobOpportunityStage.noInterest,
                        s, CandidateOpportunityStage.notFitForRole);
                    addClosingLogic(JobOpportunityStage.noSuitableCandidates,
                        s, CandidateOpportunityStage.notFitForRole);
                    addClosingLogic(JobOpportunityStage.noJobOffer,
                        s, CandidateOpportunityStage.notFitForRole);
                    addClosingLogic(JobOpportunityStage.noVisa,
                        s, CandidateOpportunityStage.notFitForRole);
                });

        //Override cases after twoWayReview to close with noJobOffer
        addClosingLogic(JobOpportunityStage.noInterest,
            CandidateOpportunityStage.twoWayReview, CandidateOpportunityStage.noJobOffer);
        addClosingLogic(JobOpportunityStage.noInterest,
            CandidateOpportunityStage.offer, CandidateOpportunityStage.noJobOffer);
        addClosingLogic(JobOpportunityStage.noSuitableCandidates,
            CandidateOpportunityStage.twoWayReview, CandidateOpportunityStage.noJobOffer);
        addClosingLogic(JobOpportunityStage.noSuitableCandidates,
            CandidateOpportunityStage.offer, CandidateOpportunityStage.noJobOffer);
        addClosingLogic(JobOpportunityStage.noJobOffer,
            CandidateOpportunityStage.twoWayReview, CandidateOpportunityStage.noJobOffer);
        addClosingLogic(JobOpportunityStage.noJobOffer,
            CandidateOpportunityStage.offer, CandidateOpportunityStage.noJobOffer);
        addClosingLogic(JobOpportunityStage.noVisa,
            CandidateOpportunityStage.twoWayReview, CandidateOpportunityStage.noJobOffer);
        addClosingLogic(JobOpportunityStage.noVisa,
            CandidateOpportunityStage.offer, CandidateOpportunityStage.noJobOffer);

        //Candidates who have had an offer and are at the acceptance stage before job is closed
        //for any reason  are closed with candidateRejectsOffer (because they didn't accept it).
        addClosingLogic(JobOpportunityStage.noInterest,
            CandidateOpportunityStage.acceptance, CandidateOpportunityStage.candidateRejectsOffer);
        addClosingLogic(JobOpportunityStage.noSuitableCandidates,
            CandidateOpportunityStage.acceptance, CandidateOpportunityStage.candidateRejectsOffer);
        addClosingLogic(JobOpportunityStage.noJobOffer,
            CandidateOpportunityStage.acceptance, CandidateOpportunityStage.candidateRejectsOffer);
        addClosingLogic(JobOpportunityStage.noVisa,
            CandidateOpportunityStage.acceptance, CandidateOpportunityStage.candidateRejectsOffer);

        //Candidates in later stages when job is closed with noVisa are set to noVisa
        addClosingLogic(JobOpportunityStage.noVisa,
            CandidateOpportunityStage.provincialVisaPreparation, CandidateOpportunityStage.noVisa);
        addClosingLogic(JobOpportunityStage.noVisa,
            CandidateOpportunityStage.provincialVisaProcessing, CandidateOpportunityStage.noVisa);
        addClosingLogic(JobOpportunityStage.noVisa,
            CandidateOpportunityStage.visaPreparation, CandidateOpportunityStage.noVisa);
        addClosingLogic(JobOpportunityStage.noVisa,
            CandidateOpportunityStage.visaProcessing, CandidateOpportunityStage.noVisa);
    }

    @Override
    public SalesforceJobOpp createJob(UpdateJobRequest request)
        throws EntityExistsException, SalesforceException {
        User loggedInUser = getLoggedInUser("create job");

        //The partner associated with the person who created the job is the job creator
        final PartnerImpl loggedInUserPartner = loggedInUser.getPartner();
        if (!loggedInUserPartner.isJobCreator()) {
            throw new UnauthorisedActionException("create job");
        }

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

        updateJobFromRequest(job, request);

        job.setAuditFields(loggedInUser);

        //Create submission list
        UpdateSavedListInfoRequest savedListInfoRequest = new UpdateSavedListInfoRequest();
        savedListInfoRequest.setRegisteredJob(true);
        savedListInfoRequest.setSfJoblink(sfJoblink);
        SavedList submissionList = savedListService.createSavedList(savedListInfoRequest);
        job.setSubmissionList(submissionList);

        String exclusionListName = submissionList.getName() + EXCLUSION_LIST_SUFFIX;

        SavedList exclusionList;
        try {
           //Create exclusion list for the employer (account) associated with this job
           exclusionList =
               salesforceBridgeService.findSeenCandidates(exclusionListName, job.getAccountId());
        } catch (Exception ex) {
            log.error("CreateJob: Could not create exclusion list", ex);
            UpdateSavedListInfoRequest req = new UpdateSavedListInfoRequest();
            req.setName(exclusionListName);
            exclusionList = savedListService.createSavedList(req);
        }
        job.setExclusionList(exclusionList);

        job.setJobCreator(loggedInUserPartner);

        return salesforceJobOppRepository.save(job);
    }

    private User getLoggedInUser(String operation) {
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            throw new UnauthorisedActionException(operation);
        }
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

    @Async
    @Override
    public void loadJobOppsAndCandidateOpps() {

        log.info("Loading candidate opportunities from Salesforce");

        final int limit = 100;

        String lastId = null;
        int totalOpps = 0;
        int nOpps = -1;
        while (nOpps != 0) {

            log.info("Attempting to load up to " + limit + " opps from " + (lastId == null ? "start" : lastId));
            List<Opportunity> ops = salesforceService.findCandidateOpportunities(
                lastId == null ? null : "Id > '" + lastId + "'", limit);
            nOpps = ops.size();
            totalOpps += nOpps;
            log.info("Loaded " + nOpps + " candidate opportunities from Salesforce. Total " + totalOpps);
            if (nOpps > 0) {
                lastId = ops.get(nOpps - 1).getId();

                for (Opportunity op : ops) {
                    String jobOppId = op.getParentOpportunityId();
                    if (jobOppId == null) {
                        log.warn("Candidate opportunity without parent job opp: " + op.getName());
                    } else {
                        CandidateOpportunity candidateOpp =
                            candidateOpportunityService.loadCandidateOpportunity(op);
//                        log.info("Updated/created candidate opportunity " + candidateOpp.getName());
                    }
                }
            }
        }
    }

    @Override
    public @NonNull SalesforceJobOpp publishJob(long id) throws NoSuchObjectException {
        SalesforceJobOpp job = getJob(id);
        User loggedInUser = authService.getLoggedInUser().orElseThrow(
            () -> new UnauthorisedActionException("publish job")
        );

        final JobOpportunityStage stage = job.getStage();
        if (stage.compareTo(JobOpportunityStage.candidateSearch) < 0 )  {
            //Current stage is before CandidateSearch so update it to CandidateSearch here
            job.setStage(JobOpportunityStage.candidateSearch);

            //Update Salesforce stage to match - setting Next Step and Due date
            final LocalDate submissionDueDate = job.getSubmissionDueDate();

            //Next step
            final String nowDate = nextStepDateFormat.format(LocalDateTime.now());
            final String nextStep = nowDate + ": Waiting to receive candidate CVs for review";

            salesforceService.updateEmployerOpportunityStage(
                job.getSfId(), JobOpportunityStage.candidateSearch, nextStep, submissionDueDate);
        }

        job.setPublishedBy(loggedInUser);
        job.setPublishedDate(OffsetDateTime.now());

        //Save non-empty submission list to suggested list.
        SavedList submissionList = job.getSubmissionList();
        if (!submissionList.getCandidates().isEmpty()) {

            CopySourceContentsRequest request = new CopySourceContentsRequest();
            request.setSavedListId(0L);
            request.setNewListName(submissionList.getName() + "-suggest");
            request.setSfJoblink(SalesforceHelper.sfOppIdToLink(job.getSfId()));
            //Copy to the target list.
            SavedList suggestedList = candidateSavedListService.copy(submissionList, request);
            job.setSuggestedList(suggestedList);
        }

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

    @Override
    public void updateIntakeData(long id, JobIntakeData data) throws NoSuchObjectException {
        SalesforceJobOpp job = getJob(id);
         JobOppIntake intake = job.getJobOppIntake();
        if (intake == null) {
            intake = jobOppIntakeService.create(data);
            job.setJobOppIntake(intake);
            salesforceJobOppRepository.save(job);
        } else {
            jobOppIntakeService.update(intake.getId(), data);
        }
    }

    private void updateJobFromRequest(SalesforceJobOpp job, UpdateJobRequest request) {
        final JobOpportunityStage stage = request.getStage();
        if (stage != null) {
            job.setStage(stage);

            //Do automation logic
            if (stage.isClosed()) {
                closeUnclosedCandidateOppsForJob(job, stage);
            }
        }

        final String nextStep = request.getNextStep();
        if (nextStep != null) {
            job.setNextStep(nextStep);
        }

        final LocalDate nextStepDueDate = request.getNextStepDueDate();
        if (nextStepDueDate != null) {
            job.setNextStepDueDate(nextStepDueDate);
        }

        final Long contactUserId = request.getContactUserId();
        if (contactUserId != null) {
            User contactUser = userService.getUser(contactUserId);
            job.setContactUser(contactUser);
        }

        final LocalDate submissionDueDate = request.getSubmissionDueDate();
        if (submissionDueDate != null) {
            job.setSubmissionDueDate(submissionDueDate);
        }
    }

    @NonNull
    @Override
    public SalesforceJobOpp updateJob(long id, UpdateJobRequest request)
        throws NoSuchObjectException, SalesforceException {
        User loggedInUser = getLoggedInUser("update job");
        SalesforceJobOpp job = getJob(id);

        final JobOpportunityStage stage = request.getStage();
        final String nextStep = request.getNextStep();
        final LocalDate nextStepDueDate = request.getNextStepDueDate();
        salesforceService.updateEmployerOpportunityStage(
            job.getSfId(), stage, nextStep, nextStepDueDate);

        updateJobFromRequest(job, request);
        job.setAuditFields(loggedInUser);
        return salesforceJobOppRepository.save(job);
    }

    private void closeUnclosedCandidateOppsForJob(SalesforceJobOpp job, JobOpportunityStage jobCloseStage) {
        Set<CandidateOpportunity> candidateOpportunities = job.getCandidateOpportunities();
        final List<CandidateOpportunity> activeOpps = candidateOpportunities.stream()
            //Not interested in opps which are already closed or at an employed stage
            .filter(co -> !co.isClosed() && !co.getStage().isEmployed()).toList();

        //This will be populated with the candidates whose opps need to be updated for each
        //closing stage.
        Map<CandidateOpportunityStage, List<Candidate>> closingStageCandidatesMap = new HashMap<>();

        if (activeOpps.size() > 0) {
            final EnumMap<CandidateOpportunityStage, CandidateOpportunityStage>
                currentToClosingStageMap = closingStageLogic.get(jobCloseStage);

            for (CandidateOpportunity activeOpp : activeOpps) {
                CandidateOpportunityStage closingStage = currentToClosingStageMap.get(activeOpp.getStage());
                if (closingStage == null) {
                    //Missing logic
                    log.warn("Closing logic missing case for job closing stage " + jobCloseStage +
                        " and candidate in stage " + activeOpp.getStage());
                    //Default to closing candidate opp as notFitForRole
                    closingStage = CandidateOpportunityStage.notFitForRole;
                }
                List<Candidate> candidates = closingStageCandidatesMap.computeIfAbsent(
                    closingStage, k -> new ArrayList<>());
                candidates.add(activeOpp.getCandidate());
            }

            for (Entry<CandidateOpportunityStage, List<Candidate>> stageListEntry :
                closingStageCandidatesMap.entrySet()) {

                CandidateOpportunityParams params = new CandidateOpportunityParams();
                final CandidateOpportunityStage candidateOppClosedStage = stageListEntry.getKey();
                params.setStage(candidateOppClosedStage);
                params.setClosingComments("Job opportunity closed: " + jobCloseStage.toString());

                candidateOpportunityService.createUpdateCandidateOpportunities(
                    stageListEntry.getValue(), job, params);
            }

            log.info("Closed opps for candidates going for job  " + job.getId() + ": "
                + activeOpps.stream().map(opp -> opp.getCandidate().getCandidateNumber())
                .collect(Collectors.joining(",")));

        }
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
