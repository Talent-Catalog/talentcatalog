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

package org.tctalent.server.api.admin;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.api.services.drive.model.FileList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.configuration.SalesforceConfig;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.Environment;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.CandidateNote;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.EducationType;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.NoteType;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.model.sf.Contact;
import org.tctalent.server.repository.db.CandidateAttachmentRepository;
import org.tctalent.server.repository.db.CandidateNoteRepository;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.ChatPostRepository;
import org.tctalent.server.repository.db.JobChatRepository;
import org.tctalent.server.repository.db.JobChatUserRepository;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.SavedSearchRepository;
import org.tctalent.server.request.candidate.SavedListGetRequest;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.job.SearchJobRequest;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.response.DuolingoDashboardResponse;
import org.tctalent.server.response.DuolingoVerifyScoreResponse;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.BackgroundProcessingService;
import org.tctalent.server.service.db.CandidateOppBackgroundProcessingService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.DataSharingService;
import org.tctalent.server.service.db.DuolingoApiService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.JobService;
import org.tctalent.server.service.db.LanguageService;
import org.tctalent.server.service.db.NotificationService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.PopulateElasticsearchService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.aws.S3ResourceHelper;
import org.tctalent.server.service.db.cache.CacheService;
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.BackRunner;
import org.tctalent.server.util.background.PageContext;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;
import org.tctalent.server.util.textExtract.TextExtractHelper;

@RestController
@RequestMapping("/api/admin/system")
@Slf4j
public class SystemAdminApi {
    final static String DATE_FORMAT = "dd-MM-yyyy";

    private final AuthService authService;

    private final DataSharingService dataSharingService;

    private final CandidateAttachmentRepository candidateAttachmentRepository;
    private final CandidateNoteRepository candidateNoteRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateOpportunityRepository candidateOpportunityRepository;
    private final CandidateOpportunityService candidateOpportunityService;
    private final CandidateService candidateService;
    private final CountryService countryService;
    private final FileSystemService fileSystemService;
    private final JobService jobService;
    private final LanguageService languageService;
    private final NotificationService notificationService;
    private final PopulateElasticsearchService populateElasticsearchService;
    private final SalesforceService salesforceService;
    private final SalesforceConfig salesforceConfig;
    private final SavedListRepository savedListRepository;
    private final SalesforceJobOppRepository salesforceJobOppRepository;
    private final SavedListService savedListService;
    private final SavedSearchRepository savedSearchRepository;
    private final JobChatRepository jobChatRepository;
    private final JobChatUserRepository jobChatUserRepository;
    private final ChatPostRepository chatPostRepository;
    private final S3ResourceHelper s3ResourceHelper;
    private final CacheService cacheService;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private final Map<Integer, Integer> countryForGeneralCountry;

    private final GoogleDriveConfig googleDriveConfig;
    private final TaskScheduler taskScheduler;
    private final BackgroundProcessingService backgroundProcessingService;
    private final SavedSearchService savedSearchService;
    private final PartnerService partnerService;
    private final CandidateOppBackgroundProcessingService candidateOppBackgroundProcessingService;

    @Value("${spring.datasource.url}")
    private String targetJdbcUrl;

    @Value("${spring.datasource.username}")
    private String targetUser;

    @Value("${spring.datasource.password}")
    private String targetPwd;

    @Value("${google.drive.candidateDataDriveId}")
    private String candidateDataDriveId;

    @Value("${google.drive.candidateRootFolderId}")
    private String candidateRootFolderId;

    @Value("${google.drive.listFoldersDriveId}")
    private String listFoldersDriveId;

    @Value("${google.drive.listFoldersRootId}")
    private String listFoldersRootId;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${environment}")
    private String environment;
    private final DuolingoApiService duolingoApiService;

    @Autowired
    public SystemAdminApi(
            DataSharingService dataSharingService,
            AuthService authService,
            CandidateAttachmentRepository candidateAttachmentRepository,
            CandidateNoteRepository candidateNoteRepository,
            CandidateRepository candidateRepository,
        CandidateOpportunityRepository candidateOpportunityRepository,
            CandidateOpportunityService candidateOpportunityService, CandidateService candidateService,
            CountryService countryService,
            FileSystemService fileSystemService,
            JobService jobService, LanguageService languageService,
        NotificationService notificationService,
            PopulateElasticsearchService populateElasticsearchService,
            SalesforceService salesforceService,
            SalesforceConfig salesforceConfig, SalesforceJobOppRepository salesforceJobOppRepository, SavedListService savedListService,
            SavedListRepository savedListRepository,
            JobChatRepository jobChatRepository, JobChatUserRepository jobChatUserRepository, ChatPostRepository chatPostRepository,
            SavedSearchRepository savedSearchRepository, S3ResourceHelper s3ResourceHelper,
            GoogleDriveConfig googleDriveConfig, CacheService cacheService,
        TaskScheduler taskScheduler, BackgroundProcessingService backgroundProcessingService,
        SavedSearchService savedSearchService, PartnerService partnerService,
        CandidateOppBackgroundProcessingService candidateOppBackgroundProcessingService, DuolingoApiService duolingoApiService
        ) {
        this.dataSharingService = dataSharingService;
        this.authService = authService;
        this.candidateAttachmentRepository = candidateAttachmentRepository;
        this.candidateNoteRepository = candidateNoteRepository;
        this.candidateRepository = candidateRepository;
        this.candidateOpportunityRepository = candidateOpportunityRepository;
        this.candidateOpportunityService = candidateOpportunityService;
        this.candidateService = candidateService;
        this.countryService = countryService;
        this.fileSystemService = fileSystemService;
        this.jobService = jobService;
        this.languageService = languageService;
        this.notificationService = notificationService;
        this.populateElasticsearchService = populateElasticsearchService;
        this.salesforceService = salesforceService;
        this.salesforceConfig = salesforceConfig;
        this.savedListRepository = savedListRepository;
        this.salesforceJobOppRepository = salesforceJobOppRepository;
        this.savedListService = savedListService;
        this.savedSearchRepository = savedSearchRepository;
        this.jobChatRepository = jobChatRepository;
        this.jobChatUserRepository = jobChatUserRepository;
        this.chatPostRepository = chatPostRepository;
        this.s3ResourceHelper = s3ResourceHelper;
        this.googleDriveConfig = googleDriveConfig;
        this.cacheService = cacheService;
        this.taskScheduler = taskScheduler;
      this.backgroundProcessingService = backgroundProcessingService;
      this.savedSearchService = savedSearchService;
      this.partnerService = partnerService;
      this.candidateOppBackgroundProcessingService = candidateOppBackgroundProcessingService;
      countryForGeneralCountry = getExtraCountryMappings();
      this.duolingoApiService = duolingoApiService;
    }

    @GetMapping("set_public_ids")
    public void setPublicIds() {
        backgroundProcessingService.setCandidatePublicIds();
        backgroundProcessingService.setSavedListPublicIds();
        backgroundProcessingService.setSavedSearchPublicIds();
    }

    @GetMapping("fix_null_case_sfids")
    public void fixNullCaseSfids() {
        List<CandidateOpportunity> opps = candidateOpportunityRepository.findAllBySfIdIsNull();
        for (CandidateOpportunity opp : opps) {
            String sfid = candidateOpportunityService.fetchSalesforceId(opp);
            if (sfid == null) {
                LogBuilder.builder(log)
                    .caseId(opp.getId())
                    .user(authService.getLoggedInUser())
                    .action("FixNullCaseSfids")
                    .message("No Salesforce opp for case " + opp.getId())
                    .logInfo();
            } else {
                opp.setSfId(sfid);
                candidateOpportunityRepository.save(opp);
                LogBuilder.builder(log)
                    .caseId(opp.getId())
                    .user(authService.getLoggedInUser())
                    .action("FixNullCaseSfids")
                    .message("Updated Sfid of case " + opp.getId())
                    .logInfo();
            }
        }
    }
    /**
     * Fetches Duolingo dashboard results based on optional date filters.
     *
     * @param minDateTime Optional start date and time in 'yyyy-MM-ddTHH:mm:ss' format.
     * @param maxDateTime Optional end date and time in 'yyyy-MM-ddTHH:mm:ss' format.
     * @return A list of duolingo dashboard results within the specified date range.
     */
    @GetMapping("duolingo/dashboard-results")
    public List<DuolingoDashboardResponse> fetchDashboardResults(
        @RequestParam(required = false) String minDateTime,
        @RequestParam(required = false) String maxDateTime) {

        // Parse the minDateTime and maxDateTime only if they are provided
        LocalDateTime min = (minDateTime != null) ? LocalDateTime.parse(minDateTime) : null;
        LocalDateTime max = (maxDateTime != null) ? LocalDateTime.parse(maxDateTime) : null;

        // Pass the parsed min and max values to the service method
        return duolingoApiService.getDashboardResults(min, max);
    }

    /**
     * Verifies a Duolingo score using the provided certificate ID and birthdate.
     *
     * @param certificateId The ID of the certificate to verify.
     * @param birthdate The birthdate of the certificate holder in 'yyyy-MM-dd' format.
     * @return A response containing the score verification details.
     */
    @GetMapping("duolingo/verify-score")
    public DuolingoVerifyScoreResponse verifyScore(
        @RequestParam String certificateId,
        @RequestParam String birthdate) {
        return duolingoApiService.verifyScore(certificateId, birthdate);
    }


    @GetMapping("flush_user_cache")
    public void flushUserCache() {
        cacheService.flushUserCache();
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("FlushUserCache")
            .message("User cache flushed")
            .logInfo();
    }

    @GetMapping("notifyOfChatsWithNewPosts")
    public void notifyOfNewChatPosts() {
        notificationService.notifyUsersOfChatsWithNewPosts();
    }

    /**
     * This loads the last active stages of all cases from Salesforce.
     */
    @GetMapping("load_case_last_active_stages")
    public void loadCandidateOpportunityLastActiveStages() {
        candidateOpportunityService.loadCandidateOpportunityLastActiveStages();
    }


    @GetMapping("create_employer_for_all_jobs")
    public void createEmployerForAllJobs() {
        jobService.createEmployerForAllJobs();
    }

    /**
     * This loads ALL historical data of jobs which had candidates - creating jobs and cases
     * on the TC as needed.
     */
    @GetMapping("load_job_opps_and_candidate_opps")
    public void loadJobOppsAndCandidateOpps() {
        jobService.loadJobOppsAndCandidateOpps();
    }

    @GetMapping("close_candidate_ops_for_closed_jobs")
    public void closeCandidateOpportunitiesForClosedJobs() {
        //Get all closed job opps
        SearchJobRequest request = new SearchJobRequest();
        request.setSfOppClosed(true);
        final List<SalesforceJobOpp> jobOpps = jobService.searchJobsUnpaged(request);

        //For each closed job opp, closed it again (with the same closed stage).
        //This will trigger the automatic closing of associated candidate opps.
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("CloseCandidateOpportunitiesForClosedJobs")
            .message("Processing " + jobOpps.size() + " closed jobs")
            .logInfo();

        int count = 0;
        for (SalesforceJobOpp jobOpp : jobOpps) {
            UpdateJobRequest updateJobRequest = new UpdateJobRequest();
            updateJobRequest.setStage(jobOpp.getStage());
            try {
                jobService.updateJob(jobOpp.getId(), updateJobRequest);
            } catch (Exception e) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("CloseCandidateOpportunitiesForClosedJobs")
                    .message("Exception thrown updating job " + jobOpp.getId())
                    .logWarn(e);
            }
            count++;
            if (count%20 == 0) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("CloseCandidateOpportunitiesForClosedJobs")
                    .message("Processed " + count)
                    .logInfo();
            }
        }
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("CloseCandidateOpportunitiesForClosedJobs")
            .message("Checked that cases are closed for " + jobOpps.size() + " closed jobs")
            .logInfo();
    }

  // This method was written for the initial migration of opps from SF to the TC. It may have some
  // future utility so is only commented out for the time being, but it should only be used again
  // advisedly and probably with some modification for the current purpose.
