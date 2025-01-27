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
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.logging.LogBuilder;
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
import org.tctalent.server.request.candidate.dependant.UpdateRelocatingDependantIds;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.SalesforceHelper;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateOpportunityServiceImpl implements CandidateOpportunityService {
    private final CandidateOpportunityRepository candidateOpportunityRepository;
    private final CandidateService candidateService;
    private final JobChatService jobChatService;
    private final SalesforceJobOppService salesforceJobOppService;
    private final SalesforceService salesforceService;
    private final UserService userService;
    private final AuthService authService;
    private final GoogleDriveConfig googleDriveConfig;
    private final FileSystemService fileSystemService;
    private final ChatPostService chatPostService;

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
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("createOrUpdateCandidateOpportunity")
                    .message("Could not find SF candidate opp for candidate "
                        + candidate.getCandidateNumber() + " job " + jobOpp.getId())
                    .logError();
            }
            opp.setSfId(sfId);
        }

        opp.setAuditFields(loggedInUser);

        opp = updateCandidateOpportunity(opp, oppParams);

        if (create) {
            // todo why are we creating a recruiting chat here? Candidate is only now a prospect?
            jobChatService.createCandidateRecruitingChat(opp.getCandidate(), opp.getJobOpp());
            //Create the automated post to notify that a candidate has been added to a submission list.
            publishAddedToSubmissionListPosts(opp);
        }

        return opp;
    }

    public @Nullable String fetchSalesforceId(@NonNull CandidateOpportunity opp) {
        return fetchSalesforceId(opp.getCandidate(), opp.getJobOpp());
    }

    private @Nullable String fetchSalesforceId(
        @NonNull Candidate candidate, @NonNull SalesforceJobOpp jobOpp) {
        Opportunity sfOpp = salesforceService.findCandidateOpportunity(
                candidate.getCandidateNumber(), jobOpp.getSfId());
        return sfOpp == null ? null : sfOpp.getId();
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

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("loadCandidateOpportunities")
            .message("Updating candidate opportunities from Salesforce")
            .logInfo();

        //Remove duplicates
        final String[] ids = Arrays.stream(jobOppIds).distinct().toArray(String[]::new);

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("loadCandidateOpportunities")
            .message(ids.length + " jobs to process")
            .logInfo();

        final int maxJobsAtATime = 10;

        int startJobIndex = 0;

        while (startJobIndex < ids.length) {
            int endJobIndex = startJobIndex + maxJobsAtATime;
            if (endJobIndex > ids.length) {
                endJobIndex = ids.length;
            }
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("loadCandidateOpportunities")
                .message("Processing jobs from " + startJobIndex + " to " + (endJobIndex - 1))
                .logInfo();

            String[] idsChunk = Arrays.copyOfRange(ids, startJobIndex, endJobIndex);
            List<Opportunity> ops = salesforceService.findCandidateOpportunitiesByJobOpps(idsChunk);

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("loadCandidateOpportunities")
                .message("Loaded " + ops.size() + " candidate opportunities from Salesforce")
                .logInfo();

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
     * @param sfOpp Salesforce opportunity retrieved from Salesforce
     * @param tcOpp Cached job opp on our DB
     */
    private void copyOpportunityToCandidateOpportunity(
        @NonNull Opportunity sfOpp, @NonNull CandidateOpportunity tcOpp, boolean createJobOpp) {

        partCopyFromSalesforce(sfOpp, tcOpp);

        //Look up job opp from parent
        String jobOppSfid = sfOpp.getParentOpportunityId();
        SalesforceJobOpp jobOpp;
        if (createJobOpp) {
            jobOpp = salesforceJobOppService.getOrCreateJobOppFromId(jobOppSfid);
        } else {
            jobOpp = salesforceJobOppService.getJobOppById(jobOppSfid);
        }
        if (jobOpp == null) {
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("LoadCandidateOpportunity")
                .message("Could not find job opp: " + jobOppSfid + " parent of " + sfOpp.getName())
                .logError();
        }
        tcOpp.setJobOpp(jobOpp);

        //Look up candidate from id
        String candidateNumber = sfOpp.getCandidateId();
        Candidate candidate = candidateService.findByCandidateNumber(candidateNumber);
        if (candidate == null) {
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("LoadCandidateOpportunity")
                .message("Could not find candidate number: " + candidateNumber + " in candidate op " + sfOpp.getName())
                .logError();
        }
        tcOpp.setCandidate(candidate);

        final String createdDate = sfOpp.getCreatedDate();
        if (createdDate != null) {
            try {
                tcOpp.setCreatedDate(SalesforceHelper.parseSalesforceOffsetDateTime(createdDate));
            } catch (DateTimeParseException ex) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("LoadCandidateOpportunity")
                    .message("Error decoding createdDate from SF: " + createdDate +
                        " in candidate op " + sfOpp.getName())
                    .logError();
            }
        }

        tcOpp.setSfId(sfOpp.getId());
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

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("loadCandidateOpportunityLastActiveStages")
            .message("Loading candidate opportunities from Salesforce")
            .logInfo();

        final int limit = 10;

        String lastId = null;
        int totalOpps = 0;
        int nOpps = -1;
        while (nOpps != 0) {

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("loadCandidateOpportunityLastActiveStages")
                .message("Attempting to load up to " + limit + " opps from " + (lastId == null ? "start" : lastId))
                .logInfo();

            List<Opportunity> ops = salesforceService.findCandidateOpportunities(
                lastId == null ? null : "Id > '" + lastId + "'", limit);
            nOpps = ops.size();
            totalOpps += nOpps;

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("loadCandidateOpportunityLastActiveStages")
                .message("Loaded " + nOpps + " candidate opportunities from Salesforce. Total " + totalOpps)
                .logInfo();

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
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("loadCandidateOpportunityLastActiveStages")
                    .message("Could not find candidate opp with SF id = " + oppId)
                    .logWarn();
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

                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("loadCandidateOpportunityLastActiveStages")
                    .message("Updated lastActiveStage of candidate opportunity "
                        + opp.getName() + "(" + opp.getId() + ") to " + lastActiveStage.name())
                    .logInfo();
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
            final CandidateOpportunityStage newStage = oppParams.getStage();
            if (newStage != null) {
                // Stage is changing
                if (!newStage.equals(opp.getStage())) {
                    // If stage is changing to CLOSED (e.g. removed from submission list) publish posts
                    if (newStage.isClosed()) {
                        publishRemovedFromSubmissionListPosts(opp, newStage);
                    // If a stage is changed to ACCEPTANCE (job offer is accepted)
                    } else if (newStage.equals(CandidateOpportunityStage.acceptance)) {
                        publishOppAcceptedPosts(opp);
                    } else {
                        // If non closing stage change, publish posts
                        publishStageChangePosts(opp, newStage);
                    }

                    // If new stage is relocated or above AND not closed or won, then set relocated address
                    if (newStage.compareTo(CandidateOpportunityStage.relocated) >= 0 && (!newStage.isClosed() || newStage.isWon())) {
                        updateCandidateRelocatedCountry(opp);
                    }
                }

                opp.setStage(newStage);
            }

            //Process next step
            User loggedInUser = userService.getLoggedInUser();
            if (loggedInUser == null) {
                throw new InvalidSessionException("Not logged in");
            }

            final String processedNextStep = auditStampNextStep(
                loggedInUser.getUsername(), LocalDate.now(),
                opp.getNextStep(), oppParams.getNextStep());

            // If next step details changing, send automated post to JobCreatorSourcePartner chat.
            if (oppParams.getNextStep() != null
            ) {
                // To compare previous next step to new one, need to ensure neither is null.
                // Cases are auto-populated with a value for next step when created, but this has
                // not always been the case.
                String currentNextStep = opp.getNextStep() == null ? "" : opp.getNextStep();

                // If only the due date has changed, we still want to send a message.
                // As above, there are some old cases with null values that need to be dealt with.
                LocalDate currentDueDate =
                    opp.getNextStepDueDate() == null ?
                        LocalDate.of(1970, 1, 1) : opp.getNextStepDueDate();

                // If the request due date is null (user deletes the existing value in the form but
                // doesn't set a new one, then submits) it will not be used (see below) — so, for
                // purpose of comparison we give it the same value as the current due date (no
                // message will be sent because they're the same).
                // TODO: next step due date should be a required value in the form
                LocalDate requestDueDate =
                    oppParams.getNextStepDueDate() == null ?
                        currentDueDate : oppParams.getNextStepDueDate();

                if (!processedNextStep.equals(currentNextStep) || !requestDueDate.equals(currentDueDate)) {
                    // Find the relevant job chat
                    JobChat jcspChat = jobChatService.getOrCreateJobChat(
                        JobChatType.JobCreatorSourcePartner,
                        opp.getJobOpp(),
                        opp.getCandidate().getUser().getPartner(),
                        null
                    );

                    String candidateNameAndNumber = getCandidateNameNumber(opp.getCandidate());

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
                    ChatPost nextStepChangeChatPost = chatPostService.createPost(
                        autoPostNextStepChange, jcspChat, userService.getSystemAdminUser());

                    // Publish the chat post
                    chatPostService.publishChatPost(nextStepChangeChatPost);
                }
            }

            opp.setNextStep(processedNextStep);

            // A next step always needs a due date
            final LocalDate requestDueDate = oppParams.getNextStepDueDate();
            if (requestDueDate != null) {
                opp.setNextStepDueDate(requestDueDate);
            }

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
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("uploadFile")
                .message("Failed to delete temporary file " + tempFile)
                .logError();
        }

        return uploadedFile;
    }

    @Override
    public CandidateOpportunity updateRelocatingDependants(long id, UpdateRelocatingDependantIds request) {
        CandidateOpportunity opp = getCandidateOpportunity(id);
        opp.setRelocatingDependantIds(request.getRelocatingDependantIds());
        return candidateOpportunityRepository.save(opp);
    }

    /**
     * Publish posts for a candidate opportunity that is moved to a closing stage.
     * Publish to:
     * - JobCreatorSourcePartner chat
     * - CandidateProspect chat
     * - CandidateRecruiting chat
     * @param opp CandidateOpportunity - the candidate opp that's stage is being changed
     * @param newStage CandidateOpportunityStage - closing stage that the opp is being changed to
     */
    private void publishRemovedFromSubmissionListPosts(CandidateOpportunity opp, CandidateOpportunityStage newStage) {
        Candidate candidate = opp.getCandidate();
        String candidateNameAndNumber = getCandidateNameNumber(opp.getCandidate());

        Post autoPostRemovedFromSubList = new Post();
        autoPostRemovedFromSubList.setContent("The candidate " + candidateNameAndNumber +
                " has been removed for the job '" + opp.getJobOpp().getName() +
                "' with the reason " + newStage.getSalesforceStageName() + ".");

        //Only post to candidate if they got past the prospect stage for this job
        if (opp.getStage() != CandidateOpportunityStage.prospect) {
            // AUTO CHAT TO PROSPECT CHAT
            JobChat prospectChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateProspect, null,
                null, candidate);
            // Create the chat post
            ChatPost prospectChatPostRemoved = chatPostService.createPost(
                autoPostRemovedFromSubList, prospectChat, userService.getSystemAdminUser());
            // Publish chat post
            chatPostService.publishChatPost(prospectChatPostRemoved);
        }

        // AUTO CHAT TO RECRUITING CHAT
        JobChat recruitingChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateRecruiting, opp.getJobOpp(),
                candidate.getUser().getPartner(), candidate);
        // Create the chat post
        ChatPost recruitingChatPostRemoved = chatPostService.createPost(
                autoPostRemovedFromSubList, recruitingChat, userService.getSystemAdminUser());
        // Publish chat post
        chatPostService.publishChatPost(recruitingChatPostRemoved);

        // AUTO CHAT TO JOB CREATOR SOURCE PARTNER CHAT
        JobChat jcspChat = jobChatService.getOrCreateJobChat(JobChatType.JobCreatorSourcePartner, opp.getJobOpp(),
                candidate.getUser().getPartner(), null);
        // Create the chat post
        ChatPost jcspChatPostRemoved = chatPostService.createPost(
                autoPostRemovedFromSubList, jcspChat, userService.getSystemAdminUser());
        // Publish chat post
        chatPostService.publishChatPost(jcspChatPostRemoved);
    }

    /**
     * Publish posts for after a Candidate Opportunity is created (e.g. Added to submission list)
     * and the stage is now prospect.
     * Publish to:
     * - CandidateProspect chat
     * - JobCreatorSourcePartner chat.
     * @param opp CandidateOpportunity - the candidate opp that's stage is being changed
     */
    private void publishAddedToSubmissionListPosts(CandidateOpportunity opp) {
        Candidate candidate = opp.getCandidate();
        String candidateNameAndNumber = getCandidateNameNumber(opp.getCandidate());

        Post autoPostAddedToSubList = new Post();
        autoPostAddedToSubList.setContent("The candidate " + candidateNameAndNumber +
                " is a prospect for the job '" + opp.getJobOpp().getName() +"'.");

        // Note that we don't post to candidates until they get past the prospect stage
        if (opp.getStage() != CandidateOpportunityStage.prospect) {
            // AUTO CHAT TO PROSPECT CHAT
            JobChat prospectChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateProspect,
                null,null, candidate);
            // Create the chat post
            ChatPost prospectChatPost = chatPostService.createPost(
                autoPostAddedToSubList, prospectChat, userService.getSystemAdminUser());
            //publish chat post
            chatPostService.publishChatPost(prospectChatPost);
        }

        // AUTO CHAT TO JOB CREATOR SOURCE PARTNER CHAT
        JobChat jcspChat = jobChatService.getOrCreateJobChat(JobChatType.JobCreatorSourcePartner, opp.getJobOpp(),
                candidate.getUser().getPartner(), null);
        // Create the chat post
        ChatPost jcspChatPost = chatPostService.createPost(
                autoPostAddedToSubList, jcspChat, userService.getSystemAdminUser());
        //publish chat post
        chatPostService.publishChatPost(jcspChatPost);
    }

    /**
     * Publish post for a candidate opportunity stage change that is not to a closing stage.
     * Publish to Job Creator Source Partner chat.
     * @param opp CandidateOpportunity - the candidate opp that's stage is being changed
     * @param newStage CandidateOpportunityStage - non-closing stage that the opp is being changed to
     */
    private void publishStageChangePosts(CandidateOpportunity opp, CandidateOpportunityStage newStage) {
        // Find the relevant job chat
        JobChat jcspChat = jobChatService.getOrCreateJobChat(
                JobChatType.JobCreatorSourcePartner,
                opp.getJobOpp(),
                opp.getCandidate().getUser().getPartner(),
                null
        );

        String candidateNameAndNumber = getCandidateNameNumber(opp.getCandidate());

        // Set the chat post content
        Post autoPostCandidateOppStageChange = new Post();
        autoPostCandidateOppStageChange.setContent(
                "💼 <b>" + opp.getName() + "</b> 🪜<br> This case for candidate "
                        + candidateNameAndNumber
                        + " has changed stage from '" + opp.getStage() + "' to '"
                        + newStage + "'."
        );

        // Create the chat post
        ChatPost candidateOppStageChangeChatPost = chatPostService.createPost(
                autoPostCandidateOppStageChange, jcspChat, userService.getSystemAdminUser());

        // Publish the chat post
        chatPostService.publishChatPost(candidateOppStageChangeChatPost);
    }

    /**
     * Publish post for a candidate opportunity stage change to acceptance. Notify all previous chats.
     * - CandidateProspect chat
     * - CandidateRecruiting chat
     * - JobCreatorSourcePartner chat
     * @param opp CandidateOpportunity - the candidate opp that's stage is being changed
     */
    private void publishOppAcceptedPosts(CandidateOpportunity opp) {
        Candidate candidate = opp.getCandidate();
        String candidateNameAndNumber = getCandidateNameNumber(opp.getCandidate());
        Post autoPostAcceptedJobOffer = new Post();
        autoPostAcceptedJobOffer.setContent("The candidate " + candidateNameAndNumber + " has accepted the job offer from '"
                + opp.getJobOpp().getName() + " and is now a member of the <a href=\"https://pathwayclub.org/about\" target=\"_blank\">Pathway Club</a>.");

        // AUTO CHAT TO PROSPECT CHAT
        JobChat prospectChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateProspect, null,
                null, candidate);
        // Create the chat post
        ChatPost prospectChatPostAccepted = chatPostService.createPost(
                autoPostAcceptedJobOffer, prospectChat, userService.getSystemAdminUser());
        // Publish chat post
        chatPostService.publishChatPost(prospectChatPostAccepted);

        // AUTO CHAT TO RECRUITING CHAT
        JobChat recruitingChat = jobChatService.getOrCreateJobChat(JobChatType.CandidateRecruiting, opp.getJobOpp(),
                candidate.getUser().getPartner(), candidate);
        // Create the chat post
        ChatPost recruitingChatPostAccepted = chatPostService.createPost(
                autoPostAcceptedJobOffer, recruitingChat, userService.getSystemAdminUser());
        // Publish chat post
        chatPostService.publishChatPost(recruitingChatPostAccepted);

        // AUTO CHAT TO JOB CREATOR SOURCE PARTNER CHAT
        JobChat jcspChat = jobChatService.getOrCreateJobChat(JobChatType.JobCreatorSourcePartner, opp.getJobOpp(),
                candidate.getUser().getPartner(), null);
        // Create the chat post
        ChatPost jcspChatPostAccepted = chatPostService.createPost(
                autoPostAcceptedJobOffer, jcspChat, userService.getSystemAdminUser());
        // Publish chat post
        chatPostService.publishChatPost(jcspChatPostAccepted);
    }

    /**
     * If the job opportunity has a country associated, set that as the relocated country for the candidate and create
     * candidate note to track change.
     * @param opp Candidate Opportunity which we get the job opp and the candidate from
     */
    private void updateCandidateRelocatedCountry(CandidateOpportunity opp) {
        Candidate candidate = opp.getCandidate();
        SalesforceJobOpp jobOpp = opp.getJobOpp();
        if (jobOpp.getCountry() != null) {
            // create candidate note to track that the candidate's relocated country is updated
            candidateService.auditNoteIfRelocatedAddressChange(candidate, null,
                    null, null, opp.getJobOpp().getCountry().getName());
            candidate.setRelocatedCountry(jobOpp.getCountry());
            candidateService.save(candidate, false);
        }
    }

    /**
     * Get candidate name and number string for automated chat posts
     * @param candidate Candidate to get details from
     */
    private String getCandidateNameNumber(Candidate candidate) {
        // Get candidate name and number for automated chat posts
        return candidate.getUser().getFirstName() + " "
                + candidate.getUser().getLastName()
                + " (" + candidate.getCandidateNumber() + ")";
    }

    @Transactional
    @Override
    public void processCaseUpdateBatch(List<Opportunity> oppBatch) {
        for (Opportunity sfOpp : oppBatch) {
            String sfId = sfOpp.getId();
            // Fetch SF equivalent of TC Opp
            CandidateOpportunity tcOpp =
                candidateOpportunityRepository.findBySfId(sfId).orElse(null);

            if (tcOpp != null) {
                partCopyFromSalesforce(sfOpp, tcOpp);
                candidateOpportunityRepository.save(tcOpp);
            }
        }
    }

    /**
     * Updates TC opp from its Saleforce equivalent - limited to fields having to do with
     * opportunity progress. For creation of new TC Opps from Saleforce Opps, use
     * {@link #copyOpportunityToCandidateOpportunity(Opportunity, CandidateOpportunity, boolean)}.
     * @param sfOpp SF Opp from which updated values will be copied
     * @param tcOpp TC Opp to which updated values will be copied
     */
    private void partCopyFromSalesforce(
        @NonNull Opportunity sfOpp, @NonNull CandidateOpportunity tcOpp
    ) {
        // NEXT STEP DUE DATE
        final String nextStepDueDate = sfOpp.getNextStepDueDate();
        if (nextStepDueDate != null) {
            try {
                tcOpp.setNextStepDueDate(
                    LocalDate.parse(nextStepDueDate));
            } catch (DateTimeParseException ex) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("LoadCandidateOpportunity")
                    .message("Error decoding nextStepDueDate: " + nextStepDueDate +
                        " in Candidate Opp " + sfOpp.getName())
                    .logError();
            }
        }

        // STAGE
        CandidateOpportunityStage stage;
        try {
            stage = CandidateOpportunityStage.textToEnum(sfOpp.getStageName());
        } catch (IllegalArgumentException e) {
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("LoadCandidateOpportunity")
                .message("Error decoding stage in load: " + sfOpp.getStageName() +
                    " in Candidate Opp " + sfOpp.getName())
                .logError();

            stage = CandidateOpportunityStage.prospect;
        }
        tcOpp.setStage(stage);

        // LAST MODIFIED DATE
        final String lastModifiedDate = sfOpp.getLastModifiedDate();
        if (lastModifiedDate != null) {
            try {
                tcOpp.setUpdatedDate(SalesforceHelper.parseSalesforceOffsetDateTime(lastModifiedDate));
            } catch (DateTimeParseException ex) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("LoadCandidateOpportunity")
                    .message("Error decoding lastModifiedDate from SF: " + lastModifiedDate + " in candidate op " + sfOpp.getName())
                    .logError();
            }
        }

        // NEXT STEP
        if (sfOpp.getNextStep() != null) {
            tcOpp.setNextStep(sfOpp.getNextStep());
        }

        // CLOSING COMMENTS
        if (sfOpp.getClosingComments() != null) {
            tcOpp.setClosingComments(sfOpp.getClosingComments());
        }

        // CLOSING COMMENTS FOR CANDIDATE
        if (sfOpp.getClosingCommentsForCandidate() != null) {
            tcOpp.setClosingCommentsForCandidate(sfOpp.getClosingCommentsForCandidate());
        }

        //  EMPLOYER FEEDBACK
        if (sfOpp.getEmployerFeedback() != null) {
            tcOpp.setEmployerFeedback(sfOpp.getEmployerFeedback());
        }

        //  WON/CLOSED
        tcOpp.setWon(sfOpp.isWon());
        tcOpp.setClosed(sfOpp.isClosed());
    }

    @Override
    public List<String> findAllNonNullSfIdsByClosedFalse() {
        return candidateOpportunityRepository.findAllNonNullSfIdsByClosedFalse();
    }

}
