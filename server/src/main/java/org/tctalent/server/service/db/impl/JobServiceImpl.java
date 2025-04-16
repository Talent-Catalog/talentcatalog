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

package org.tctalent.server.service.db.impl;

import static org.tctalent.server.util.NextStepHelper.isNextStepDifferent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.configuration.SalesforceConfig;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.JobOppIntake;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.SavedSearchType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.sf.Account;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.model.sf.Opportunity.OpportunityType;
import org.tctalent.server.repository.db.JobSpecification;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tctalent.server.request.job.JobInfoForSlackPost;
import org.tctalent.server.request.job.JobIntakeData;
import org.tctalent.server.request.job.SearchJobRequest;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.request.link.UpdateLinkRequest;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.request.search.UpdateSavedSearchRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.EmployerService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.JobOppIntakeService;
import org.tctalent.server.service.db.JobService;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.OppNotificationService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.SalesforceBridgeService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;
import org.tctalent.server.util.SalesforceHelper;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    /**
     * This is initialized (in {@link #initialiseClosingCandidateStageLogic()} with the logic which
     * drives selecting the appropriate candidate opp closing stage to be selected when the
     * associated job opp is closed with the given stage.
     */
    private EnumMap<JobOpportunityStage,
        EnumMap<CandidateOpportunityStage, CandidateOpportunityStage>> closingStageLogic;

    private final static String EXCLUSION_LIST_SUFFIX = "Exclude";

    private final static DateTimeFormatter nextStepDateFormat = DateTimeFormatter.ofPattern("ddMMMyy", Locale.ENGLISH);
    private final AuthService authService;
    private final CandidateOpportunityService candidateOpportunityService;
    private final CandidateSavedListService candidateSavedListService;
    private final EmailHelper emailHelper;
    private final EmployerService employerService;
    private final UserService userService;
    private final FileSystemService fileSystemService;
    private final GoogleDriveConfig googleDriveConfig;

    private final JobChatService jobChatService;
    private final JobServiceHelper jobServiceHelper;
    private final OppNotificationService oppNotificationService;
    private final PartnerService partnerService;
    private final SalesforceBridgeService salesforceBridgeService;
    private final SalesforceConfig salesforceConfig;
    private final SalesforceService salesforceService;
    private final SalesforceJobOppRepository salesforceJobOppRepository;
    private final SalesforceJobOppService salesforceJobOppService;
    private final SavedListService savedListService;
    private final SavedSearchService savedSearchService;
    private final JobOppIntakeService jobOppIntakeService;
    private final NextStepProcessingService nextStepProcessingService;

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
        closingStageLogic = new EnumMap<>(JobOpportunityStage.class);

        //Candidates who have not got to an employed stage before job is closed for any reason
        //are closed with notFitForRole
        Arrays.stream(CandidateOpportunityStage.values())
            .filter(s -> !s.isEmployed() && !s.isClosed())
                .forEach(s -> {
                    // Recruitment doesn't proceed for reasons unrelated to candidate:
                    //      1. Job/employer ineligible
                    addClosingLogic(JobOpportunityStage.ineligibleEmployer,
                        s, CandidateOpportunityStage.jobIneligible);
                    addClosingLogic(JobOpportunityStage.ineligibleRegion,
                        s, CandidateOpportunityStage.jobIneligible);
                    addClosingLogic(JobOpportunityStage.ineligibleOccupation,
                        s, CandidateOpportunityStage.jobIneligible);

                    //      2. Employer issues with process
                    addClosingLogic(JobOpportunityStage.noInterest,
                        s, CandidateOpportunityStage.jobWithdrawn);
                    addClosingLogic(JobOpportunityStage.tooExpensive,
                        s, CandidateOpportunityStage.jobWithdrawn);
                    addClosingLogic(JobOpportunityStage.tooHighWage,
                        s, CandidateOpportunityStage.jobWithdrawn);
                    addClosingLogic(JobOpportunityStage.tooLong,
                        s, CandidateOpportunityStage.jobWithdrawn);

                    // Recruitment doesn't proceed for reasons related to candidate:
                    addClosingLogic(JobOpportunityStage.noSuitableCandidates,
                        s, CandidateOpportunityStage.notFitForRole);
                    addClosingLogic(JobOpportunityStage.noJobOffer,
                        s, CandidateOpportunityStage.notFitForRole);
                    addClosingLogic(JobOpportunityStage.noVisa,
                        s, CandidateOpportunityStage.notFitForRole);
                    addClosingLogic(JobOpportunityStage.hiringCompleted,
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

        SalesforceJobOpp job;

        //Different processing based on different types of job creator
        if (loggedInUserPartner.isDefaultJobCreator()) {
            //Old style (TBB) partner
            //Check if we already have a job for this Salesforce job opp.
            final String sfJoblink = request.getSfJoblink();
            if (sfJoblink == null) {
                throw new InvalidRequestException("Missing link to Salesforce opportunity");
            }
            String sfId = SalesforceHelper.extractIdFromSfUrl(sfJoblink);
            job = salesforceJobOppService.getJobOppById(sfId);
            if (job != null) {
                throw new EntityExistsException("job",
                    job.getName() + " (" + job.getId() + ")");
            }

            //Create job
            job = salesforceJobOppService.createJobOpp(sfId);
            if (job == null) {
                throw new InvalidRequestException(
                    "No such Salesforce opportunity: " + sfJoblink);
            }

            updateJobFromRequest(job, request);

            job.setAuditFields(loggedInUser);

            job.setJobCreator(loggedInUserPartner);

            //Fetch employer corresponding to job's accountId
            Account account = salesforceService.findAccount(job.getAccountId());
            Employer employer = employerService.findOrCreateEmployerFromSalesforceAccount(account);
            //... and use it set the job's country to the employer's country
            job.setCountry(employer.getCountry());

            job = salesforceJobOppRepository.save(job);
        } else if (loggedInUserPartner.getEmployer() != null) {
            //Employer partner
            job = createUpdateJob(loggedInUserPartner.getEmployer(), request);
        } else {
            //todo Eventually Recruiter partner will go here - expecting specification in the
            //request of both the employer associated with the job as well as the name of the role.
            throw new InvalidRequestException(
                "Unsupported type of partner: " + loggedInUserPartner.getName());
        }

        //Create submission list
        final SavedList submissionList = createSubmissionListForJob(job);
        job.setSubmissionList(submissionList);

        String exclusionListName = submissionList.getName() + EXCLUSION_LIST_SUFFIX;

        SavedList exclusionList;
        try {
           //Create exclusion list for the employer (account) associated with this job
           exclusionList = salesforceBridgeService.findSeenCandidates(
               exclusionListName,
               job.getAccountId() != null ? job.getAccountId() : job.getEmployerEntity().getSfId()
           );
        } catch (Exception ex) {
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("CreateJob")
                .message("Could not create exclusion list: " + ex.getMessage())
                .logError(ex);

            UpdateSavedListInfoRequest req = new UpdateSavedListInfoRequest();
            req.setName(exclusionListName);
            exclusionList = savedListService.createSavedList(req);
        }
        job.setExclusionList(exclusionList);

        //If copying an existing job, copy across those fields.
        if (request.getJobToCopyId() != null) {
            copyJobFields(request.getJobToCopyId(), job);
        }

        job = salesforceJobOppRepository.save(job);

        //Create the chats associated with this job
        createJobChats(job);

        return job;
    }

    private void createJobChats(SalesforceJobOpp job) {
        jobChatService.createJobCreatorChat(JobChatType.AllJobCandidates, job);
        jobChatService.createJobCreatorChat(JobChatType.JobCreatorAllSourcePartners, job);

        //Add chats with each source partner
        List<PartnerImpl> sourcePartners = partnerService.listActiveSourcePartners();
        for (PartnerImpl sourcePartner : sourcePartners) {
            jobChatService.createJobCreatorSourcePartnerChat(job, sourcePartner);
        }
    }

    private SavedList createSubmissionListForJob(SalesforceJobOpp job) {
        UpdateSavedListInfoRequest savedListInfoRequest = new UpdateSavedListInfoRequest();
        savedListInfoRequest.setRegisteredJob(true);
        savedListInfoRequest.setSfJobOpp(job);
        SavedList submissionList = savedListService.createSavedList(savedListInfoRequest);
        return submissionList;
    }

    @Override
    public SalesforceJobOpp createUpdateJob(@NonNull Employer employer,
        @NonNull UpdateJobRequest request) throws SalesforceException, WebClientException {
        User loggedInUser = getLoggedInUser("create update job");

        //The partner associated with the person who created the job is the job creator
        final PartnerImpl loggedInUserPartner = loggedInUser.getPartner();
        if (!loggedInUserPartner.isJobCreator()) {
            throw new UnauthorisedActionException("create update job");
        }

        //Check if we already have a job for this Salesforce job opp.
        String sfId = request.getSfId();
        SalesforceJobOpp job = sfId == null ? null : salesforceJobOppService.getJobOppById(sfId);
        boolean create = job == null;
        if (create) {
            //No job exists, create one
            job = new SalesforceJobOpp();
            job.setEmployerEntity(employer);
            job.setCountry(employer.getCountry());
            job.setStage(JobOpportunityStage.prospect);
            job.setNextStep("");
            job.setJobCreator(loggedInUserPartner);
        }

        if (request.getRoleName() != null) {
            job.setName(generateJobName(employer, request.getRoleName()));
        }

        job.setAuditFields(loggedInUser);

        //Update from request
        updateJobFromRequest(job, request);

        //Save job to TC so that it has an id.
        job = salesforceJobOppRepository.save(job);

        //Update SF - set sfId.
        return updateJobOnSalesforce(job);
    }

    private SalesforceJobOpp updateJobOnSalesforce(SalesforceJobOpp job) {
        //Update SF - set sfId.
        String sfId = salesforceService.createOrUpdateJobOpportunity(job);
        job.setSfId(sfId);

        return salesforceJobOppRepository.save(job);
    }

    private static String generateJobName(@NonNull Employer employer, @NonNull String roleName) {
        return employer.getName() + "-" + OffsetDateTime.now().getYear() + "-" + roleName;
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
        SalesforceJobOpp jobOpp = salesforceJobOppRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(SalesforceJobOpp.class, id));
        return checkEmployerEntity(jobOpp);
    }

    /**
     * Checks whether given job has an associated employer entity.
     * If it does, method just returns the job unchanged.
     * If it does not have an employer, it will try and create one from the job's accountId.
     * @param jobOpp Given job
     * @return Job, updated with employer entity matching job's accountId
     * @throws NoSuchObjectException if the job has a null or invalid Salesforce account id.
     */
    @NonNull
    private SalesforceJobOpp checkEmployerEntity(@NonNull SalesforceJobOpp jobOpp)
        throws NoSuchObjectException {
        if (jobOpp.getEmployerEntity() == null) {
            String accountId = jobOpp.getAccountId();
            if (accountId == null) {
                throw new NoSuchObjectException("Job " + jobOpp.getId() + " has null accountId");
            }
            //Find or create employer for this account.
            Employer employer = employerService.findOrCreateEmployerFromSalesforceId(accountId);
            jobOpp.setEmployerEntity(employer);
            jobOpp = salesforceJobOppRepository.save(jobOpp);
        }
        return jobOpp;
    }

    private void checkEmployerEntities(Iterable<SalesforceJobOpp> jobs) {
        for (SalesforceJobOpp job : jobs) {
            try {
                checkEmployerEntity(job);
            } catch (NoSuchObjectException ex) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("CheckEmployerEntities")
                    .message("Could not create employer for job " + job.getId())
                    .logError(ex);
            }
        }
    }

    @Override
    public Page<SalesforceJobOpp> searchJobs(SearchJobRequest request) {
        User loggedInUser = userService.getLoggedInUser();
        Page<SalesforceJobOpp> jobs = salesforceJobOppRepository.findAll(
            JobSpecification.buildSearchQuery(request, loggedInUser),
            request.getPageRequest());
        checkEmployerEntities(jobs);
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
        request.setJobId(id);
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

        jobInfo.setSfJobLink(SalesforceHelper.sfOppIdToLink(job.getSfId(), salesforceConfig.getBaseLightningUrl()));

        jobInfo.setTcJobLink(tcJobLink);

        return jobInfo;
    }

    @Async
    @Override
    public void createEmployerForAllJobs() {
        List<SalesforceJobOpp> jobs = salesforceJobOppRepository.findAll();
        checkEmployerEntities(jobs);
    }

    @Async
    @Override
    public void loadJobOppsAndCandidateOpps() {

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("LoadJobOppsAndCandidateOpps")
            .message("Loading candidate opportunities from Salesforce")
            .logInfo();

        final int limit = 100;

        String lastId = null;
        int totalOpps = 0;
        int nOpps = -1;
        while (nOpps != 0) {

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("LoadJobOppsAndCandidateOpps")
                .message("Attempting to load up to " + limit + " opps from " + (lastId == null ? "start" : lastId))
                .logInfo();

            List<Opportunity> ops = salesforceService.findCandidateOpportunities(
                lastId == null ? null : "Id > '" + lastId + "'", limit);
            nOpps = ops.size();
            totalOpps += nOpps;

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("LoadJobOppsAndCandidateOpps")
                .message("Loaded " + nOpps + " candidate opportunities from Salesforce. Total " + totalOpps)
                .logInfo();

            if (nOpps > 0) {
                lastId = ops.get(nOpps - 1).getId();

                for (Opportunity op : ops) {
                    String jobOppId = op.getParentOpportunityId();
                    if (jobOppId == null) {
                        LogBuilder.builder(log)
                            .user(authService.getLoggedInUser())
                            .action("LoadJobOppsAndCandidateOpps")
                            .message("Candidate opportunity without parent job opp: " + op.getName())
                            .logWarn();
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
            //Current stage is before CandidateSearch so update it

            //New stage will depend on skipCandidateSearch
            JobOpportunityStage nextStage =
                job.isSkipCandidateSearch() ? JobOpportunityStage.visaEligibility :
                    JobOpportunityStage.candidateSearch;
            job.setStage(nextStage);

            //Update Salesforce stage to match - setting Next Step and Due date
            final LocalDate submissionDueDate = job.getSubmissionDueDate();

            //Next step
            final String nowDate = nextStepDateFormat.format(LocalDateTime.now());
            final String nextStep = nowDate + ": Waiting to receive candidate CVs for review";

            salesforceService.updateEmployerOpportunityStage(
                job, JobOpportunityStage.candidateSearch, nextStep, submissionDueDate
            );
        }

        job.setPublishedBy(loggedInUser);
        job.setPublishedDate(OffsetDateTime.now());

        //Save non-empty submission list to suggested list.
        SavedList submissionList = job.getSubmissionList();
        if (!submissionList.getCandidates().isEmpty()) {

            CopySourceContentsRequest request = new CopySourceContentsRequest();
            request.setSavedListId(0L);
            request.setNewListName(submissionList.getName() + "-suggest");
            request.setJobId(id);
            //Copy to the target list.
            SavedList suggestedList = candidateSavedListService.copy(submissionList, request);
            job.setSuggestedList(suggestedList);
        }

        // Send out notifications
        oppNotificationService.notifyNewJobOpp(job);

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
    public List<Long> findUnreadChatsInOpps(SearchJobRequest request) {
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            throw new InvalidSessionException("Not logged in");
        }

        //Construct query
        final Specification<SalesforceJobOpp> spec =
            JobSpecification.buildSearchQuery(request, loggedInUser);

        //Retrieve all results and gather the ids
        List<SalesforceJobOpp> allOpps = salesforceJobOppRepository.findAll(spec);
        List<Long> oppIds = allOpps.stream().map(SalesforceJobOpp::getId).toList();
        List<Long> unreadChatIds =
            salesforceJobOppRepository.findUnreadChatsInOpps(loggedInUser.getId(), oppIds);
        return unreadChatIds;
    }

    @Override
    public List<SalesforceJobOpp> searchJobsUnpaged(SearchJobRequest request) {
        User loggedInUser = userService.getLoggedInUser();
        List<SalesforceJobOpp> jobs = salesforceJobOppRepository.findAll(
            JobSpecification.buildSearchQuery(request, loggedInUser));
        checkEmployerEntities(jobs);
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
    @NonNull
    public SalesforceJobOpp updateInterviewGuidanceLink(long id, UpdateLinkRequest updateLinkRequest)
        throws InvalidRequestException, NoSuchObjectException {
        User loggedInUser = getLoggedInUser("update interview guidance file");

        SalesforceJobOpp job = getJob(id);
        if (job.getSubmissionList() == null) {
            throw new InvalidRequestException("Job " + id + " does not have submission list");
        }
        setJobInterviewGuidanceLink(job, updateLinkRequest.getName(), updateLinkRequest.getUrl());

        job.setAuditFields(loggedInUser);

        return salesforceJobOppRepository.save(job);
    }

    @Override
    @NonNull
    public SalesforceJobOpp updateMouLink(long id, UpdateLinkRequest updateLinkRequest)
            throws InvalidRequestException, NoSuchObjectException {
        User loggedInUser = getLoggedInUser("update interview guidance file");

        SalesforceJobOpp job = getJob(id);
        if (job.getSubmissionList() == null) {
            throw new InvalidRequestException("Job " + id + " does not have submission list");
        }
        setJobMouLink(job, updateLinkRequest.getName(), updateLinkRequest.getUrl());

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

        //Perform any notifications before actually applying the change so that we have the
        //old and current state
        oppNotificationService.notifyJobOppChanges(job, request);

        final String nextStep = request.getNextStep();
        if (nextStep != null) {
            final String processedNextStep =
                nextStepProcessingService.processNextStep(job, nextStep);

            job.setNextStep(processedNextStep);
        }

        final Boolean evergreen = request.getEvergreen();
        if (evergreen != null) {
            job.setEvergreen(evergreen);
        }

        final Boolean skipCandidateSearch = request.getSkipCandidateSearch();
        if (skipCandidateSearch != null) {
            job.setSkipCandidateSearch(skipCandidateSearch);
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

        //Do stage change last - changing stage may spawn a new evergreen opp - and we
        //want to copy other updated changed fields.
        final JobOpportunityStage stage = request.getStage();
        if (stage != null) {
            changeJobStage(job, stage);
        }

        final String newJobName = request.getJobName();
        if (newJobName != null) {
            String oldJobName = job.getName();
            job.setName(newJobName);
            savedSearchService.updateSuggestedSearchesNames(job, oldJobName);
            savedListService.updateAssociatedListsNames(job);
        }
    }

    /**
     * All changes to a job stage should go through here.
     * <p/>
     * This allows us to kick off automated actions that are triggered by a stage change.
     * @param job Job who stage is changing
     * @param stage New stage
     */
    private void changeJobStage(SalesforceJobOpp job, JobOpportunityStage stage) {
        job.setStage(stage);

        //Do automation logic
        if (stage.isClosed()) {
            closeUnclosedCandidateOppsForJob(job, stage);
        }

        if (job.isEvergreen()) {
            //Once an evergreen job enters the Recruitment stage, it spawns another copy of the
            //job in CandidateSearch.
            if (job.getEvergreenChild() == null) {
                if (stage.compareTo(JobOpportunityStage.recruitmentProcess) >= 0) {
                    SalesforceJobOpp evergreenChild = spawnEvergreenChildOpp(job);
                    job.setEvergreenChild(evergreenChild);
                }
            }
        }
    }

    private SalesforceJobOpp spawnEvergreenChildOpp(SalesforceJobOpp job) {
        SalesforceJobOpp child = new SalesforceJobOpp();

        //Set audit fields using creating user
        child.setAuditFields(job.getCreatedBy());

        child.setAccountId(job.getAccountId());

        //Do not copy candidate opportunities

        child.setContactUser(job.getContactUser());
        child.setCountry(job.getCountry());
        child.setDescription(job.getDescription());
        child.setEmployer(job.getEmployer());
        child.setEmployerEntity(job.getEmployerEntity());
        child.setEvergreen(job.isEvergreen());

        //Do not copy evergreenChild

        child.setExclusionList(job.getExclusionList());
        child.setJobSummary(job.getJobSummary());

        //Generate new name from original name
        child.setName(generateChildName(job.getName()));

        child.setOwnerId(job.getOwnerId());
        child.setPublishedBy(job.getPublishedBy());
        child.setPublishedDate(job.getPublishedDate());
        child.setJobCreator(job.getJobCreator());

        //Do not copy stage. Child starts in candidateSearch
        child.setStage(JobOpportunityStage.candidateSearch);

        //Do not copy starring users
        //Do not copy submissionDueDate - typically not used for evergreen jobs

        //Do not use suggested list

        //Copy across suggested searches
        child.setSuggestedSearches(new HashSet<>(job.getSuggestedSearches()));

        //Need to duplicate jobOppIntake - 1-1 can't be shared, may change
        child.setJobOppIntake(jobOppIntakeService.create(job.getJobOppIntake()));

        child.setHiringCommitment(job.getHiringCommitment());
        child.setEmployerWebsite(job.getEmployerWebsite());
        child.setEmployerHiredInternationally(job.getEmployerHiredInternationally());
        child.setOpportunityScore(job.getOpportunityScore());
        child.setEmployerDescription(job.getEmployerDescription());

        //Save child job before setting submission list on it
        child = salesforceJobOppRepository.save(child);

        //Child has its own new submission list
        final SavedList childSubmissionList = createSubmissionListForJob(child);

        //Copy Job Description and interview guidance fields across from existing submission list
        SavedList jobSubmissionList = job.getSubmissionList();
        if (jobSubmissionList != null) {
            childSubmissionList.setFileJdLink(jobSubmissionList.getFileJdLink());
            childSubmissionList.setFileJdName(jobSubmissionList.getFileJdName());
            childSubmissionList.setFileInterviewGuidanceLink(
                jobSubmissionList.getFileInterviewGuidanceLink());
            childSubmissionList.setFileInterviewGuidanceName(
                jobSubmissionList.getFileInterviewGuidanceName());
        }
        child.setSubmissionList(childSubmissionList);

        child = updateJobOnSalesforce(child);

        createJobChats(child);

        return child;
    }

    String generateChildName(String parentJobName) {
        return jobServiceHelper.generateNextEvergreenJobName(parentJobName);
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
        salesforceService.updateEmployerOpportunityStage(job, stage, nextStep, nextStepDueDate);

        final String jobName = request.getJobName();
        if (jobName != null) {
            salesforceService.updateEmployerOpportunityName(job.getSfId(), jobName);
        }

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

        if (!activeOpps.isEmpty()) {
            if (closingStageLogic == null) {
                initialiseClosingCandidateStageLogic();
            }

            final EnumMap<CandidateOpportunityStage, CandidateOpportunityStage>
                currentToClosingStageMap = closingStageLogic.get(jobCloseStage);

            if (currentToClosingStageMap == null) {
                //Log no closing logic
                String errorMessage = "No closing logic for job stage " + jobCloseStage +
                    " of opportunity " + job.getName() + " (" + job.getId() + ")";

                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("CloseUnclosedCandidateOppsForJob")
                    .message(errorMessage)
                    .logError();

                emailHelper.sendAlert(errorMessage);
            } else {
                for (CandidateOpportunity activeOpp : activeOpps) {
                    CandidateOpportunityStage closingStage = currentToClosingStageMap.get(
                        activeOpp.getStage());
                    if (closingStage == null) {
                        //Missing logic
                        LogBuilder.builder(log)
                            .user(authService.getLoggedInUser())
                            .action("CloseUnclosedCandidateOppsForJob")
                            .message("Closing logic missing case for job closing stage " + jobCloseStage +
                                " and candidate in stage " + activeOpp.getStage())
                            .logWarn();

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
                    params.setClosingComments(
                        "Job opportunity closed: " + jobCloseStage.toString());

                    candidateOpportunityService.createUpdateCandidateOpportunities(
                        stageListEntry.getValue(), job, params);
                }

                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("CloseUnclosedCandidateOppsForJob")
                    .message("Closed opps for candidates going for job  " + job.getId() + ": "
                        + activeOpps.stream().map(opp -> opp.getCandidate().getCandidateNumber())
                        .collect(Collectors.joining(",")))
                    .logInfo();
            }
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

    /**
     * Fetches all open SF Jobs on the TC DB and supplies them to {@link #syncOpenJobsFromSf(List)}
     * where they are combined with open jobs from SF whose stage has recently changed. The
     * resulting collection is used to update the TC opps with values from their SF equivalents, if
     * changes have been made there.
     *
     * <p>While that would seem to favour changes made on SF, that is not the outcome: because the
     * TC updates SF opps in real-time, most SF records contain TC data - in which cases no update
     * will be performed. It is not the preferred user behaviour for updates to be made on SF, but
     * until safeguards are put in place, this scheduled update (daily at 1am GMT) keeps TC data as
     * accurate as possible.
     */
    @Transactional
    @Scheduled(cron = "0 0 1 * * ?", zone = "GMT")
    @SchedulerLock(name = "JobOppSyncFromSalesforce", lockAtLeastFor = "PT23H",
        lockAtMostFor = "PT23H")
    @Override
    public void initiateOpenJobSyncFromSf() {
        try {
            // Find all open Jobs on TC
            SearchJobRequest request = new SearchJobRequest();
            request.setSfOppClosed(false);

            List<SalesforceJobOpp> jobs = searchJobsUnpaged(request);

            // Populate List with their SF IDs
            List<String> sfIds = jobs.stream()
                .map(SalesforceJobOpp::getSfId)
                .collect(Collectors.toList());

            // Now update them from their Salesforce equivalent, if changes made there
            syncOpenJobsFromSf(sfIds);
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("JobOppSyncFromSalesforce")
                .message("Failed to update open Jobs from Salesforce equivalents")
                .logError(e);
        }
    }

    /**
     * Updates TC Jobs corresponding to the given SF IDs, if the SF equivalent has new changes.
     * <p/>
     * Will also update TC Jobs open on SF even if not provided, which is useful for Jobs which have
     * been closed on the TC and then are reopened on SF. Typically, we only pass in IDs of local
     * Jobs that are open in order to limit the total number of IDs. Otherwise, the total number of
     * IDs being passed in will grow infinitely over time (and might eventually crash the SF link by
     * making the SF query too long). This way the id's can be limited to open job opps only -
     * ignoring closed opps. This approach ensures that closed opps can be reopened as a result of
     * this update.
     *
     * @param sfIds Salesforce IDs of cache records to be updated from Salesforce
     * @throws SalesforceException if there are issues contacting Salesforce
     */
    private void syncOpenJobsFromSf(List<String> sfIds) throws SalesforceException {
        if (sfIds != null && !sfIds.isEmpty()) {
            LogBuilder.builder(log)
                .action("JobServiceImpl_syncOpenJobsFromSf")
                .message("Updating open Job Opportunities from Salesforce")
                .logInfo();

            // Fetch Salesforce equivalents in batches - because there's a 4,000-character limit on
            // single strings in a SOQL WHERE clause, which the concatenated sfIds might exceed.
            int totalItems = sfIds.size();
            int batchSize = 100;

            // Fetch from Salesforce any potentially reopened Opps and use to initialise list of
            // sfOpps to be used for potential updates.
            List<Opportunity> potentialReopenedOpps =
                salesforceService.fetchOpportunitiesByOpenOnSF(OpportunityType.JOB);
            List<Opportunity> sfOpps = new ArrayList<>(potentialReopenedOpps);

            // To avoid duplicates in the next step, create Set of SF IDs for comparison
            Set<String> sfOppIds = sfOpps.stream()
                .map(Opportunity::getId)
                .collect(Collectors.toSet());

            for (int i = 0; i < totalItems; i += batchSize) {
                List<String> batch =
                    new ArrayList<>(sfIds.subList(i, Math.min(i + batchSize, totalItems)));

                // Remove any ID that exists in the reopenedOppIds Set
                batch.removeIf(sfOppIds::contains);

                if (!batch.isEmpty()) {
                    sfOpps.addAll(
                        salesforceService.fetchOpportunitiesById(batch, OpportunityType.JOB)
                    );
                }
            }

            LogBuilder.builder(log)
                .action("UpdateJobs")
                .message("Loaded " + sfOpps.size() + " job opportunities from Salesforce")
                .logInfo();

            processOpenJobSyncBatch(sfOpps);
        }
    }

    /**
     * Takes a List containing objects of type {@link Opportunity}, populated by Salesforce Job Opps,
     * and updates their TC equivalent if changes have been made on Salesforce.
     * @param sfOpps List of {@link Opportunity} to update from
     */
    private void processOpenJobSyncBatch(List<Opportunity> sfOpps) {
        int count = 0;
        int updates = 0;

        for (Opportunity sfOpp : sfOpps) {
            try {
                String id = sfOpp.getId();
                //Fetch DB with id
                SalesforceJobOpp tcOpp = salesforceJobOppRepository.findBySfId(id)
                    .orElse(null);
                if (tcOpp != null) {
                    UpdateJobRequest updateRequest =
                        extractUpdateJobRequestFromSalesforceOpp(sfOpp, tcOpp);
                    if (updateRequest != null) {
                        updateJobFromRequest(tcOpp, updateRequest);
                        salesforceJobOppRepository.save(tcOpp);
                        updates++;
                    }
                }
                count++;

                if ((count % 100 == 0) || count == sfOpps.size()) {
                    LogBuilder.builder(log)
                        .action("Sync Open Jobs From Salesforce")
                        .message("Processed " + count + " Job Opps from Salesforce, of which "
                            + updates + " led to update of TC equivalent.")
                        .logInfo();
                }

            } catch (Exception e) {
                LogBuilder.builder(log)
                    .action("Process open job sync batch")
                    .message("Failed to process job with SF ID " + sfOpp.getId() + ". Error: " +
                        e.getMessage())
                    .logError(e);
            }
        }
    }

    /**
     * Creates {@link UpdateJobRequest} from a Salesforce Job Opportunity. Only adds a value if the
     * SF version is non-null and different to the TC version. Returns null if no values added.
     * @param sfOpp SF Opp from which oppParams will be extracted
     * @param tcOpp TC equivalent to which it will be compared
     * @return {@link UpdateJobRequest} if any values added, otherwise null
     */
    private @Nullable UpdateJobRequest extractUpdateJobRequestFromSalesforceOpp(
        @NonNull Opportunity sfOpp,
        @NonNull SalesforceJobOpp tcOpp
    ) {
        UpdateJobRequest request = new UpdateJobRequest();
        int changes = 0;

        // NEXT STEP
        // Change only if SF value is non-null and user-entered value different to TC version
        if (sfOpp.getNextStep() != null) {
            if (isNextStepDifferent(tcOpp.getNextStep(), sfOpp.getNextStep())) {
                request.setNextStep(sfOpp.getNextStep());
                changes++;
            }
        }

        // NEXT STEP DUE DATE
        final String nextStepDueDate = sfOpp.getNextStepDueDate();
        if (nextStepDueDate != null) {
            try {
                LocalDate parsedDate = LocalDate.parse(nextStepDueDate);
                if (!Objects.equals(tcOpp.getNextStepDueDate(), parsedDate)) {
                    request.setNextStepDueDate(parsedDate);
                    changes++;
                }
            } catch (DateTimeParseException ex) {
                LogBuilder.builder(log)
                    .action("extractUpdateJobRequestFromSalesforceOpp")
                    .message("Error decoding nextStepDueDate: " + nextStepDueDate +
                        " in Job Opp from Salesforce: " + sfOpp.getName())
                    .logError();
            }
        }

        // STAGE
        try {
            JobOpportunityStage stage = JobOpportunityStage.textToEnum(sfOpp.getStageName());
            if (tcOpp.getStage() != stage) {
                request.setStage(stage);
                changes++;
            }
        } catch (IllegalArgumentException e) {
            LogBuilder.builder(log)
                .action("extractUpdateJobRequestFromSalesforceOpp")
                .message("Error decoding stage: " + sfOpp.getStageName() +
                    " in Job Opp from Salesforce: " + sfOpp.getName())
                .logError();
        }

        return changes > 0 ? request : null;
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

    private void setJobInterviewGuidanceLink(SalesforceJobOpp job, String name, String url) {
        SavedList submissionList = job.getSubmissionList();
        submissionList.setFileInterviewGuidanceLink(url);
        submissionList.setFileInterviewGuidanceName(name);
        savedListService.saveIt(submissionList);
    }

    private void setJobMouLink(SalesforceJobOpp job, String name, String url) {
        SavedList submissionList = job.getSubmissionList();
        submissionList.setFileMouLink(url);
        submissionList.setFileMouName(name);
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
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("UploadFile")
                .message("Failed to delete temporary file " + tempFile)
                .logError();
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

    @Override
    public SalesforceJobOpp uploadInterviewGuidance(long id, MultipartFile file)
            throws InvalidRequestException, NoSuchObjectException, IOException {

        SalesforceJobOpp job = getJob(id);
        if (job.getSubmissionList() == null) {
            throw new InvalidRequestException("Job " + id + " does not have submission list");
        }
        GoogleFileSystemFile uploadedFile = uploadJobFile(job, file);
        setJobInterviewGuidanceLink(job, uploadedFile.getName(), uploadedFile.getUrl());
        return job;
    }

    @Override
    public SalesforceJobOpp uploadMou(long id, MultipartFile file)
            throws InvalidRequestException, NoSuchObjectException, IOException {

        SalesforceJobOpp job = getJob(id);
        if (job.getSubmissionList() == null) {
            throw new InvalidRequestException("Job " + id + " does not have submission list");
        }
        GoogleFileSystemFile uploadedFile = uploadJobFile(job, file);
        setJobMouLink(job, uploadedFile.getName(), uploadedFile.getUrl());
        return job;
    }

    /**
     * When creating a job, a user can select to copy from existing job.
     * This will create a new job but also copy the
     * following from the job to copy:
     * - Job uploads
     * - Job summary
     * - JOI fields
     * @param jobToCopyId id of selected job to copy from.
     * @param job job that is being created and fields are being copied across to.
     */
    private void copyJobFields(long jobToCopyId, SalesforceJobOpp job) {
        SalesforceJobOpp jobToCopy = salesforceJobOppService.getJobOpp(jobToCopyId);

        // Copy job summary
        job.setJobSummary(jobToCopy.getJobSummary());

        // Copy JOI data
        if (jobToCopy.getJobOppIntake() != null) {
            JobOppIntake copiedIntake = jobOppIntakeService.create(jobToCopy.getJobOppIntake());
            job.setJobOppIntake(copiedIntake);
        }

        // Copy all associated job uploads
        SavedList submissionListToCopy = jobToCopy.getSubmissionList();
        SavedList submissionList = job.getSubmissionList();

        // Copy JD file
        submissionList.setFileJdLink(submissionListToCopy.getFileJdLink());
        submissionList.setFileJdName(submissionListToCopy.getFileJdName());
        // Copy JOI file
        submissionList.setFileJoiLink(submissionListToCopy.getFileJoiLink());
        submissionList.setFileJoiName(submissionListToCopy.getFileJoiName());
        // Copy MOU file
        submissionList.setFileMouLink(submissionListToCopy.getFileMouLink());
        submissionList.setFileMouName(submissionListToCopy.getFileMouName());
        // Copy Interview Guidance file
        submissionList.setFileInterviewGuidanceLink(submissionListToCopy.getFileInterviewGuidanceLink());
        submissionList.setFileInterviewGuidanceName(submissionListToCopy.getFileInterviewGuidanceName());
    }
}