//    @GetMapping("load_candidate_ops")
//    public void loadCandidateOpportunities() {
//        //Get all job opps known to TC
//        List<SavedList> listsWithJobs = savedListService.findListsAssociatedWithJobs();
//
//        //Extract their distinct ids
//        String[] jobIds = listsWithJobs.stream()
//            .filter(sl -> sl.getSfJobOpp() != null)
//            .map(sl -> sl.getSfJobOpp().getSfId())
//            .distinct()
//            .toArray(String[]::new);
//        candidateOpportunityService.loadCandidateOpportunities(jobIds);
//    }

    @GetMapping("sf-sync-open-jobs")
    ResponseEntity<?> sfSyncOpenJobs() {
      try {
        LogBuilder.builder(log)
            .action("Sync Open Jobs From SF")
            .message("Manually triggered")
            .logInfo();

        jobService.initiateOpenJobSyncFromSf();

        return ResponseEntity.ok().build(); // Return 200 OK - front-end will display 'Done'
      } catch(Exception e) {
        LogBuilder.builder(log)
            .action("Sync Open Jobs From SF")
            .message("Manually triggered operation failed")
            .logInfo();

        // Return 500 Internal Server Error including error in body for display on frontend
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
      }
    }

  /**
   * Manual trigger for method that updates TC Candidate Opportunities from their Salesforce
   * equivalents.
   * @return ResponseEntity indicating success or failure
   */
  @GetMapping("sf-sync-open-cases")
  ResponseEntity<?> sfSyncOpenCases() {
    try {
      LogBuilder.builder(log)
          .action("Sync Open Cases From SF")
          .message("Manually triggered")
          .logInfo();

      candidateOppBackgroundProcessingService.initiateBackgroundCaseUpdate();

      return ResponseEntity.ok().build(); // Return 200 OK - front-end will display 'Done'

    } catch(Exception e) {
      LogBuilder.builder(log)
          .action("Sync Open Cases From SF")
          .message("Manually triggered operation failed")
          .logInfo();

      // Return 500 Internal Server Error including error in body for display on frontend
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
    }
  }

    /**
     * Move candidate to the current candidate data drive.
     * @param number Candidate number
     * @throws IOException
     */
    @GetMapping("move-candidate-drive/{number}")
    public void moveCandidate(@PathVariable("number") String number) throws IOException {
        doMoveCandidate(number);
    }

    /**
     * Move candidates from the given list to the current candidate data drive if needed.
     * <p/>
     * For large numbers of candidates, the request will timeout (504 response) but it will
     * continue processing on server.
     * @param id List of candidates to be processed.
     */
    @GetMapping("move-candidates-drive/{listid}")
    public void moveCandidates(@PathVariable("listid") long id) {
        SavedList savedList = savedListService.get(id);

        final Set<Candidate> candidates = savedList.getCandidates();
        int count = candidates.size();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("MoveCandidatesDrive")
            .message(count + " candidates to move")
            .logInfo();

        for (Candidate candidate : candidates) {
            String folder = candidate.getFolderlink();
            if (folder == null) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("MoveCandidatesDrive")
                    .message("Candidate " + candidate.getCandidateNumber() + " has no folder")
                    .logInfo();
            } else {
                GoogleFileSystemFolder candidateFolder = new GoogleFileSystemFolder(folder);
                try {
                    final GoogleFileSystemDrive currentDrive =
                        fileSystemService.getDriveFromEntity(candidateFolder);
                    if (!googleDriveConfig.getCandidateDataDriveId().equals(currentDrive.getId())) {
                        doMoveCandidate(candidate.getCandidateNumber());
                        LogBuilder.builder(log)
                            .user(authService.getLoggedInUser())
                            .action("MoveCandidatesDrive")
                            .message("Moved candidate " + candidate.getCandidateNumber())
                            .logInfo();
                    } else {
                        LogBuilder.builder(log)
                            .user(authService.getLoggedInUser())
                            .action("MoveCandidatesDrive")
                            .message("Candidate " + candidate.getCandidateNumber() + " already moved")
                            .logInfo();
                    }
                } catch (Exception ex) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("MoveCandidatesDrive")
                        .message("Candidate " + candidate.getCandidateNumber() + " processing error: ")
                        .logError(ex);
                }
            }
            count--;
            if (count%100 == 0) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("MoveCandidatesDrive")
                    .message(count + " candidates still to move")
                    .logInfo();
            }
        }
    }

    private void doMoveCandidate(String number) throws IOException {
        Candidate candidate = this.candidateService.findByCandidateNumber(number);
        if (candidate != null) {
            String folder = candidate.getFolderlink();
            if (folder != null) {
                GoogleFileSystemFolder candidateFolder = new GoogleFileSystemFolder(folder);
                final GoogleFileSystemDrive currentDrive =
                    fileSystemService.getDriveFromEntity(candidateFolder);
                if (!googleDriveConfig.getCandidateDataDriveId().equals(currentDrive.getId())) {
                    fileSystemService.moveEntityToFolder(
                        candidateFolder, googleDriveConfig.getCandidateRootFolder());
                }
            }
        }
    }

//    @GetMapping("create-sf-job-opps")
//    public String createSfJobOpps() {
//        int count = 0;
//        int errorCount = 0;
//        List<SavedList> lists = savedListRepository.findAll();
//        for (SavedList list : lists) {
//            final String sfJoblink = list.getSfJoblink();
//            if (sfJoblink != null && sfJoblink.trim().length() > 0 && list.getSfJobOpp() == null) {
//                try {
//                    final SalesforceJobOpp opp =
//                        salesforceJobOppService.getOrCreateJobOppFromLink(sfJoblink);
//                    if (opp == null) {
//                        log.warn("Problem converting sfJoblink " + sfJoblink + " of list " +
//                            list.getId() + " (" + list.getName() +
//                            ") to sfJobOpp. Null jobOpp returned");
//                        errorCount++;
//                    } else {
//                        list.setSfJobOpp(opp);
//                        savedListRepository.save(list);
//                        log.info("Created sfJobOpp for list " + list.getId() + " (" +  list.getName() + ")");
//                        count++;
//                    }
//                } catch (Exception ex) {
//                    log.warn("Problem converting sfJoblink " + sfJoblink + " of list " +
//                        list.getId() + " (" + list.getName() + ") to sfJobOpp: " + ex);
//                    errorCount++;
//                }
//            }
//        }
//        log.info("Completed Saved List processing. Total converted: " + count + " Errors: " + errorCount);
//
//        //Saved searches as well
//        count = 0;
//        errorCount = 0;
//        List<SavedSearch> searches = savedSearchRepository.findAll();
//        for (SavedSearch search : searches) {
//            final String sfJoblink = search.getSfJoblink();
//            if (sfJoblink != null && sfJoblink.trim().length() > 0 && search.getSfJobOpp() == null) {
//                try {
//                    final SalesforceJobOpp opp =
//                        salesforceJobOppService.getOrCreateJobOppFromLink(sfJoblink);
//                    if (opp == null) {
//                        log.warn("Problem converting sfJoblink " + sfJoblink + " of search " +
//                            search.getId() + " (" + search.getName() +
//                            ") to sfJobOpp. Null jobOpp returned");
//                        errorCount++;
//                    } else {
//                        search.setSfJobOpp(opp);
//                        savedSearchRepository.save(search);
//                        log.info("Created sfJobOpp for search " + search.getId() + " (" +  search.getName() + ")");
//                        count++;
//                    }
//                } catch (Exception ex) {
//                    log.warn("Problem converting sfJoblink " + sfJoblink + " of search " +
//                        search.getId() + " (" + search.getName() + ") to sfJobOpp: " + ex);
//                    errorCount++;
//                }
//            }
//        }
//        log.info("Completed Saved Search processing. Total converted: " + count + " Errors: " + errorCount);
//        return "Done";
//    }

    //    @GetMapping("jd-folders-viewable")
    public String makeJobDescriptionFoldersViewable() throws GeneralSecurityException, IOException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("MakeJobDescriptionFoldersViewable")
            .message("About to get folders")
            .logInfo();

        String nextPageToken = null;
        int count = 0;
        do {
            // Getting folders
            FileList result = googleDriveConfig.getGoogleDriveService().files().list()
                .setQ("'" + listFoldersRootId + "' in parents" +
                    " and mimeType='application/vnd.google-apps.folder'")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .setCorpora("drive")
                .setDriveId(listFoldersDriveId)
                .setPageToken(nextPageToken)
                .setPageSize(100)
                .setFields("nextPageToken, files(id,name,webViewLink)")
                .execute();
            List<com.google.api.services.drive.model.File> folders = result.getFiles();
            nextPageToken = result.getNextPageToken();
            // Looping over folders
            int size = folders.size();

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MakeJobDescriptionFoldersViewable")
                .message("Got " + size + " ID folders. About to loop through.")
                .logInfo();

            for(com.google.api.services.drive.model.File folder: folders) {

                String folderId = folder.getId();
                //Get subfolders
                FileList result2 = googleDriveConfig.getGoogleDriveService().files().list()
                    .setQ("'" + folderId + "' in parents" +
                        " and mimeType='application/vnd.google-apps.folder'")
                    .setSupportsAllDrives(true)
                    .setIncludeItemsFromAllDrives(true)
                    .setCorpora("drive")
                    .setDriveId(listFoldersDriveId)
                    .setFields("files(id,name,webViewLink)")
                    .execute();
                List<com.google.api.services.drive.model.File> subfolders = result2.getFiles();

                //Should only be one subfolder - the list name folder - but loop anyway looking for
                //a folder with a JobDescription subfolder
                for (com.google.api.services.drive.model.File subfolder : subfolders) {

                    String subfolderId = subfolder.getId();
                    //Get subsubfolders
                    FileList result3 = googleDriveConfig.getGoogleDriveService().files().list()
                        .setQ("'" + subfolderId + "' in parents" +
                            " and mimeType='application/vnd.google-apps.folder'")
                        .setSupportsAllDrives(true)
                        .setIncludeItemsFromAllDrives(true)
                        .setCorpora("drive")
                        .setDriveId(listFoldersDriveId)
                        .setFields("files(id,name,webViewLink)")
                        .execute();
                    List<com.google.api.services.drive.model.File> subsubfolders = result3.getFiles();

                    for (com.google.api.services.drive.model.File subsubfolder : subsubfolders) {
                        GoogleFileSystemFile gsf = new GoogleFileSystemFile(
                            subsubfolder.getWebViewLink());
                        final String name = subsubfolder.getName();
                        if (name.equals("JobDescription")) {
                            try {
                                fileSystemService.publishFile(gsf);
                                LogBuilder.builder(log)
                                    .user(authService.getLoggedInUser())
                                    .action("MakeJobDescriptionFoldersViewable")
                                    .message("List " + folder.getName() + ": Made JobDescription viewable")
                                    .logInfo();
                            } catch (Exception ex) {
                                LogBuilder.builder(log)
                                    .user(authService.getLoggedInUser())
                                    .action("MakeJobDescriptionFoldersViewable")
                                    .message("List " + folder.getName() + ": Failed to make JobDescription viewable")
                                    .logError(ex);
                            }
                        }
                    }
                }

                if (count%100 == 0) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("MakeJobDescriptionFoldersViewable")
                        .message("Folders processed:" + count)
                        .logInfo();
                }
                count++;
            }
        } while(
            nextPageToken != null
        );

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("MakeJobDescriptionFoldersViewable")
            .message("Completed processing. Total: " + count)
            .logInfo();

        return "Done";
    }

