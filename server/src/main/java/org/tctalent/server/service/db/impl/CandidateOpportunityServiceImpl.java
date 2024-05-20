/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import static org.tctalent.server.util.NextStepHelper.auditStampNextStep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.CandidateOpportunityStageHistory;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.chat.Post;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.model.sf.OpportunityHistory;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.repository.db.CandidateOpportunitySpecification;
import org.tctalent.server.request.candidate.UpdateCandidateOppsRequest;
import org.tctalent.server.request.candidate.UpdateCandidateStatusInfo;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;
import org.tctalent.server.util.SalesforceHelper;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

@Service
@RequiredArgsConstructor
public class CandidateOpportunityServiceImpl implements CandidateOpportunityService {
    private static final Logger log = LoggerFactory.getLogger(SalesforceJobOppServiceImpl.class);
    private final CandidateOpportunityRepository candidateOpportunityRepository;
    private final CandidateService candidateService;
    private final EmailHelper emailHelper;
    private final JobChatService jobChatService;
    private final SalesforceJobOppService salesforceJobOppService;
    private final SalesforceService salesforceService;
    private final UserService userService;
    private final AuthService authService;
    private final GoogleDriveConfig googleDriveConfig;
    private final FileSystemService fileSystemService;
    private final ChatPostServiceImpl chatPostServiceImpl;

    /**
     * Creates or updates CandidateOpportunities associated with the given candidates going for
     * the given job, using the given opportunity data.
     * @param candidates Candidates whose opportunities are going to be created or updated
     * @param oppParams Opportunity data common to all opportunities. Can be null in which case
     *                  no changes are made to existing opps, but new opps will be created
     *                  if needed with the stage defaulting to "prospect".
     * @param jobOpp Job associated with candidate opportunities
     */
    private void createOrUpdateCandidateOpportunities(
        List<Candidate> candidates, @Nullable CandidateOpportunityParams oppParams,
        SalesforceJobOpp jobOpp) {

        for (Candidate candidate : candidates) {
            createOrUpdateCandidateOpportunity(candidate, oppParams, jobOpp);
        }
    }

    private CandidateOpportunity createOrUpdateCandidateOpportunity(
        Candidate candidate, @Nullable CandidateOpportunityParams oppParams,
        SalesforceJobOpp jobOpp) {
        User loggedInUser = userService.getLoggedInUser();

        CandidateOpportunity opp = findOpp(candidate, jobOpp);
        boolean create = opp == null;
        if (create) {
            opp = new CandidateOpportunity();
            opp.setJobOpp(jobOpp);
            opp.setCandidate(candidate);
            opp.setName(salesforceService.generateCandidateOppName(candidate, jobOpp));

            //todo These defaults will be overwritten by whatever is in oppParams (if it is non null).
            //Instead should set these defaults only if oppParams corresponding are empty.
            opp.setStage(CandidateOpportunityStage.prospect);
            opp.setNextStep("Contact candidate and do intake");
            opp.setNextStepDueDate(LocalDate.now().plusWeeks(2));

            String sfId = fetchSalesforceId(candidate, jobOpp);
            if (sfId == null) {
                log.error("Could not find SF candidate opp for candidate "
                    + candidate.getCandidateNumber() + " job " + jobOpp.getId());
            }
            opp.setSfId(sfId);
        }

        opp.setAuditFields(loggedInUser);

        opp = updateCandidateOpportunity(opp, oppParams);

        if (create) {
            //Create the chats
            JobChat prospectChat = jobChatService.createCandidateProspectChat(opp.getCandidate());
            jobChatService.createCandidateRecruitingChat(opp.getCandidate(), opp.getJobOpp());

            //Automate post to notify that a candidate has been added to a submission list.
            //Create the automated post
            String candidateName = opp.getCandidate().getUser().getFirstName() + " "
                + opp.getCandidate().getUser().getLastName();
            Post autoPostAddedToSubList = new Post();
            autoPostAddedToSubList.setContent("The candidate " + candidateName +
                " is a prospect for the job '" + opp.getJobOpp().getName() +"'.");
            // Create the chat post
            ChatPost prospectChatPost = chatPostServiceImpl.createPost(
                autoPostAddedToSubList, prospectChat, userService.getSystemAdminUser());
            //publish chat post
            chatPostServiceImpl.publishChatPost(prospectChatPost);
            //Create another auto post to the job creator source partner chat.
            JobChat jcspChat = jobChatService.getOrCreateJobChat(JobChatType.JobCreatorSourcePartner, opp.getJobOpp(),
                candidate.getUser().getPartner(), null);
            // Create the chat post
            ChatPost jcspChatPost = chatPostServiceImpl.createPost(
                autoPostAddedToSubList, jcspChat, userService.getSystemAdminUser());
            //publish chat post
            chatPostServiceImpl.publishChatPost(jcspChatPost);
        }

        return opp;
    }