//    @GetMapping("namedlist-folders-viewable")
    public String makeNamedListFoldersViewable() throws GeneralSecurityException, IOException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("MakeNamedListFoldersViewable")
            .message("About to get folders")
            .logInfo();

        String nextPageToken = null;
        int count = 0;
        do {
            // Getting folders
            FileList result = googleDriveConfig.getGoogleDriveService().files().list()
                .setQ("'" + listFoldersRootId + "' in parents" +
                    " and mimeType='application/vnd.google-apps.folder'")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .setCorpora("drive")
                .setDriveId(listFoldersDriveId)
                .setPageToken(nextPageToken)
                .setPageSize(100)
                .setFields("nextPageToken, files(id,name,webViewLink)")
                .execute();
            List<com.google.api.services.drive.model.File> folders = result.getFiles();
            nextPageToken = result.getNextPageToken();
            // Looping over folders
            int size = folders.size();

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MakeNamedListFoldersViewable")
                .message("Got " + size + " ID folders. About to loop through.")
                .logInfo();

            for(com.google.api.services.drive.model.File folder: folders) {

                String folderId = folder.getId();
                //Get subfolders
                FileList result2 = googleDriveConfig.getGoogleDriveService().files().list()
                    .setQ("'" + folderId + "' in parents" +
                        " and mimeType='application/vnd.google-apps.folder'")
                    .setSupportsAllDrives(true)
                    .setIncludeItemsFromAllDrives(true)
                    .setCorpora("drive")
                    .setDriveId(listFoldersDriveId)
                    .setFields("files(id,name,webViewLink)")
                    .execute();
                List<com.google.api.services.drive.model.File> subfolders = result2.getFiles();

                //Should only be one subfolder - the list name folder
                if (subfolders.size() != 1) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("MakeNamedListFoldersViewable")
                        .message("List " + folder.getName() + " has " + subfolders.size() + " subfolders")
                        .logWarn();
                }
                for (com.google.api.services.drive.model.File subfolder : subfolders) {
                    GoogleFileSystemFile gsf = new GoogleFileSystemFile(
                        subfolder.getWebViewLink());
                    fileSystemService.publishFile(gsf);
                    final String name = subfolder.getName();
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("MakeNamedListFoldersViewable")
                        .message("List " + folder.getName() + ": Made " + name + " viewable")
                        .logInfo();
                }

                if (count%100 == 0) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("MakeNamedListFoldersViewable")
                        .message("Folders processed:" + count)
                        .logInfo();
                }
                count++;
            }
        } while(
            nextPageToken != null
        );

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("MakeNamedListFoldersViewable")
            .message("Completed processing. Total: " + count)
            .logInfo();

        return "Done";
    }

//    @GetMapping("rename-candidate-folders")
    public String renameCandidateFolders() throws GeneralSecurityException, IOException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("RenameCandidateFolders")
            .message("Starting candidate folder re-name. About to get folders.")
            .logInfo();

        Map<String, String> renames = new HashMap<>();
        renames.put("English", "Language");
        renames.put("Medicals", "Medical");
        renames.put("TBB Forms", "Engagement");
        String nextPageToken = null;
        int count = 0;
        do {
            // Getting folders
            FileList result = googleDriveConfig.getGoogleDriveService().files().list()
                .setQ("'" + candidateRootFolderId + "' in parents" +
                    " and mimeType='application/vnd.google-apps.folder'")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .setCorpora("drive")
                .setDriveId(candidateDataDriveId)
                .setPageToken(nextPageToken)
                .setPageSize(100)
                .setFields("nextPageToken, files(id,name,webViewLink)")
                .execute();
            List<com.google.api.services.drive.model.File> folders = result.getFiles();
            nextPageToken = result.getNextPageToken();
            // Looping over folders
            int size = folders.size();

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("RenameCandidateFolders")
                .message("Got " + size + " folders. About to loop through.")
                .logInfo();

            for(com.google.api.services.drive.model.File folder: folders) {

                String folderId = folder.getId();
                //Get subfolders
                FileList result2 = googleDriveConfig.getGoogleDriveService().files().list()
                    .setQ("'" + folderId + "' in parents" +
                        " and mimeType='application/vnd.google-apps.folder'")
                    .setSupportsAllDrives(true)
                    .setIncludeItemsFromAllDrives(true)
                    .setCorpora("drive")
                    .setDriveId(candidateDataDriveId)
                    .setFields("files(id,name,webViewLink)")
                    .execute();
                List<com.google.api.services.drive.model.File> subfolders = result2.getFiles();
                for (com.google.api.services.drive.model.File subfolder : subfolders) {
                    GoogleFileSystemFile gsf = new GoogleFileSystemFile(subfolder.getWebViewLink());
                    final String name = subfolder.getName();
                    String newName = renames.get(name);
                    if (newName != null) {
                        gsf.setName(newName);
                        fileSystemService.renameFile(gsf);

                        LogBuilder.builder(log)
                            .user(authService.getLoggedInUser())
                            .action("RenameCandidateFolders")
                            .message("Candidate " + folder.getName() + ": " + name + " --> " + newName)
                            .logInfo();
                    }
                }

                if (count%100 == 0) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("RenameCandidateFolders")
                        .message("Folders processed:" + count)
                        .logInfo();
                }
                count++;
            }
        } while(
            nextPageToken != null
        );

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("RenameCandidateFolders")
            .message("Completed processing. Total: " + count)
            .logInfo();

        return "Done";
    }

    @GetMapping("updatesflinks")
    public String updateCandidateSalesforceLinks() throws GeneralSecurityException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateCandidateSalesforceLinks")
            .message("Searching Salesforce for candidate contact records")
            .logInfo();

        List<Contact> contacts = salesforceService.findCandidateContacts();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateCandidateSalesforceLinks")
            .message("Updating " + contacts.size() + " candidates")
            .logInfo();

        int count = 0;
        for (Contact contact : contacts) {
            final String candidateNumber = contact.getTbbId().toString();
            Candidate candidate = candidateRepository.findByCandidateNumber(candidateNumber);
            if (candidate == null) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("UpdateCandidateSalesforceLinks")
                    .message("No candidate found for TBBid " + candidateNumber
                        + " SF link " + contact.getUrl(salesforceConfig.getBaseLightningUrl()))
                    .logWarn();
            } else {
                candidate.setSflink(contact.getUrl(salesforceConfig.getBaseLightningUrl()));
                candidateRepository.save(candidate);
            }
            count++;
            if (count%20 == 0) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("UpdateCandidateSalesforceLinks")
                    .message("Processed " + count)
                    .logInfo();
            }
        }
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateCandidateSalesforceLinks")
            .message("Done. Processed " + count)
            .logInfo();

        return "done";
    }

    @GetMapping("sfcontactsupdate")
    public String updateContactsMatchingCondition(@RequestParam String q) throws GeneralSecurityException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateContactsMatchingCondition")
            .message("Searching Salesforce for contact records matching condition " + q)
            .logInfo();

        List<Contact> contacts = salesforceService.findContacts(q);

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateContactsMatchingCondition")
            .message("Updating " + contacts.size() + " candidates")
            .logInfo();

        int count = 0;
        for (Contact contact : contacts) {

            final Long sfTbbId = contact.getTbbId();
            if (sfTbbId == null) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("UpdateContactsMatchingCondition")
                    .message("Contact is not a TBB candidate: " + contact.getUrl(salesforceConfig.getBaseLightningUrl()))
                    .logWarn();
            } else {
                final String candidateNumber = sfTbbId.toString();
                Candidate candidate = candidateRepository.findByCandidateNumber(candidateNumber);
                if (candidate == null) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("UpdateContactsMatchingCondition")
                        .message("No candidate found for TBBid " + candidateNumber
                            + " SF link " + contact.getUrl(salesforceConfig.getBaseLightningUrl()))
                        .logWarn();
                } else {
                    try {
                        salesforceService.updateContact(candidate);
                    } catch (Exception ex) {
                        LogBuilder.builder(log)
                            .user(authService.getLoggedInUser())
                            .action("UpdateContactsMatchingCondition")
                            .message("Problem updating candidate "
                                + candidateNumber + ": " + ex.getMessage())
                            .logWarn();
                    }
                }
            }
            count++;
            if (count%20 == 0) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("UpdateContactsMatchingCondition")
                    .message("Processed " + count)
                    .logInfo();
            }
        }
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateContactsMatchingCondition")
            .message("Done. Processed " + count)
            .logInfo();

        return "done";
    }

    @GetMapping("awsmetadata")
    public String updateAwsFileTypes() {
        List<S3ObjectSummary> objectSummaries = s3ResourceHelper.getObjectSummaries();
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateAwsFileTypes")
            .message("Got the object summaries. There is a total of: " + objectSummaries.size())
            .logInfo();

        List<S3ObjectSummary> filteredSummaries = s3ResourceHelper.filterMigratedObjects(objectSummaries);
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateAwsFileTypes")
            .message("Filtered out the migrated objects. There is a total of: " + filteredSummaries.size())
            .logInfo();

        int count = 0;
        int success = 0;
        for(S3ObjectSummary summary : filteredSummaries) {
            try {
                s3ResourceHelper.addObjectMetadata(summary);
                success++;
            } catch (Exception e) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("UpdateAwsFileTypes")
                    .message("Error adding metadata to object with key: " + summary.getKey())
                    .logWarn(e);
            }
            count++;
            if (count%100 == 0) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("UpdateAwsFileTypes")
                    .message("Processed " + count)
                    .logInfo();
            }
        }
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateAwsFileTypes")
            .message("Finished processing. Success total of: " + success + " out of " + count)
            .logInfo();

        return "done";
    }

    //Remove after running. One off method. Login as System Admin user.