    private String fetchSalesforceId(Candidate candidate, SalesforceJobOpp jobOpp) {

        Opportunity opp = salesforceService.findCandidateOpportunity(
            candidate.getCandidateNumber(), jobOpp.getSfId());
        return opp == null ? null : opp.getId();
    }

    @Override
    public void createUpdateCandidateOpportunities(UpdateCandidateOppsRequest request)
        throws NoSuchObjectException, SalesforceException, WebClientException {

        List<Candidate> candidates = candidateService.findByIds(request.getCandidateIds());

        final String sfJobOppId = request.getSfJobOppId();
        final SalesforceJobOpp sfJobOpp =
            salesforceJobOppService.getOrCreateJobOppFromId(sfJobOppId);
        createUpdateCandidateOpportunities(candidates, sfJobOpp, request.getCandidateOppParams());
    }

    public void createUpdateCandidateOpportunities(Collection<Candidate> candidates,
        @Nullable SalesforceJobOpp sfJobOpp, @Nullable CandidateOpportunityParams candidateOppParams)
        throws SalesforceException, WebClientException {
        //Need ordered list so that can match with returned contacts.
        List<Candidate> orderedCandidates = new ArrayList<>(candidates);

        candidateService.upsertCandidatesToSf(orderedCandidates);

        //If we have a Salesforce job opportunity, we can also update associated candidate opps.
        if (sfJobOpp != null) {
            //If the sfJobOpp is not very recent, reload it
            final OffsetDateTime lastUpdate = sfJobOpp.getUpdatedDate();
            if (lastUpdate == null ||
                Duration.between(lastUpdate, OffsetDateTime.now()).toMinutes() >= 3) {
                sfJobOpp = salesforceJobOppService.updateJob(sfJobOpp);
            }

            //Create/update opportunities on Salesforce first
            salesforceService.createOrUpdateCandidateOpportunities(
                orderedCandidates, candidateOppParams, sfJobOpp);
            //Then update opps on local DB. SF opps already created, which will allow us to
            //add sfId to created opps on the local db.
            createOrUpdateCandidateOpportunities(
                orderedCandidates, candidateOppParams, sfJobOpp);

            //Detect any auto candidate status changes based on stage changes
            final CandidateOpportunityStage stage =
                candidateOppParams == null ? null : candidateOppParams.getStage();
            if (stage != null) {
                performAutoStageRelatedStatusUpdates(sfJobOpp, orderedCandidates, stage);
            }
        }
    }

    @Override
    public @Nullable CandidateOpportunity findOpp(Candidate candidate, SalesforceJobOpp jobOpp) {
        return candidateOpportunityRepository.findByCandidateIdAndJobId(candidate.getId(), jobOpp.getId());
    }

    @NonNull
    @Override
    public List<CandidateOpportunity> findJobCreatorPartnerOpps(@Nullable Partner partner) {
        List<CandidateOpportunity> opps;
        if (partner != null && partner.isJobCreator()) {
            opps = candidateOpportunityRepository.findPartnerOpps(partner.getId());
        } else {
            opps = new ArrayList<>();
        }
        return opps;
    }

    @NonNull
    @Override
    public CandidateOpportunity getCandidateOpportunity(long id) throws NoSuchObjectException {
        return candidateOpportunityRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(CandidateOpportunity.class, id));
    }

    @Async
    @Override
    public void loadCandidateOpportunities(String... jobOppIds) throws SalesforceException {

        log.info("Updating candidate opportunities from Salesforce");

        //Remove duplicates
        final String[] ids = Arrays.stream(jobOppIds).distinct().toArray(String[]::new);

        log.info(ids.length + " jobs to process");

        final int maxJobsAtATime = 10;

        int startJobIndex = 0;

        while (startJobIndex < ids.length) {
            int endJobIndex = startJobIndex + maxJobsAtATime;
            if (endJobIndex > ids.length) {
                endJobIndex = ids.length;
            }
            log.info("Processing jobs from " + startJobIndex + " to " + (endJobIndex - 1) );

            String[] idsChunk = Arrays.copyOfRange(ids, startJobIndex, endJobIndex);
            List<Opportunity> ops = salesforceService.findCandidateOpportunitiesByJobOpps(idsChunk);

            log.info("Loaded " + ops.size() + " candidate opportunities from Salesforce");
            for (Opportunity op : ops) {
                loadCandidateOpportunity(op, false);
            }

            startJobIndex = endJobIndex;
        }
    }

    @Override
    @NonNull
    public CandidateOpportunity loadCandidateOpportunity(Opportunity op) throws SalesforceException {
        return loadCandidateOpportunity(op, true);
    }

    @NonNull
    private CandidateOpportunity loadCandidateOpportunity(Opportunity op, boolean createJobOpp) throws SalesforceException {
        String id = op.getId();
        //Look for existing candidate op with that SF id.
        CandidateOpportunity candidateOpportunity = getCandidateOpportunityFromSfId(id);
        if (candidateOpportunity == null) {
            candidateOpportunity = new CandidateOpportunity();
        }
        copyOpportunityToCandidateOpportunity(op, candidateOpportunity, createJobOpp);
        return candidateOpportunityRepository.save(candidateOpportunity);
    }

    /**
     * Copies a Salesforce opportunity record to a CandidateOpportunity
     * @param op Salesforce opportunity retrieved from Salesforce
     * @param candidateOpportunity Cached job opp on our DB
     */
    private void copyOpportunityToCandidateOpportunity(
        @NonNull Opportunity op, @NonNull CandidateOpportunity candidateOpportunity, boolean createJobOpp) {

        //Update DB with data from op

        //Look up job opp from parent
        String jobOppSfid = op.getParentOpportunityId();
        SalesforceJobOpp jobOpp;
        if (createJobOpp) {
            jobOpp = salesforceJobOppService.getOrCreateJobOppFromId(jobOppSfid);
        } else {
            jobOpp = salesforceJobOppService.getJobOppById(jobOppSfid);
        }
        if (jobOpp == null) {
            log.error("Could not find job opp: " + jobOppSfid + " parent of " + op.getName());
        }
        candidateOpportunity.setJobOpp(jobOpp);

        //Look up candidate from id
        String candidateNumber = op.getCandidateId();
        Candidate candidate = candidateService.findByCandidateNumber(candidateNumber);
        if (candidate == null) {
            log.error("Could not find candidate number: " + candidateNumber + " in candidate op " + op.getName());
        }
        candidateOpportunity.setCandidate(candidate);

        candidateOpportunity.setClosed(op.isClosed());
        candidateOpportunity.setWon(op.isWon());
        candidateOpportunity.setClosingCommentsForCandidate(op.getClosingCommentsForCandidate());
        candidateOpportunity.setEmployerFeedback(op.getEmployerFeedback());
        candidateOpportunity.setName(op.getName());
        candidateOpportunity.setNextStep(op.getNextStep());

        final String nextStepDueDate = op.getNextStepDueDate();
        if (nextStepDueDate != null) {
            try {
                candidateOpportunity.setNextStepDueDate(
                    LocalDate.parse(nextStepDueDate));
            } catch (DateTimeParseException ex) {
                log.error("Error decoding nextStepDueDate: " + nextStepDueDate + " in candidate op " + op.getName());
            }
        }

        final String createdDate = op.getCreatedDate();
        if (createdDate != null) {
            try {
                candidateOpportunity.setCreatedDate(SalesforceHelper.parseSalesforceOffsetDateTime(createdDate));
            } catch (DateTimeParseException ex) {
                log.error("Error decoding createdDate from SF: " + createdDate + " in candidate op " + op.getName());
            }
        }

        final String lastModifiedDate = op.getLastModifiedDate();
        if (lastModifiedDate != null) {
            try {
                candidateOpportunity.setUpdatedDate(SalesforceHelper.parseSalesforceOffsetDateTime(lastModifiedDate));
            } catch (DateTimeParseException ex) {
                log.error("Error decoding lastModifiedDate from SF: " + lastModifiedDate + " in candidate op " + op.getName());
            }
        }
        candidateOpportunity.setSfId(op.getId());
        CandidateOpportunityStage stage;
        try {
            stage = CandidateOpportunityStage.textToEnum(op.getStageName());
        } catch (IllegalArgumentException e) {
            log.error("Error decoding stage in load: " + op.getStageName() + " in candidate op " + op.getName());
            stage = CandidateOpportunityStage.prospect;
        }
        candidateOpportunity.setStage(stage);
    }

    /**
     * Checks whether the status of the given candidates going for the given job opportunity
     * can be automatically changed based on them moving to the given stage in that job opp.
     * <p/>
     * Candidates' statuses will be updated if appropriate.
     * @param sfJobOpp Job opportunity
     * @param candidates Some candidates who are going for that opportunity
     * @param stage Stage those candidates have just been updated to.
     */
    private void performAutoStageRelatedStatusUpdates(@NonNull SalesforceJobOpp sfJobOpp,
        List<Candidate> candidates, @NonNull CandidateOpportunityStage stage) {

        for (Candidate candidate : candidates) {
            final CandidateStatus newStatus = checkForNewStatus(stage, candidate);

            if (newStatus != null) {
                UpdateCandidateStatusInfo info = new UpdateCandidateStatusInfo();
                info.setStatus(newStatus);
                info.setComment(
                    "Status changed automatically due to candidate's stage in the '"
                        + sfJobOpp.getName() + "' job opportunity changing to '" + stage + "'");
               candidateService.updateCandidateStatus(candidate, info);
            }
        }
    }

    @Nullable
    private static CandidateStatus checkForNewStatus(CandidateOpportunityStage stage,
        Candidate candidate) {
        CandidateStatus status = candidate.getStatus();
        CandidateStatus newStatus = null;
        if (stage.isEmployed() && status != CandidateStatus.employed) {
            //Auto set status to employed
            newStatus = CandidateStatus.employed;
        } else if (stage == CandidateOpportunityStage.notEligibleForTC
            && status != CandidateStatus.ineligible) {
            //Auto set status
            newStatus = CandidateStatus.ineligible;
        } else if (stage == CandidateOpportunityStage.relocatedNoJobOfferPathway
            && status != CandidateStatus.withdrawn) {
            //Auto set status
            newStatus = CandidateStatus.withdrawn;
        }
        return newStatus;
    }

    @Async
    @Override
    public void loadCandidateOpportunityLastActiveStages() {

        log.info("Loading candidate opportunities from Salesforce");

        final int limit = 10;

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

                List<String> oppIds = ops.stream().map(Opportunity::getId).toList();
                List<OpportunityHistory> histories = salesforceService.findOpportunityHistories(oppIds);

                String currentOppId = null;
                List<OpportunityHistory> currentOppHistory = new ArrayList<>();
                for (OpportunityHistory history : histories) {
                    if (history.getOpportunityId().equals(currentOppId)) {
                        currentOppHistory.add(history);
                    } else {
                        //Process current opp's history
                        processOppHistory(currentOppId, currentOppHistory);

                        //Start new history
                        currentOppId = history.getOpportunityId();
                        currentOppHistory.clear();
                        currentOppHistory.add(history);
                    }
                }
                //Need to process the last one
                processOppHistory(currentOppId, currentOppHistory);
            }
        }
    }

    private void processOppHistory(@Nullable String oppId, List<OpportunityHistory> oppHistories) {
        if (oppId != null) {
            //Fetch opp to update.
            CandidateOpportunity opp = getCandidateOpportunityFromSfId(oppId);
            if (opp == null) {
                log.warn("Could not find candidate opp with SF id = " + oppId);
            } else {
                CandidateOpportunityStage lastActiveStage;
                if (oppHistories.isEmpty()) {
                    //If we have no history, assume last active stage is prospect
                    lastActiveStage = CandidateOpportunityStage.prospect;
                } else {
                    //Decode Salesforce history into stageHistories
                    List<CandidateOpportunityStageHistory> stageHistories = new ArrayList<>();
                    for (OpportunityHistory history : oppHistories) {
                        CandidateOpportunityStageHistory stageHistory = new CandidateOpportunityStageHistory();
                        stageHistory.decodeFromSfHistory(history);
                        stageHistories.add(stageHistory);
                    }

                    //Process decoded stageHistories.
                    //Note that this relies on the fact that the histories are sorted in descending
                    //timestamp order - do that the most recent come first.
                    //See SalesforceService.findOpportunityHistories
                    final Optional<CandidateOpportunityStage> lastActiveStageOptional = stageHistories.stream()
                        .map(CandidateOpportunityStageHistory::getStage)
                        .filter(stage -> !stage.isClosed())
                        .findFirst();

                    //If we only have closed stages - so no lastActiveStage - default to prospect
                    lastActiveStage = lastActiveStageOptional.orElse(
                        CandidateOpportunityStage.prospect);
                }

                //Set lastActiveStage on candidate opp
                opp.setLastActiveStage(lastActiveStage);
                candidateOpportunityRepository.save(opp);
                log.info("Updated lastActiveStage of candidate opportunity "
                    + opp.getName() + "(" + opp.getId() + ") to " + lastActiveStage.name());

            }
        }
    }

    @Nullable
    private CandidateOpportunity getCandidateOpportunityFromSfId(String oppId) {
        //todo If this is running too slow, could index the sfId field on the database.
        //todo But this is currently only used in an one off SystemAdminApi call so maybe
        //todo not worth indexing.
        //todo If we do index it, this method should be exposed in CandidateOpportunityService interface.
        return candidateOpportunityRepository.findBySfId(oppId).orElse(null);
    }

    @Override
    public List<Long> findUnreadChatsInOpps(SearchCandidateOpportunityRequest request) {
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            throw new InvalidSessionException("Not logged in");
        }

        //Construct query
        final Specification<CandidateOpportunity> spec =
            CandidateOpportunitySpecification.buildSearchQuery(request, loggedInUser);

        //Retrieve all results and gather the ids
        List<CandidateOpportunity> allOpps = candidateOpportunityRepository.findAll(spec);
        List<Long> oppIds = allOpps.stream().map(CandidateOpportunity::getId).toList();
        List<Long> unreadChatIds =
            candidateOpportunityRepository.findUnreadChatsInOpps(loggedInUser.getId(), oppIds);
        return unreadChatIds;
    }

    @Override
    public Page<CandidateOpportunity> searchCandidateOpportunities(
        SearchCandidateOpportunityRequest request) {
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            throw new InvalidSessionException("Not logged in");
        }

        //Construct query
        final Specification<CandidateOpportunity> spec =
            CandidateOpportunitySpecification.buildSearchQuery(request, loggedInUser);

        //Retrieve just page requested.
        Page<CandidateOpportunity> opps = candidateOpportunityRepository
            .findAll(spec, request.getPageRequest());

        return opps;
    }

    @Override
    public CandidateOpportunity updateCandidateOpportunity(long id,
        CandidateOpportunityParams request) throws NoSuchObjectException {

        CandidateOpportunity opp = getCandidateOpportunity(id);

        //todo Update nextStep with stamp if it has changed
        //Can't do in setNextStep method of opp because we don't have user
        //Is there a danger in modifying request? Same request can be applied to multiple opps.
        //Not really a problem - that multi update should be treated as new for everyone anyway.
        //todo Where do those multi updates get done?

        final SalesforceJobOpp jobOpp = opp.getJobOpp();
        final Candidate candidate = opp.getCandidate();

        //Update Salesforce
        createUpdateCandidateOpportunities(Collections.singletonList(candidate), jobOpp, request);

        //todo I think this is already called by the above method - but we need the updated opp
        //which we don't get from the above methods
        opp = createOrUpdateCandidateOpportunity(candidate, request, jobOpp );

        return opp;
    }

    private CandidateOpportunity updateCandidateOpportunity(
        CandidateOpportunity opp, @Nullable CandidateOpportunityParams oppParams) {

        if (oppParams != null) {
            // Get candidate name and number for automated chat posts
            String candidateNameAndNumber = opp.getCandidate().getUser().getFirstName() + " "
                + opp.getCandidate().getUser().getLastName()
                + " (" + opp.getCandidate().getCandidateNumber() + ")";

            final CandidateOpportunityStage stage = oppParams.getStage();
            if (stage != null) {
                // If stage changing, automate post to JobCreatorSourcePartner chat
                if (!oppParams.getStage().equals(opp.getStage())) {

                    // Find the relevant job chat
                    JobChat jcspChat = jobChatService.getOrCreateJobChat(
                        JobChatType.JobCreatorSourcePartner,
                        opp.getJobOpp(),
                        opp.getCandidate().getUser().getPartner(),
                        null
                    );

                    // Set the chat post content
                    Post autoPostCandidateOppStageChange = new Post();
                    autoPostCandidateOppStageChange.setContent(
                        "💼 <b>" + opp.getName() + "</b> 🪜<br> This case for candidate "
                            + candidateNameAndNumber
                            + " has changed stage from '" + opp.getStage() + "' to '"
                            + oppParams.getStage() + "'."
                    );

                    // Create the chat post
                    ChatPost candidateOppStageChangeChatPost = chatPostServiceImpl.createPost(
                        autoPostCandidateOppStageChange, jcspChat, userService.getSystemAdminUser());

                    // Publish the chat post
                    chatPostServiceImpl.publishChatPost(candidateOppStageChangeChatPost);
                }

                opp.setStage(stage);
            }

            //Process next step
            User loggedInUser = userService.getLoggedInUser();
            if (loggedInUser == null) {
                throw new InvalidSessionException("Not logged in");
            }

            final String processedNextStep = auditStampNextStep(
                loggedInUser.getUsername(), LocalDate.now(),
                opp.getNextStep(), oppParams.getNextStep());

            // TODO: separate next step and due date, simplify condition checks
            // If next step details changing, send automated post to JobCreatorSourcePartner chat.
            if (
                !processedNextStep.equals(opp.getNextStep()) ||
                (oppParams.getNextStepDueDate() != null && opp.getNextStepDueDate() == null) ||
                (!oppParams.getNextStepDueDate().isEqual(opp.getNextStepDueDate()))
            ) {
                // Find the relevant job chat
                JobChat jcspChat = jobChatService.getOrCreateJobChat(
                    JobChatType.JobCreatorSourcePartner,
                    opp.getJobOpp(),
                    opp.getCandidate().getUser().getPartner(),
                    null
                );

                // Set the chat post content
                Post autoPostNextStepChange = new Post();
                autoPostNextStepChange.setContent(
                    "💼 <b>" + opp.getName()
                        + "</b> 🪜<br> The next step details have changed for this case relating to candidate "
                        + candidateNameAndNumber
                        + ".<br><b>Next step:</b> " + processedNextStep
                        + "<br><b>Due date:</b> "
                        + (oppParams.getNextStepDueDate() == null ?
                        opp.getNextStepDueDate() : oppParams.getNextStepDueDate())
                );

                // Create the chat post
                ChatPost nextStepChangeChatPost = chatPostServiceImpl.createPost(
                    autoPostNextStepChange, jcspChat, userService.getSystemAdminUser());

                // Publish the chat post
                chatPostServiceImpl.publishChatPost(nextStepChangeChatPost);
            }

            opp.setNextStep(processedNextStep);

            opp.setNextStepDueDate(oppParams.getNextStepDueDate());
            opp.setClosingComments(oppParams.getClosingComments());
            opp.setClosingCommentsForCandidate(oppParams.getClosingCommentsForCandidate());
            opp.setEmployerFeedback(oppParams.getEmployerFeedback());
        }
        return candidateOpportunityRepository.save(opp);
    }

    @Override
    public CandidateOpportunity uploadOffer(long id, MultipartFile file)
        throws InvalidRequestException, NoSuchObjectException, IOException {

        CandidateOpportunity opp = getCandidateOpportunity(id);
        if (opp.getCandidate() == null) {
            throw new InvalidRequestException("Opportunity " + id + " does not have candidate associated.");
        }
        GoogleFileSystemFile uploadedFile = uploadOfferFile(opp, file);
        opp.setFileOfferLink(uploadedFile.getUrl());
        opp.setFileOfferName(uploadedFile.getName());
        opp.setAuditFields(authService.getLoggedInUser().orElse(null));
        return candidateOpportunityRepository.save(opp);
    }

    private GoogleFileSystemFile uploadOfferFile(CandidateOpportunity opp, MultipartFile file)
        throws IOException {
        Candidate candidate = opp.getCandidate();

        // Create the candidate folder where the job offer file will exist.
        // If folder already exists this does nothing.
        candidateService.createCandidateFolder(candidate.getId());

        String folderLink = candidate.getFolderlink();

        //Name of file being uploaded (this is the name it had on the
        //originating computer).
        String fileName = file.getOriginalFilename();

        return uploadFile(folderLink, fileName, file);
    }

    private GoogleFileSystemFile uploadFile(String folderLink, String fileName,
        MultipartFile file) throws IOException {

        //Save to a temporary file
        InputStream is = file.getInputStream();
        File tempFile = File.createTempFile("offer", ".tmp");
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = is.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

        final GoogleFileSystemDrive candidateDataDrive = googleDriveConfig.getCandidateDataDrive();
        final GoogleFileSystemFolder parentFolder = new GoogleFileSystemFolder(folderLink);

        //Upload the file to its folder, with the correct name (not the temp
        //file name).
        GoogleFileSystemFile uploadedFile =
            fileSystemService.uploadFile(candidateDataDrive, parentFolder, fileName, tempFile);

        //Delete tempfile
        if (!tempFile.delete()) {
            log.error("Failed to delete temporary file " + tempFile);
        }

        return uploadedFile;
    }

    //One minute past Midnight GMT
    @Scheduled(cron = "0 1 0 * * ?", zone = "GMT")
    @SchedulerLock(name = "CandidateOpportunityService_scheduledNotifyOfChatsWithNewPosts", lockAtLeastFor = "PT23H", lockAtMostFor = "PT23H")
    @Transactional
    public void scheduledNotifyOfChatsWithNewPosts() {
        notifyOfChatsWithNewPosts();
    }

    /**
     * Can be called directly by SystemAdminApi as often as needed without running into
     * the SchedulerLock which is on {@link #scheduledNotifyOfChatsWithNewPosts()}
     */
    public void notifyOfChatsWithNewPosts() {

        Map<Long, Set<JobChat>> userNotifications = new HashMap<>();

        OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
        List<Long> chatsWithNewPosts = jobChatService.findChatsWithPostsSinceDate(yesterday);

        List<JobChat> chats = jobChatService.findByIds(chatsWithNewPosts);

        //Note that this is Candidate user notification only.
        //Extract all users who need to be notified of chats with new posts
        for (JobChat chat : chats) {
            JobChatType chatType = chat.getType();
            Candidate candidate = chat.getCandidate();
            SalesforceJobOpp job = chat.getJobOpp();
            switch (chatType) {
                case CandidateProspect -> {
                    if (candidate != null) {
                        Set<JobChat> userChats =
                            userNotifications.computeIfAbsent(
                                candidate.getUser().getId(), k -> new HashSet<>());
                        userChats.add(chat);
                    }
                }
                case CandidateRecruiting -> {
                    if (candidate != null && job != null) {
                        CandidateOpportunity aCase = findOpp(candidate, job);
                        if (aCase != null) {
                            //Candidates only see this chat if they are at or past the review stage
                            if (aCase.getStage().isWon() || !aCase.getStage().isClosed()
                                && CandidateOpportunityStage.cvReview.compareTo(aCase.getStage()) <= 0) {
                                Set<JobChat> userChats =
                                    userNotifications.computeIfAbsent(
                                        candidate.getUser().getId(), k -> new HashSet<>());
                                userChats.add(chat);
                            }
                        }
                    }
                }
                case AllJobCandidates -> {
                    if (job != null) {
                        Set<CandidateOpportunity> cases = job.getCandidateOpportunities();
                        for (CandidateOpportunity aCase : cases) {
                            //Candidates only see this chat if they have accepted the job offer
                            if (aCase.getStage().isWon() || !aCase.getStage().isClosed()
                                && CandidateOpportunityStage.acceptance.compareTo(aCase.getStage()) <= 0) {
                                candidate = aCase.getCandidate();
                                Set<JobChat> userChats =
                                    userNotifications.computeIfAbsent(
                                        candidate.getUser().getId(), k -> new HashSet<>());
                                userChats.add(chat);
                            }
                        }
                    }
                }
            }
        }

        //Construct and send emails
        for (Long userId : userNotifications.keySet()) {
            final Set<JobChat> userChats = userNotifications.get(userId);
            String s = userChats.stream()
                .map(c -> c.getId().toString())
                .collect(Collectors.joining(","));
            log.info("Tell user " + userId + " about posts to chats " + s);
            User user = userService.getUser(userId);
            if (user != null) {
                emailHelper.sendNewChatPostsForCandidateUserEmail(user, userChats);
            }
        }
    }

}