//    @GetMapping("update-isocodes")
    public String updateIsoCodes() {

        String sb = "Countries: "
            + countryService.updateIsoCodes()
            + "\nLanguages: "
            + languageService.updateIsoCodes();

        return "Done: " + sb;
    }

    // Removed after running. One off method. Login as System Admin user.
    //@GetMapping("update-statuses")
    public String updateStatusesIneligible() {
        List<CandidateStatus> statuses = new ArrayList<>(EnumSet.of(CandidateStatus.pending, CandidateStatus.incomplete));
        List<Candidate> candidates = candidateRepository.findByStatuses(statuses);
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateStatusesIneligible")
            .message("Have all pending and incomplete candidates. There is a total of: " + candidates.size())
            .logInfo();

        User loggedInUser = authService.getLoggedInUser().orElse(null);
        int count = 0;
        int success = 0;
        for(Candidate candidate : candidates) {
            try {
                if (candidate.getCountry() != null && candidate.getCountry() == candidate.getNationality()) {
                    candidate.setStatus(CandidateStatus.ineligible);
                    candidateRepository.save(candidate);
                    try {
                        CandidateNote candidateNote = new CandidateNote();
                        candidateNote.setCandidate(candidate);
                        candidateNote.setTitle("Status change from pending to ineligible");
                        candidateNote.setComment("TC criteria not met: Country located is same as country of nationality.");
                        candidateNote.setNoteType(NoteType.admin);
                        candidateNote.setAuditFields(loggedInUser);
                        candidateNoteRepository.save(candidateNote);
                    } catch (Exception e) {
                        LogBuilder.builder(log)
                            .user(authService.getLoggedInUser())
                            .action("UpdateStatusesIneligible")
                            .message("Error creating note for candidate with candidate number: " + candidate.getCandidateNumber())
                            .logWarn(e);
                    }
                }
                success++;
            } catch (Exception e) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("UpdateStatusesIneligible")
                    .message("Error changing status for candidate with candidate number: " + candidate.getCandidateNumber())
                    .logWarn(e);
            }
            count++;
            if (count%100 == 0) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("UpdateStatusesIneligible")
                    .message("Processed " + count)
                    .logInfo();
            }
        }
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("UpdateStatusesIneligible")
            .message("Finished processing. Success total of: " + success + " out of " + count)
            .logInfo();

        return "Done. Now run esload to update elasticsearch.";
    }

    @GetMapping("google")
    public String migrateGoogleDriveFolders() throws IOException, GeneralSecurityException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("MigrateGoogleDriveFolders")
            .message("Starting google folder re-linking. About to get folders.")
            .logInfo();

        String nextPageToken = null;
        int count = 0;
        do {
            // Getting folders
            FileList result = googleDriveConfig.getGoogleDriveService().files().list()
                    .setQ("'" + candidateRootFolderId + "' in parents" +
                            " and mimeType='application/vnd.google-apps.folder'")
                    .setSupportsAllDrives(true)
                    .setIncludeItemsFromAllDrives(true)
                    .setCorpora("drive")
                    .setDriveId(candidateDataDriveId)
                    .setPageToken(nextPageToken)
                    .setPageSize(100)
                    .setFields("nextPageToken, files(id,name,webViewLink)")
                    .execute();
            List<com.google.api.services.drive.model.File> folders = result.getFiles();
            nextPageToken = result.getNextPageToken();
            // Looping over folders
            int size = folders.size();
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MigrateGoogleDriveFolders")
                .message("Got " + size + " folders. About to loop through.")
                .logInfo();

            for(com.google.api.services.drive.model.File folder: folders) {
                setCandidateFolderLink(folder);
                if (count%100 == 0) {
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("MigrateGoogleDriveFolders")
                        .message("Folders processed:" + count)
                        .logInfo();
                }
                count++;
            }
        } while(
           nextPageToken != null
        );

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("MigrateGoogleDriveFolders")
            .message("Completed processing. Total: " + count)
            .logInfo();

        return "done";
    }

    void setCandidateFolderLink(com.google.api.services.drive.model.File folder) {
        // Get candidate number from folder name
        String cn = checkForCN(folder.getName());
        // Find candidate with that candidate number
        if(cn != null){
            Candidate candidate = candidateRepository.findByCandidateNumber(cn);
            if(candidate != null){
                candidate.setFolderlink(folder.getWebViewLink());
                candidateRepository.save(candidate);
            } else {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("MigrateGoogleDriveFolders")
                    .message("Can't find candidate with candidate number: " + cn + " " + folder.getName())
                    .logError();
            }
        }
    }

    String checkForCN(String folderName) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(folderName);
        if (m.find()) {
            return m.group();
        } else {
            return "";
        }
    }

    @GetMapping("dbcopy")
    public String doDBCopy() throws Exception {
        dataSharingService.dbCopy();
        return "done";
    }

//    @GetMapping("migrate/status")
    public String migrateStatus() {
        try {
            Long userId = 1L;
            if (authService != null) {
                User loggedInUser = authService.getLoggedInUser().orElse(null);
                if (loggedInUser != null){
                    userId = loggedInUser.getId();
                }
            }

            Connection sourceConn = DriverManager.getConnection("jdbc:mysql://...", "", "");
            Statement sourceStmt = sourceConn.createStatement();

            Connection targetConn = DriverManager.getConnection(targetJdbcUrl, targetUser, targetPwd);

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MigrateStatus")
                .message("Migration data for candidates")
                .logInfo();

            String updateSql = "update candidate set migration_status =  ? where candidate_number = ? ";
            String selectSql = "select user_id, u.status from  user_jobseeker j join user u on u.id = j.user_id";
            PreparedStatement update = targetConn.prepareStatement(updateSql);
            ResultSet result = sourceStmt.executeQuery(selectSql);
            int count = 0;
            while (result.next()) {
                int i = 1;
                update.setString(i++, result.getString("status"));
                update.setString(i++, result.getString("user_id"));
                update.addBatch();

                if (count%100 == 0) {
                    update.executeBatch();
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("MigrateStatus")
                        .message("candidates - saving batch  " + count)
                        .logInfo();
                }

                count++;
            }
            update.executeBatch();

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MigrateStatus")
                .message("candidates - saving batch " + count)
                .logInfo();

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MigrateStatus")
                .message("Migration data for candidates")
                .logInfo();

            updateSql = "update candidate set status =  'deleted' where migration_status = '0' ";
            update = targetConn.prepareStatement(updateSql);
            update.execute();

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MigrateStatus")
                .message("candidates - fix deleted " + count)
                .logInfo();

        } catch (Exception e){
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MigrateStatus")
                .message("unable to migrate status data")
                .logError(e);
        }

        return "done";
    }

    /**
     * @see PopulateElasticsearchService#populateElasticCandidates(boolean, boolean, Integer, Integer)
     */
    @GetMapping("esload")
    public void loadElasticsearch(
        @RequestParam(value = "reset", required = false) String reset,
        @RequestParam(value = "frompage", required = false) Integer fromPage,
        @RequestParam(value = "topage", required = false) Integer toPage) {

        boolean deleteExisting = reset != null;

        boolean createElastic = deleteExisting;

        populateElasticsearchService.populateElasticCandidates(
            deleteExisting, createElastic, fromPage, toPage);
    }

    @GetMapping("migrate/extract")
    public void migrateExtract() {
        TextExtractHelper textExtractHelper = new TextExtractHelper(candidateAttachmentRepository, s3ResourceHelper);
        Long userId = 1L;
        if (authService != null) {
            User loggedInUser = authService.getLoggedInUser().orElse(null);
            if (loggedInUser != null){
                userId = loggedInUser.getId();
            }
        }

        List<String> types = Arrays.asList("pdf", "docx", "doc", "txt");
        extractTextFromMigratedFiles(textExtractHelper, types);
        extractTextFromNewFiles(textExtractHelper, types);
    }

//    @GetMapping("es-to-db/unhcr-status")
    public void migrateUnhcrStatus() {
        populateElasticsearchService.populateCandidateFromElastic();
    }

    private void extractTextFromMigratedFiles(TextExtractHelper textExtractHelper, List<String> types) {
        List<CandidateAttachment> files = candidateAttachmentRepository.findByFileTypesAndMigrated(types, true);
        int count = 0;
        int success = 0;
        for(CandidateAttachment file : files) {
            try {
                String uniqueFilename = file.getLocation();
                String destination = "candidate/migrated/" + uniqueFilename;
                File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), destination);
                String extractedText = textExtractHelper.getTextExtractFromFile(srcFile, file.getFileType());
                if(StringUtils.isNotBlank(extractedText)) {
                    file.setTextExtract(extractedText);
                    candidateAttachmentRepository.save(file);
                    success++;
                }
            } catch (Exception e) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("MigrateExtract")
                    .message("Unable to extract text from file " + file.getLocation())
                    .logError(e);
            }
            if (count%100 == 0) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("MigrateExtract")
                    .message(count + " new files processed, with " + success + " successfully extracted text.")
                    .logInfo();
            }
            count++;
        }
    }

    private void extractTextFromNewFiles(TextExtractHelper textExtractHelper, List<String> types) {
        List<CandidateAttachment> files = candidateAttachmentRepository.findByFileTypesAndMigrated(types, false);
        int count = 0;
        int success = 0;
        for(CandidateAttachment file : files) {
            try {
                String uniqueFilename = file.getLocation();
                String destination = "candidate/" + file.getCandidate().getCandidateNumber() + "/" + uniqueFilename;
                File srcFile = this.s3ResourceHelper.downloadFile(this.s3ResourceHelper.getS3Bucket(), destination);
                String extractedText = textExtractHelper.getTextExtractFromFile(srcFile, file.getFileType());
                if (StringUtils.isNotBlank(extractedText)) {
                    file.setTextExtract(extractedText);
                    candidateAttachmentRepository.save(file);
                    success++;
                }
            } catch (Exception e) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("MigrateExtract")
                    .message("Unable to extract text from new file " + file.getLocation())
                    .logError(e);
            }
            if (count%100 == 0) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("MigrateExtract")
                    .message(count + " new files processed, with " + success + " successfully extracted text.")
                    .logInfo();
            }
            count++;
        }
    }

//    @GetMapping("migrate/survey")
    public String migrateSurvey() {
        try {
            Long userId = 1L;
            if (authService != null) {
                User loggedInUser = authService.getLoggedInUser().orElse(null);
                if (loggedInUser != null){
                    userId = loggedInUser.getId();
                }
            }

            Connection sourceConn = DriverManager.getConnection("jdbc:mysql:", "", "");
            Statement sourceStmt = sourceConn.createStatement();

            Connection targetConn = DriverManager.getConnection(targetJdbcUrl, targetUser, targetPwd);

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MigrateSurvey")
                .message("Migration survey data for candidates")
                .logInfo();

            String updateSql = "update candidate set survey_type_id = ?, " +
                    " survey_comment = ? where candidate_number = ?";

            String selectSql = "select u.id as userID, `option` as optionID, " +
                    "       option_information_session, option_community_center, " +
                    "       option_facebook_page, option_other, option_ngo, option_outreach " +
                    "from user u " +
                    "    left join user_jobseeker_survey s on s.user_id = u.id " +
                    "where `option` is not null;";
            PreparedStatement update = targetConn.prepareStatement(updateSql);
            ResultSet result = sourceStmt.executeQuery(selectSql);
            int count = 0;
            while (result.next()) {
                String candidateNumber = result.getString("userID");
                int optionID = result.getInt("optionID");
                int targetID = optionID == 0 ? 8 : optionID - 8770;

                String text = "";

                String s;
                s = result.getString("option_information_session");
                text += s == null ? "" : s;
                s = result.getString("option_community_center");
                text += s == null ? "" : s;
                s = result.getString("option_facebook_page");
                text += s == null ? "" : s;
                s = result.getString("option_other");
                text += s == null ? "" : s;
                s = result.getString("option_ngo");
                text += s == null ? "" : s;
                s = result.getString("option_outreach");
                text += s == null ? "" : s;


                int i = 1;
                update.setInt(i++, targetID);
                update.setString(i++, text);
                update.setString(i++, candidateNumber);

//                log.info("Update candidate " + candidateNumber + " " + targetID + ": " + text);
                update.addBatch();

                if (count%100 == 0) {
                    update.executeBatch();
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("MigrateSurvey")
                        .message("candidates - saving batch  " + count)
                        .logInfo();
                }

                count++;
            }
            update.executeBatch();

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MigrateSurvey")
                .message("candidates - saving batch " + count)
                .logInfo();

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MigrateSurvey")
                .message("Migration survey data for candidates")
                .logInfo();

        } catch (Exception e){
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("MigrateSurvey")
                .message("unable to migrate survey data")
                .logError(e);
        }

        return "done";
    }

    //Hidden for now as should no longer be required - can probably delete
//    @GetMapping("migrate")
    public String migrate() {
        try {
            Long userId = 1L;
            if (authService != null) {
                User loggedInUser = authService.getLoggedInUser().orElse(null);
                if (loggedInUser != null){
                    userId = loggedInUser.getId();
                }
            }

            Connection sourceConn = DriverManager.getConnection("jdbc:mysql://", "", "");
            Statement sourceStmt = sourceConn.createStatement();

            Connection targetConn = DriverManager.getConnection(targetJdbcUrl, targetUser, targetPwd);

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("Migrate")
                .message("Preparing translations insert")
                .logInfo();

            PreparedStatement translationInsert = targetConn.prepareStatement("insert into translation (object_id, object_type, language, value, created_by, created_date) values (?, ?, ?, ?, ?, ?)");

            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "country", "country", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "education_level", "education_level", true);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "education_major", "major", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "industry", "industry", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "language", "languages", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "language_level", "language_level", true);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "nationality", "nationality", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "nationality", "nationality_other", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "occupation", "job_ocupation", false);

            migrateUsers(targetConn, sourceStmt);
            migrateAdmins(targetConn, sourceStmt);

            migrateCandidates(targetConn, sourceStmt);

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("Migrate")
                .message("loading candidate ids")
                .logInfo();

            Map<Long, Long> candidateIdsByUserId = loadCandidateIds(targetConn);

            migrateCandidateCertifications(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateLanguages(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateEducations(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateOccupations(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateExperiences(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateAdminNotes(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateLinks(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateSkills(targetConn, sourceStmt, candidateIdsByUserId);

        } catch (Exception e){
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("Migrate")
                .message("unable to migrate data")
                .logError(e);
        }

        return "done";
    }

    private void migrateFormOption(Connection targetConn,
                                   Statement sourceStmt,
                                   PreparedStatement translationInsert,
                                   Long userId,
                                   String tableName,
                                   String optionType,
                                   boolean hasLevel) throws SQLException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for " + tableName)
            .logInfo();

        String insertSql = null;
        String selectSql = null;
        if (optionType == "education_level"){
            insertSql = "insert into " + tableName + " (id, name, level, status, education_type) values (?, ?, ?, ?, ?) on conflict (id) do nothing";
            selectSql = "select id, name, name_ar, `order` from frm_options where type = '" + optionType + "'";
        } else if (hasLevel) {
            insertSql = "insert into " + tableName + " (id, name, level, status) values (?, ?, ?, ?) on conflict (id) do nothing";
            selectSql = "select id, name, name_ar, `order` from frm_options where type = '" + optionType + "'";
        } else {
            insertSql = "insert into " + tableName + " (id, name, status) values (?, ?, ?) on conflict (id) do nothing";
            selectSql = "select id, name, name_ar from frm_options where type = '" + optionType + "'";
        }

        PreparedStatement optionInsert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            int i = 1;
            Long id = result.getLong("id");
            optionInsert.setLong(i++, id);
            String name = result.getString("name");
            optionInsert.setString(i++, name);
            if (hasLevel) {
                optionInsert.setInt(i++, result.getInt("order"));
            }
            optionInsert.setString(i++, "active");
            if (optionType == "education_level"){
                String educationType = getEducationType(name);
                if (educationType != null) {
                    optionInsert.setString(i++, educationType);
                } else {
                    optionInsert.setNull(i++, Types.VARCHAR);
                }
            }
            optionInsert.addBatch();

            addTranslation(translationInsert, id, tableName, "ar", result.getString("name_ar"), userId);

            if (count%100 == 0) {
                optionInsert.executeBatch();
                translationInsert.executeBatch();

                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message(tableName + " - saving batch " + count)
                    .logInfo();
            }

            count++;
        }
        optionInsert.executeBatch();
        translationInsert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message(tableName + " - saving batch " + count)
            .logInfo();
    }

    private void addTranslation(PreparedStatement translationInsert,
                                Long objectId,
                                String objectType,
                                String language,
                                String value,
                                Long userId) throws SQLException {
        // object_id, object_type, language, value, created_by, created_date
        translationInsert.setLong(1, objectId);
        translationInsert.setString(2, objectType);
        translationInsert.setString(3, language);
        translationInsert.setString(4, value);
        translationInsert.setLong(5, 1L);
        translationInsert.setTimestamp(6, Timestamp.valueOf(OffsetDateTime.now().toLocalDateTime()));
        translationInsert.addBatch();
    }

    private void migrateUsers(Connection targetConn,
                              Statement sourceStmt) throws SQLException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for users from user")
            .logInfo();

        String insertSql = "insert into users (id, username, first_name, last_name, email, role, status, password_enc, created_by, created_date, updated_date) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select u.id, username, j.first_name, j.last_name, email, status, password_hash, created_at, updated_at from user u join user_jobseeker j on j.user_id = u.id";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            int i = 1;
            insert.setLong(i++, result.getLong("id"));
            insert.setString(i++, result.getString("username"));
            insert.setString(i++, result.getString("first_name"));
            insert.setString(i++, result.getString("last_name"));
            insert.setString(i++, result.getString("email"));
            insert.setString(i++, "user");
            insert.setString(i++, getUserStatus(result.getInt("status")));
            insert.setString(i++, result.getString("password_hash"));
            insert.setLong(i++, 1);
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("created_at")));
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("updated_at")));
            insert.addBatch();

            if (count%100 == 0) {
                insert.executeBatch();
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("users - saving batch " + count)
                    .logInfo();
            }

            count++;
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("users - saving batch " + count)
            .logInfo();
    }

    private void migrateAdmins(Connection targetConn,
                              Statement sourceStmt) throws SQLException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for admins from admin")
            .logInfo();

        String insertSql = "insert into users (id, username, first_name, last_name, email, role, status, password_enc, created_by, created_date, updated_date) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, username, email, status, password_hash, created_at, updated_at from admin;";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            int i = 1;
            insert.setLong(i++, result.getLong("id"));
            insert.setString(i++, result.getString("username"));
            insert.setNull(i++, Types.VARCHAR);
            insert.setNull(i++, Types.VARCHAR);
            insert.setString(i++, result.getString("email"));
            insert.setString(i++, "admin");
            insert.setString(i++, getUserStatus(result.getInt("status")));
            insert.setString(i++, result.getString("password_hash"));
            insert.setLong(i++, 1);
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("created_at")));
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("updated_at")));
            insert.addBatch();

            if (count%100 == 0) {
                insert.executeBatch();

                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("admins - saving batch " + count)
                    .logInfo();
            }

            count++;
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("admins - saving batch " + count)
            .logInfo();
    }

    private void migrateCandidates(Connection targetConn,
                                   Statement sourceStmt) throws SQLException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for candidates")
            .logInfo();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("loading reference data")
            .logInfo();

        Set<Long> countryIds = loadReferenceIds(targetConn, "country");
        Set<Long> nationalityIds = loadReferenceIds(targetConn, "nationality");
        Set<Long> eduLevelIds = loadReferenceIds(targetConn, "education_level");
        Set<Long> eduMajorIds = loadReferenceIds(targetConn, "education_major");


        // load other options
        Map<Long, String> otherNationalities = loadOtherReferenceIds(sourceStmt, "nationality");

        String insertSql = "insert into candidate (user_id, candidate_number, gender, dob, phone, whatsapp, status, country_id, "
                + " city, nationality_id, additional_info, max_education_level_id, created_by, created_date, updated_date, un_registered, "
                + " un_registration_number, migration_education_major_id) "
                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (user_id) do nothing";
        String selectSql = "select user_id, gender, concat(birth_year, '-', lpad(birth_month, 2, '0'), '-', lpad(birth_day, 2, '0')) as dob, "
                + " phone_number, phone_wapp, u.status, country, f.name as province, nationality, additional_information_summary, "
                + " current_education_level, u.created_at, u.updated_at, un_registration_status, unhcr_number, major "
                + " from user_jobseeker j join user u on u.id = j.user_id "
                + " left join frm_options f on f.id = j.province";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            int i = 1;
            insert.setLong(i++, result.getLong("user_id"));
            insert.setString(i++, result.getString("user_id"));
            insert.setString(i++, getGender(result.getString("gender")));
            insert.setDate(i++, convertToDate(result.getString("dob")));
            insert.setString(i++, result.getString("phone_number"));
            insert.setString(i++, result.getString("phone_wapp"));
            insert.setString(i++, getCandidateStatus(result.getInt("status"), result.getInt("nationality"), result.getInt("country")));
            int origCountryId = result.getInt("country");
            Long countryId = checkReference(origCountryId, countryIds);
            if (countryId == null) {
                countryId = whackyExtraCountryLookup(origCountryId);
            }
            if (countryId != null) {
                insert.setLong(i++, countryId);
            } else {
                insert.setNull(i++,  Types.BIGINT);
            }
            insert.setString(i++, result.getString("province"));
            i = setRefIdOrUnknown(result, insert, "nationality", nationalityIds, otherNationalities, i, 18);
            insert.setString(i++, result.getString("additional_information_summary"));
            i = setRefIdOrNull(result, insert, "current_education_level", eduLevelIds, i);
            insert.setLong(i++, 1);
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("created_at")));
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("updated_at")));
            insert.setBoolean(i++, getUNStatus(result.getInt("un_registration_status")));
            insert.setString(i++, result.getString("unhcr_number"));
            setRefIdOrNull(result, insert, "major", eduMajorIds, i++);
            insert.addBatch();

            if (count%100 == 0) {
                insert.executeBatch();

                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("candidates - saving batch " + count)
                    .logInfo();
            }

            count++;
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("candidates - saving batch " + count)
            .logInfo();
    }

    private void migrateCandidateCertifications(Connection targetConn,
                                                Statement sourceStmt,
                                                Map<Long, Long> candidateIdsByUserId) throws SQLException {

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for candidate certifications")
            .logInfo();

        String insertSql = "insert into candidate_certification (id, candidate_id, name, institution, date_completed) values (?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, certification_name, institution_name, date_of_receipt from user_jobseeker_certification order by user_id";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            if (candidateId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                String certName = result.getString("certification_name");
                certName = certName.replaceAll("\u0000", "");
                insert.setString(i++, certName);
                String institutionName = result.getString("institution_name");
                institutionName = institutionName.replaceAll("\u0000", "");
                insert.setString(i++, institutionName);
                Date date = getDate(result, "date_of_receipt", candidateId);
                if (date != null) {
                    insert.setDate(i++, date);
                } else {
                    insert.setNull(i++, Types.DATE);
                }
                insert.addBatch();

                if (count%100 == 0) {
                    insert.executeBatch();

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("Migrate")
                        .message("certificates - saving batch " + count)
                        .logInfo();
                }

                count++;
            } else {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("skipping record - certifications: no candidate found for userId " + userId)
                    .logWarn();
            }
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("certificates - saving batch " + count)
            .logInfo();
    }

    private void migrateCandidateLanguages(Connection targetConn,
                                           Statement sourceStmt,
                                           Map<Long, Long> candidateIdsByUserId) throws SQLException {

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for candidate languages")
            .logInfo();

        Set<Long> languageIds = loadReferenceIds(targetConn, "language");
        Set<Long> languageLevelIds = loadReferenceIds(targetConn, "language_level");
        Map<Long, String> otherLanguages = loadOtherReferenceIds(sourceStmt, "language_other");

        String insertSql = "insert into candidate_language (id, candidate_id, language_id, written_level_id, spoken_level_id, migration_language) values (?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, language, level, level_reading, if_other from user_jobseeker_languages order by user_id";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            if (candidateId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                i = setRefIdOrUnknown(result, insert, "language", languageIds, otherLanguages, i, 6);
                i = setRefIdOrNull(result, insert, "level", languageLevelIds, i);
                i = setRefIdOrNull(result, insert, "level_reading", languageLevelIds, i);
                insert.addBatch();

                if (count%100 == 0) {
                    insert.executeBatch();

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("Migrate")
                        .message("languages - saving batch " + count)
                        .logInfo();
                }

                count++;
            } else {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("skipping record - languages: no candidate found for userId " + userId)
                    .logWarn();
            }
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("languages - saving batch " + count)
            .logInfo();
    }

    private void migrateCandidateEducations(Connection targetConn,
                                            Statement sourceStmt,
                                            Map<Long, Long> candidateIdsByUserId) throws SQLException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for candidate educations")
            .logInfo();

        Set<Long> countryIds = loadReferenceIds(targetConn, "country");

        String insertSql = "insert into candidate_education (id, candidate_id, country_id, institution, year_completed, education_type, course_name) values (?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select j.id, user_id, country, f.name as university_school, graduation_year, degree, specification_emphasis from user_jobseeker_education  j "
                + " left join frm_options f on f.id = j.university_school order by user_id";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            int origCountryId = result.getInt("country");
            Long countryId = checkReference(origCountryId, countryIds);
            if (countryId == null) {
                countryId = whackyExtraCountryLookup(origCountryId);
            }
            if (candidateId != null && countryId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                insert.setLong(i++, countryId);
                insert.setString(i++,  result.getString("university_school"));
                insert.setInt(i++, result.getInt("graduation_year"));
                insert.setString(i++, getEducationLevel(result.getInt("degree")));
                insert.setString(i++,  result.getString("specification_emphasis"));
                insert.addBatch();

                if (count%100 == 0) {
                    insert.executeBatch();

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("Migrate")
                        .message("educations - saving batch " + count)
                        .logInfo();
                }

                count++;
            } else if (candidateId == null) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("skipping record - educations: no candidate found for userId " + userId)
                    .logWarn();
            } else {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("skipping record - educations: no country found for countryId " + countryId)
                    .logWarn();
            }
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("educations - saving batch " + count)
            .logInfo();
    }

    private void migrateCandidateOccupations(Connection targetConn,
                                             Statement sourceStmt,
                                             Map<Long, Long> candidateIdsByUserId) throws SQLException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for candidate occupations")
            .logInfo();

        Set<Long> occupationIds = loadReferenceIds(targetConn, "occupation");
        Map<Long, String> otherOccupations = loadOtherReferenceIds(sourceStmt, "job_occupation");

        String insertSql = "insert into candidate_occupation (candidate_id, occupation_id, years_experience, verified, migration_occupation) values (?, ?, ?, ?, ?) on conflict (candidate_id, occupation_id) do nothing";
        String selectSql = "select j.user_id, job_occupation, sum(timestampdiff(YEAR, ifnull(start_date, sysdate()), ifnull(end_date, sysdate()))) as years, case when u.status = 10 or u.status = 11 then 1 else 0 end as verified "
                        + " from user_jobseeker_experience j join user u on u.id = j.user_id group by j.user_id, job_occupation";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            if (candidateId != null) {
                int i = 1;
                insert.setLong(i++, candidateId);
                i = setRefIdOrUnknown(result, insert, "job_occupation", occupationIds, otherOccupations, i, 5);
                int years = result.getInt("years");
                if (years < 0) {
                    years = 0;
                }
                insert.setInt(i++, years);
                insert.setBoolean(i++, result.getBoolean("verified"));
                insert.addBatch();

                if (count%100 == 0) {
                    insert.executeBatch();

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("Migrate")
                        .message("occupations - saving batch " + count)
                        .logInfo();
                }

                count++;
            } else {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("skipping record - occupations: no candidate found for userId " + userId)
                    .logWarn();
            }
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("occupations - saving batch " + count)
            .logInfo();
    }

    private void migrateCandidateExperiences(Connection targetConn,
                                             Statement sourceStmt,
                                             Map<Long, Long> candidateIdsByUserId) throws SQLException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for candidate experiences")
            .logInfo();

        Set<Long> countryIds = loadReferenceIds(targetConn, "country");
        Set<Long> occupationIds = loadReferenceIds(targetConn, "occupation");
        // candidateId~occupationId -> candidateOccupationId
        Map<String, Long> candidateOccupations = loadCandidateOccupations(targetConn);

        String insertSql = "insert into candidate_job_experience (id, candidate_id, candidate_occupation_id, company_name, country_id, role, start_date, end_date, full_time, paid, description) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, job_occupation, company_name, location, position_title, start_date, end_date, fulltime, paid, description from user_jobseeker_experience";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            int origOccupationId = result.getInt("job_occupation");
            Long occupationId = checkReference(origOccupationId, occupationIds);
            Long candidateOccupationId = candidateOccupations.get(candidateId + "~" + occupationId);
            if (candidateOccupationId == null) {
                // try with unknown
                candidateOccupationId = candidateOccupations.get(candidateId + "~0");
            }
            if (candidateId != null && candidateOccupationId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                insert.setLong(i++, candidateOccupationId);
                String companyName = result.getString("company_name");
                companyName = companyName.replaceAll("\u0000", "");
                insert.setString(i++,  companyName);
                i = setRefIdOrNull(result, insert, "location", countryIds, i);
                String position = result.getString("position_title");
                position = position.replaceAll("\u0000", "");
                insert.setString(i++,  position);
                Date date = getDate(result, "start_date", candidateId);
                if (date != null) {
                    insert.setDate(i++, date);
                } else {
                    insert.setNull(i++, Types.DATE);
                }
                date = getDate(result, "end_date", candidateId);
                if (date != null) {
                    insert.setDate(i++, date);
                } else {
                    insert.setNull(i++, Types.DATE);
                }
                insert.setBoolean(i++,  getFullTime(result.getInt("fulltime")));
                insert.setBoolean(i++,  getPaid(result.getInt("paid")));
                String description = result.getString("description");
                description = description.replaceAll("\u0000", "");
                insert.setString(i++,  description);

                insert.addBatch();

                if (count%100 == 0) {
                    insert.executeBatch();

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("Migrate")
                        .message("experiences - saving batch " + count)
                        .logInfo();
                }

                count++;
            } else if (candidateId == null) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("skipping record - experiences: no candidate found for userId " + userId)
                    .logWarn();
            } else {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("skipping record - experiences: no candidateOccupation found for candidateId " + candidateId + " and occupationId " + occupationId)
                    .logWarn();
            }
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("experiences - saving batch " + count)
            .logInfo();
    }

    private void migrateCandidateAdminNotes(Connection targetConn,
                                            Statement sourceStmt,
                                            Map<Long, Long> candidateIdsByUserId) throws SQLException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for candidate admin notes")
            .logInfo();

        Set<Long> adminIds = loadAdminIds(targetConn);

        String insertSql = "insert into candidate_note (id, candidate_id, note_type, title, comment, created_date, created_by, updated_date) values (?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, profile_id, subject, comments, created_at, updated_at from admin_user_notes";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("profile_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            if (candidateId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                insert.setString(i++, NoteType.admin.name());
                insert.setString(i++, result.getString("subject"));
                insert.setString(i++, result.getString("comments"));
                insert.setTimestamp(i++, convertToTimestamp(result.getLong("created_at")));
                long adminId = result.getLong("user_id");
                if (adminIds.contains(adminId)) {
                    insert.setLong(i++, adminId);
                } else {
                    insert.setNull(i++,  Types.BIGINT);
                }
                insert.setTimestamp(i++, convertToTimestamp(result.getLong("updated_at")));
                insert.addBatch();

                if (count%100 == 0) {
                    insert.executeBatch();

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("Migrate")
                        .message("admin notes - saving batch " + count)
                        .logInfo();
                }

                count++;
            } else {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("skipping record - admin notes: no candidate found for userId " + userId)
                    .logWarn();
            }
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("admin notes - saving batch " + count)
            .logInfo();
    }

    private void migrateCandidateLinks(Connection targetConn,
                                       Statement sourceStmt,
                                       Map<Long, Long> candidateIdsByUserId) throws SQLException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for candidate attachment links")
            .logInfo();

        String insertSql = "insert into candidate_attachment (id, candidate_id, type, name, location, migrated, admin_only, created_date, created_by) values (?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, name, link from user_jobseeker_link";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            if (candidateId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                insert.setString(i++, AttachmentType.link.name());
                insert.setString(i++, result.getString("name"));
                insert.setString(i++, result.getString("link"));
                insert.setBoolean(i++, true);
                insert.setBoolean(i++, true);
                insert.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
                insert.setLong(i++, userId);
                insert.addBatch();

                if (count%100 == 0) {
                    insert.executeBatch();

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("Migrate")
                        .message("attachment links - saving batch " + count)
                        .logInfo();
                }

                count++;
            } else {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("skipping record - attachment links: no candidate found for userId " + userId)
                    .logWarn();
            }
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for candidate attachments")
            .logInfo();

        Set<Long> adminIds = loadAdminIds(targetConn);

        insertSql = "insert into candidate_attachment (candidate_id, type, name, location, file_type, migrated, admin_only, created_date, created_by) values (?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        selectSql = "select id, user_id, admin_id, filename, extension, upload_date from user_jobseeker_attachments";
        insert = targetConn.prepareStatement(insertSql);
        result = sourceStmt.executeQuery(selectSql);
        count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            if (candidateId != null) {
                int i = 1;
                insert.setLong(i++, candidateId);
                insert.setString(i++, AttachmentType.file.name());
                insert.setString(i++, result.getString("filename"));
                insert.setString(i++, result.getString("filename"));
                insert.setString(i++, result.getString("extension"));
                insert.setBoolean(i++, true);
                insert.setBoolean(i++, false);
                insert.setTimestamp(i++, convertToTimestamp(result.getLong("upload_date")));
                long adminId = result.getLong("user_id");
                if (adminIds.contains(adminId)) {
                    insert.setLong(i++, adminId);
                } else {
                    insert.setLong(i++, userId);
                }
                insert.addBatch();

                if (count%100 == 0) {
                    insert.executeBatch();

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("Migrate")
                        .message("attachment links - saving batch " + count)
                        .logInfo();
                }

                count++;
            } else {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("skipping record - attachment links: no candidate found for userId " + userId)
                    .logWarn();
            }
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("attachment links - saving batch " + count)
            .logInfo();
    }

    private void migrateCandidateSkills(Connection targetConn,
                                        Statement sourceStmt,
                                        Map<Long, Long> candidateIdsByUserId) throws SQLException {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("Migration data for candidate skills")
            .logInfo();

        String insertSql = "insert into candidate_skill (id, candidate_id, skill, time_period) values (?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, skill, time_period from user_jobseeker_skills";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            if (candidateId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                insert.setString(i++, result.getString("skill"));
                insert.setString(i++, getSkillTimePeriod(result.getInt("time_period")));
                insert.addBatch();

                if (count%100 == 0) {
                    insert.executeBatch();

                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("Migrate")
                        .message("skills - saving batch " + count)
                        .logInfo();
                }

                count++;
            } else {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("skipping record - skills: no candidate found for userId " + userId)
                    .logWarn();
            }
        }
        insert.executeBatch();

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("skills - saving batch " + count)
            .logInfo();
    }

    private int setRefIdOrNull(ResultSet result,
                               PreparedStatement insert,
                               String columnName,
                               Set<Long> referenceIds,
                               int colIndex) throws SQLException {
        Long refId = checkReference(result.getInt(columnName), referenceIds);
        if (refId != null) {
            insert.setLong(colIndex, refId);
        } else {
            insert.setNull(colIndex,  Types.BIGINT);
        }
        return colIndex + 1;
    }

    private int setRefIdOrUnknown(ResultSet result,
                                  PreparedStatement insert,
                                  String columnName,
                                  Set<Long> referenceIds,
                                  Map<Long, String> otherValues,
                                  int colIndex,
                                  int otherColIndex) throws SQLException {
        int value = result.getInt(columnName);
        Long refId = checkReference(value, referenceIds);
        if (refId != null) {
            insert.setLong(colIndex, refId);
            insert.setNull(otherColIndex, Types.VARCHAR);
        } else if (otherValues.containsKey((long)value)) {
            insert.setLong(colIndex, 0);
            insert.setString(otherColIndex, otherValues.get((long)value));
        } else {
            insert.setNull(colIndex, Types.BIGINT);
            insert.setNull(otherColIndex, Types.VARCHAR);
        }
        return colIndex + 1;
    }

    private Long getCandidateId(Long userId,
                                  Map<Long, Long> candidateIdsByUserId) {
        return candidateIdsByUserId.get(userId);
    }

    private Set<Long> loadReferenceIds(Connection targetConn,
                                       String tableName) throws SQLException {
        Set<Long> referenceIds = new HashSet<>();
        Statement stmt = targetConn.createStatement();
        ResultSet result = stmt.executeQuery("select id from " + tableName);
        while (result.next()) {
            referenceIds.add(result.getLong(1));
        }

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("loaded " + referenceIds.size() + " reference ids for " + tableName)
            .logInfo();

        return referenceIds;
    }

    private Map<Long, String> loadOtherReferenceIds(Statement sourceStmt,
                                                    String type) throws SQLException {
        Map<Long, String> referenceIds = new HashMap<>();
        ResultSet result = sourceStmt.executeQuery("select id, name from frm_options_other where type = '" + type + "'");
        while (result.next()) {
            referenceIds.put(result.getLong(1), result.getString(2));
        }

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("loaded " + referenceIds.size() + " other references for " + type)
            .logInfo();

        return referenceIds;
    }

    private Map<String, Long> loadCandidateOccupations(Connection targetConn) throws SQLException {
        Map<String, Long> candidateOccupations = new HashMap<>();
        Statement stmt = targetConn.createStatement();
        ResultSet result = stmt.executeQuery("select id, candidate_id, occupation_id from candidate_occupation");
        while (result.next()) {
            candidateOccupations.put(result.getLong(2) + "~" + result.getLong(3), result.getLong(1));
        }

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("loaded " + candidateOccupations.size() + " candidateOccupationIds")
            .logInfo();

        return candidateOccupations;
    }

    private Map<Long, Long> loadCandidateIds(Connection targetConn) throws SQLException {
        Map<Long, Long> referenceMap = new HashMap<>();
        Statement stmt = targetConn.createStatement();
        ResultSet result = stmt.executeQuery("select id, user_id from candidate");
        while (result.next()) {
            referenceMap.put(result.getLong(2), result.getLong(1));
        }

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("loaded " + referenceMap.size() + " candidate ids")
            .logInfo();

        return referenceMap;
    }

    private Set<Long> loadAdminIds(Connection targetConn) throws SQLException {
        Set<Long> referenceMap = new HashSet<>();
        Statement stmt = targetConn.createStatement();
        ResultSet result = stmt.executeQuery("select id from users where role = 'admin'");
        while (result.next()) {
            referenceMap.add(result.getLong(1));
        }

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Migrate")
            .message("loaded " + referenceMap.size() + " admin ids")
            .logInfo();

        return referenceMap;
    }

    private Long checkReference(int value,
                                Set<Long> referenceIds) {
        // null values coming from source db are converted to integer 0 which would be incorrectly linked to "unknown", so treat as null
        Long lookupVal = value > 0 ? (long) value : null;
        if (referenceIds.contains(lookupVal)) {
            return lookupVal;
        }
        return null;
    }

    private String getUserStatus(Integer status) {
        /*
        0=Deleted
        1=Incomplete profile
        2=Awaiting permission
        3=Unable to contact
        4=Not in the region where TBB currently operates
        5=Not currently interested in international employment
        6=Profile recorded in 2 languages
        7=Misplaced or incorrect data
        8=Used for testing
        9=Pending
        10=Active
        11=Active but action needed to improve profile
        */
        switch (status) {
            case 0:
                return Status.inactive.name();
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                return Status.active.name();
        }
        return Status.deleted.name();
    }

    private String getCandidateStatus(Integer status, Integer nationalityId, Integer countryId) {
        if (nationalityId == null || nationalityId == 0 || countryId == null || countryId == 0){
            return CandidateStatus.draft.name();
        }
        switch (status) {
            case 0:
                return CandidateStatus.deleted.name();
            case 1:
                return CandidateStatus.incomplete.name();
            case 2:
                return CandidateStatus.pending.name();
            case 3:
            case 4:
            case 5:
            case 6:
                return CandidateStatus.unreachable.name();
            case 7:
                return CandidateStatus.incomplete.name();
            case 8:
                return CandidateStatus.unreachable.name();
            case 9:
                return CandidateStatus.pending.name();
            case 10:
            case 11:
                return CandidateStatus.active.name();
        }
        return CandidateStatus.deleted.name();
    }

    private String getGender(String gender) {
        if (StringUtils.isNotEmpty(gender)) {
            switch (gender) {
                case "M": return Gender.male.name();
                case "F": return Gender.female.name();
            }
        }
        return null;
    }

    private boolean getUNStatus(int value) {
        // 1=UNHCR, 2=UNRWA, 3=not registered
        return (value == 1 ||  value == 2);
    }

    private String getEducationLevel(Integer value) {
        /*
        | 6864 | degree | Bachelor's Degree  |
        | 6865 | degree | Master's Degree    |
        | 6867 | degree | Doctoral Degree    |
        | 6868 | degree | Associate Degree   |
        | 9442 | degree | Vocational Degree  |
        |    0 |

        */
        if (value > 0) {
            switch (value) {
                case 6864: return EducationType.Bachelor.name();
                case 6865: return EducationType.Masters.name();
                case 6867: return EducationType.Doctoral.name();
                case 6868: return EducationType.Associate.name();
                case 9442: return EducationType.Vocational.name();
            }
        }
        return null;
    }

    private String getEducationType(String name) {
        if (name != null) {
            switch (name) {
                case "Bachelor's Degree": return EducationType.Bachelor.name();
                case "Master's Degree": return EducationType.Masters.name();
                case "Doctoral Degree": return EducationType.Doctoral.name();
                case "Associate Degree": return EducationType.Associate.name();
                case "Vocational Degree": return EducationType.Vocational.name();
                case "Some University": return EducationType.Bachelor.name();
            }
        }
        return null;
    }

    private boolean getPaid(int paidValue) {
        /*
        | 9561 | fulltime | Full time |
        | 9562 | fulltime | Part time |
        */
        return (paidValue == 9557);
    }

    private boolean getFullTime(int fullTimeValue) {
        /*
        | 9561 | fulltime | Full time |
        | 9562 | fulltime | Part time |
        */
        return (fullTimeValue == 9561);
    }

    private String getSkillTimePeriod(int period) {
        /*
        | 336 | time_period | 1 year or less   |
        | 337 | time_period | 1-2 years        |
        | 338 | time_period | 3-5 years        |
        | 339 | time_period | 5-7 years        |
        | 340 | time_period | 7-9 years        |
        | 341 | time_period | 10 years or more |
         */
        if (period > 0) {
            switch (period) {
                case 336: return "1 year or less";
                case 337: return "1-2 years";
                case 338: return "3-5 years";
                case 339: return "5-7 years";
                case 340: return "7-9 years";
                case 341: return "10 years or more";
            }
        }
        return null;
    }

    private Long whackyExtraCountryLookup(Integer origCountryId) {
        Integer val = countryForGeneralCountry.get(origCountryId);
        if (val != null) return val.longValue();
        return null;
    }

    private Date getDate(ResultSet result,
                         String columnName,
                         Long candidateId) {
        try {
            Date date = result.getDate(columnName);
            if (date != null && !"1970-01-01".equals(date.toString())) {
                return date;
            }
        } catch (Exception e) {
            try {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("candidateId " + candidateId + " - unparsable date for column: " + columnName + " - " + result.getString(columnName))
                    .logError(e);

            } catch (Exception e2) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Timestamp convertToTimestamp(Long epoch) {
        if (epoch != null && epoch > 0) {
            epoch = epoch*1000;
            java.util.Date date = new java.util.Date(epoch);
            String formattedDate = new SimpleDateFormat(DATE_FORMAT).format(date);
            if (isDateValid(formattedDate)){
                return Timestamp.from(Instant.ofEpochMilli(epoch));
            };
        }
        return null;
    }

    public static boolean isDateValid(String date)
    {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }


    private Date convertToDate(String isoDateStr) {
        if (StringUtils.isNotEmpty(isoDateStr)) {
            try {
                java.util.Date date = sdf.parse(isoDateStr);
                return new Date(date.getTime());
            } catch (Exception e) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("Migrate")
                    .message("invalid DOB " + isoDateStr)
                    .logWarn();
            }
        }
        return null;
    }

    private Map<Integer, Integer> getExtraCountryMappings() {

        /*
        +------+------+---------+---------+
        | id   | id   | name    | name    |
        +------+------+---------+---------+
        | 6288 |  358 | Jordan  | Jordan  |
        | 6296 |  359 | Lebanon | Lebanon |
        | 6400 |  360 | Turkey  | Turkey  |
        | 6389 |  361 | Syria   | Syria   |
        | 6243 |  363 | Egypt   | Egypt   |
        | 6262 | 6862 | Greece  | Greece  |
        | 6280 | 7344 | Iraq    | Iraq    |
        | 6327 | 9444 | Nauru   | Nauru   |
    */
        Map<Integer, Integer> countryForGeneralCountry = new HashMap<>();
        countryForGeneralCountry.put(358, 6288);
        countryForGeneralCountry.put(359, 6296);
        countryForGeneralCountry.put(360, 6400);
        countryForGeneralCountry.put(361, 6389);
        countryForGeneralCountry.put(363, 6243);
        countryForGeneralCountry.put(6862, 6262);
        countryForGeneralCountry.put(7344, 6280);
        countryForGeneralCountry.put(9444, 6327);
        return countryForGeneralCountry;
    }

    public void setTargetJdbcUrl(String targetJdbcUrl) {
        this.targetJdbcUrl = targetJdbcUrl;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public void setTargetPwd(String targetPwd) {
        this.targetPwd = targetPwd;
    }

    /**
     * Stub for manual call to sync active candidates to SF
     * @param noOfPagesRequested total no of pages (containing up to 200 candidates max) to be
     *                           synced. NB: passing <code>0</code> will sync full search results.
     */
    @GetMapping("sf-sync-candidates/{noOfPagesRequested}")
    public ResponseEntity<?> sfSyncCandidates(
        @PathVariable("noOfPagesRequested") long noOfPagesRequested
    ) {
        try {
            // 0 syncs all results
            if (noOfPagesRequested == 0) {
                initiateSfCandidateSync(null);
            } else {
                initiateSfCandidateSync(noOfPagesRequested);
            }

            return ResponseEntity.ok().build(); // Return 200 OK - front-end will display 'Done'

        } catch(Exception e) {
            LogBuilder.builder(log)
                .action("sfSyncCandidates")
                .message("TC-SF candidate sync failed.")
                .logError(e);

            // Return 500 Internal Server Error including error in body for display on frontend
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }

    /**
     * Sets up search parameters and scheduled background processing for SF candidate sync.
     * @param noOfPagesRequested no of pages requested to be synced - if null, all results will sync
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    private void initiateSfCandidateSync(@Nullable Long noOfPagesRequested)
        throws SalesforceException, WebClientException {

        LogBuilder.builder(log)
            .action("initiateSfCandidateSync")
            .message("Initiating TC-SF candidate sync")
            .logInfo();

        // Obtain and log search results metrics
        Pageable pageable = PageRequest.of(
            0, 200, Sort.by("id").ascending()
        );

        // Candidates with an 'active' status or already uploaded to SF
        final List<CandidateStatus> statuses = new ArrayList<>(
            EnumSet.of(CandidateStatus.active, CandidateStatus.pending,
                CandidateStatus.incomplete));

        Page<Candidate> candidatePage = candidateRepository
            .findByStatusesOrSfLinkIsNotNull(statuses, pageable);

        LogBuilder.builder(log)
            .action("initiateSfCandidateSync")
            .message(candidatePage.getTotalElements() + " candidates meet the criteria")
            .logInfo();

        // Set total page count - if requested amount is null, defers to total pages in results
        long totalNoOfPages =
            Optional.ofNullable(noOfPagesRequested).orElse((long) candidatePage.getTotalPages());

        LogBuilder.builder(log)
            .action("initiateSfCandidateSync")
            .message("This request will process " + totalNoOfPages + " pages of 200 candidates.")
            .logInfo();

        // Implement background processing
        BackProcessor<PageContext> backProcessor =
            backgroundProcessingService.createSfSyncBackProcessor(statuses, totalNoOfPages);

        // Schedule background processing
        BackRunner<PageContext> backRunner = new BackRunner<>();

        ScheduledFuture<?> scheduledFuture = backRunner.start(taskScheduler, backProcessor,
            new PageContext(null), 20);
    }

    /**
     * Scheduled TC -> SF sync of all active candidates
     */
    @Scheduled(cron = "0 0 18 * * SUN", zone = "GMT")
    @SchedulerLock(name = "CandidateService_syncLiveCandidatesToSf", lockAtLeastFor = "PT23H",
        lockAtMostFor = "PT23H")
    public void scheduledSfProdCandidateSync() {
        if (environment.equalsIgnoreCase(Environment.prod.name())) {
            // Passing null means all results will be processed
            initiateSfCandidateSync(null);
        }
    }

    /**
     * Scheduled TC staging -> SF sandbox sync
     */
    @Scheduled(cron = "0 0 18 * * SAT", zone = "GMT")
    @SchedulerLock(name = "CandidateService_syncLiveCandidatesToSf", lockAtLeastFor = "PT23H",
        lockAtMostFor = "PT23H")
    public void scheduledSfSandboxCandidateSync() {
        // Scaled-down replica of prod for testing purposes (4,000 candidates)
        if (environment.equalsIgnoreCase(Environment.staging.name())) {
            initiateSfCandidateSync(20L);
        }
    }

    @GetMapping("delete-job/{jobId}")
    @Transactional
    public ResponseEntity<Void> deleteJob(@PathVariable("jobId") Long jobId) {
        try {
        // Check if the job exists before proceeding
        Optional<SalesforceJobOpp> optionalJob = salesforceJobOppRepository.findById(jobId);
        if (!optionalJob.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 Not Found if job does not exist
        }

        SalesforceJobOpp job = optionalJob.get();
        job.setSubmissionList(null);
        job.setExclusionList(null);
        salesforceJobOppRepository.save(job);

        // Delete related entries in chat_post and job_chat_user
        deleteChatAndJobChatUserEntries(jobId);

        // Execute a native SQL query to delete records from job_suggested_saved_search table
        deleteJobSuggestedSavedSearchEntries(jobId);

        // Delete related entries in SavedList and SavedSearch
        deleteSavedListAndSavedSearchEntries(jobId);

        // Delete the job from salesforce_job_opp
        salesforceJobOppRepository.deleteById(jobId);

        return ResponseEntity.ok().build(); // Return 200 OK
    } catch (Exception e) {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("Delete")
            .message("Error deleting job with id " + jobId)
            .logError(e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Return 500 Internal Server Error
    }
}
    // Helper method to delete related entries in chat_post and job_chat_user
    private void deleteChatAndJobChatUserEntries(Long jobId) {
        List<JobChat> jobChats = jobChatRepository.findByJobOppId(jobId);
        for (JobChat jobChat : jobChats) {
            Long jobChatId = jobChat.getId();
            jobChatUserRepository.deleteByJobChatId(jobChatId);
            chatPostRepository.deleteByJobChatId(jobChatId);
        }
        jobChatRepository.deleteAll(jobChats);
    }

    // Helper method to delete records from job_suggested_saved_search table
    private void deleteJobSuggestedSavedSearchEntries(Long jobId) {
        String sql = "DELETE FROM job_suggested_saved_search WHERE tc_job_id = :jobId";
        entityManager.createNativeQuery(sql)
            .setParameter("jobId", jobId)
            .executeUpdate();
    }

    // Helper method to delete related entries in SavedList and SavedSearch
    private void deleteSavedListAndSavedSearchEntries(Long jobId) {
        List<SavedList> savedLists = savedListRepository.findByJobIds(jobId);
        savedLists.forEach(savedList -> {
            savedList.setSavedSearch(null);
            savedList.setSavedSearchSource(null);
        });
        savedListRepository.saveAll(savedLists);
        savedListRepository.flush();
        savedListRepository.deleteAll(savedLists);
        savedSearchRepository.deleteByJobId(jobId);
    }

    /**
     * Reassigns all candidates on saved list or search with given ID to partner organisation with
     * given ID. Previously done by direct DB edit but this necessitated additional steps of
     * flushing the Redis cache and updating the corresponding elasticsearch index entry. Cache
     * evictions and ES index update proceed as usual with this in-code implementation.
     * <p><strong>Check and double-check param for candidateSource — specifying the wrong one could
     * be very problematic! Also be certain to 'Update' your saved search (i.e. save the current
     * version, which is what this method will use).</strong></p>
     * <p>
     *   Examples of how to call this method from the Settings > System Admin API input:
     *      <br><code>reassign-candidates/list-393-to-partner-16</code>
     *      <br><code>reassign-candidates/search-211-to-partner-16</code>
     * </p>
     * @param candidateSource 'list' or 'search', depending on the source you're using
     * @param sourceId id of source containing candidates to be reassigned
     * @param partnerId id of the partner org to which the candidates will be reassigned
     */
    @Transactional
    @GetMapping("reassign-candidates/{candidateSource}-{sourceId}-to-partner-{partnerId}")
    public ResponseEntity<?> reassignCandidates(
        @PathVariable("candidateSource") String candidateSource,
        @PathVariable("sourceId") int sourceId,
        @PathVariable("partnerId") int partnerId
    ) {
        try {
            Partner newPartner = partnerService.getPartner(partnerId);
            if (!newPartner.isSourcePartner() || !newPartner.getStatus().equals(Status.active)) {
                throw new IllegalArgumentException("New partner must be an active source partner.");
            }

            int pagesProcessed = 0;
            long totalPages;
            long totalCandidates;

            if (candidateSource.equals("list")) {
                // Prepare first page and get metrics for logging and looping
                SavedList savedList = savedListService.get(sourceId);
                SavedListGetRequest request = new SavedListGetRequest();
                Page<Candidate> candidatePage = candidateService.getSavedListCandidates(savedList, request);
                totalPages = candidatePage.getTotalPages();
                totalCandidates = candidatePage.getTotalElements();

                while (pagesProcessed < totalPages) {
                    candidateService.reassignCandidatesOnPage(candidatePage, newPartner);
                    pagesProcessed++;
                    request.setPageNumber(pagesProcessed);
                    candidatePage = candidateService.getSavedListCandidates(savedList, request);
                    totalPages = candidatePage.getTotalElements(); // In case list changing
                }

            } else if (candidateSource.equals("search")) {
                SearchCandidateRequest request = savedSearchService.loadSavedSearch(sourceId);
                Page<Candidate> candidatePage = savedSearchService.searchCandidates(request);
                totalPages = candidatePage.getTotalPages();
                totalCandidates = candidatePage.getTotalElements();

                while (pagesProcessed < totalPages) {
                    candidateService.reassignCandidatesOnPage(candidatePage, newPartner);
                    pagesProcessed++;
                    request.setPageNumber(pagesProcessed);
                    candidatePage = savedSearchService.searchCandidates(request);
                    totalPages = candidatePage.getTotalElements(); // In case results changing
                }

            } else {
                LogBuilder.builder(log)
                    .action("Reassign candidates")
                    .message("Invalid parameter for candidateSource: " + candidateSource)
                    .logInfo();

                throw new IllegalArgumentException(
                    "Parameter candidateSource must be \"search\" or \"list\"."
                );
            }

            LogBuilder.builder(log)
                .action("Reassign candidates")
                .message(
                    "Reassignment of " + totalCandidates + " candidates from " + candidateSource +
                        " with ID " + sourceId + " to " + newPartner.getName() + " complete."
                )
                .logInfo();

            return ResponseEntity.ok().build(); // Return 200 OK - front-end will display 'Done'

        } catch(Exception e) {
            LogBuilder.builder(log)
                .action("Reassign candidates")
                .message("Reassignment of candidates from " + candidateSource + " with ID " +
                    sourceId + " to partner with ID " + partnerId + " failed.")
                .logError(e);

            // Return 500 Internal Server Error including error in body for display on frontend
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }

    @GetMapping("processPotentialDuplicateCandidates")
    ResponseEntity<?> processPotentialDuplicateCandidates() {
        try {
            this.backgroundProcessingService.processPotentialDuplicateCandidates();

            LogBuilder.builder(log)
                .action("Process potential duplicates")
                .message("Manually triggered")
                .logInfo();

            return ResponseEntity.ok().build(); // Return 200 OK - front-end will display 'Done'

        } catch(Exception e) {
            LogBuilder.builder(log)
                .action("Process potential duplicates")
                .message("Manual triggered operation failed")
                .logError(e);

            // Return 500 Internal Server Error including error in body for display on frontend
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }

}
