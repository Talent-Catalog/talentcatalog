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

package org.tbbtalent.server.service.db.impl;

import static org.tbbtalent.server.util.SalesforceHelper.parseSalesforceOffsetDateTime;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateOpportunity;
import org.tbbtalent.server.model.db.CandidateOpportunityStage;
import org.tbbtalent.server.model.db.CandidateStatus;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.sf.Contact;
import org.tbbtalent.server.model.sf.Opportunity;
import org.tbbtalent.server.repository.db.CandidateOpportunityRepository;
import org.tbbtalent.server.repository.db.CandidateOpportunitySpecification;
import org.tbbtalent.server.request.candidate.UpdateCandidateOppsRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateStatusInfo;
import org.tbbtalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tbbtalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;
import org.tbbtalent.server.service.db.CandidateOpportunityService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.SalesforceJobOppService;
import org.tbbtalent.server.service.db.SalesforceService;
import org.tbbtalent.server.service.db.UserService;

@Service
public class CandidateOpportunityServiceImpl implements CandidateOpportunityService {
    private static final Logger log = LoggerFactory.getLogger(SalesforceJobOppServiceImpl.class);
    private final CandidateOpportunityRepository candidateOpportunityRepository;
    private final CandidateService candidateService;
    private final SalesforceJobOppService salesforceJobOppService;
    private final SalesforceService salesforceService;
    private final UserService userService;


    public CandidateOpportunityServiceImpl(
        CandidateOpportunityRepository candidateOpportunityRepository,
        CandidateService candidateService, SalesforceJobOppService salesforceJobOppService, SalesforceService salesforceService,
        UserService userService) {
        this.candidateOpportunityRepository = candidateOpportunityRepository;
        this.candidateService = candidateService;
        this.salesforceJobOppService = salesforceJobOppService;
        this.salesforceService = salesforceService;
        this.userService = userService;
    }

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

        return updateCandidateOpportunity(opp, oppParams);
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

        //Update Salesforce contacts
        List<Contact> contacts =
            salesforceService.createOrUpdateContacts(orderedCandidates);

        //Update the sfLink in all candidate records.
        int nCandidates = orderedCandidates.size();
        for (int i = 0; i < nCandidates; i++) {
            Contact contact = contacts.get(i);
            if (contact.getId() != null) {
                Candidate candidate = orderedCandidates.get(i);
                candidateService.updateCandidateSalesforceLink(candidate, contact.getUrl());
            }
        }

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
    public CandidateOpportunity findOpp(Candidate candidate, SalesforceJobOpp jobOpp) {
        return candidateOpportunityRepository.findByCandidateIdAndJobId(candidate.getId(), jobOpp.getId());
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
        CandidateOpportunity candidateOpportunity = candidateOpportunityRepository.findBySfId(id)
            .orElse(null);
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
                candidateOpportunity.setCreatedDate(parseSalesforceOffsetDateTime(createdDate));
            } catch (DateTimeParseException ex) {
                log.error("Error decoding createdDate from SF: " + createdDate + " in candidate op " + op.getName());
            }
        }

        final String lastModifiedDate = op.getLastModifiedDate();
        if (lastModifiedDate != null) {
            try {
                candidateOpportunity.setUpdatedDate(parseSalesforceOffsetDateTime(lastModifiedDate));
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
            CandidateStatus status = candidate.getStatus();
            CandidateStatus newStatus = null;
            if (stage.isEmployed() && status != CandidateStatus.employed) {
                //Auto set status to employed
                newStatus = CandidateStatus.employed;
            } else if (stage == CandidateOpportunityStage.notEligibleForTC
                && status != CandidateStatus.ineligible) {
                //Auto set status to ineligible
                newStatus = CandidateStatus.ineligible;
            }

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

    @Override
    public Page<CandidateOpportunity> searchCandidateOpportunities(
        SearchCandidateOpportunityRequest request) {
        User loggedInUser = userService.getLoggedInUser();
        Page<CandidateOpportunity> opps = candidateOpportunityRepository.findAll(
            CandidateOpportunitySpecification.buildSearchQuery(request, loggedInUser),
            request.getPageRequest());

        return opps;
    }

    @Override
    public CandidateOpportunity updateCandidateOpportunity(long id,
        CandidateOpportunityParams request) throws NoSuchObjectException {

        CandidateOpportunity opp = getCandidateOpportunity(id);

        //Update Salesforce
        final SalesforceJobOpp jobOpp = opp.getJobOpp();
        final Candidate candidate = opp.getCandidate();

        createUpdateCandidateOpportunities(Collections.singletonList(candidate), jobOpp, request);
        opp = createOrUpdateCandidateOpportunity(candidate, request, jobOpp );

        return opp;
    }

    private CandidateOpportunity updateCandidateOpportunity(
        CandidateOpportunity opp, @Nullable CandidateOpportunityParams oppParams) {

        if (oppParams != null) {
            final CandidateOpportunityStage stage = oppParams.getStage();
            if (stage != null) {
                opp.setStage(stage);
            }

            opp.setNextStep(oppParams.getNextStep());
            opp.setNextStepDueDate(oppParams.getNextStepDueDate());
            opp.setClosingComments(oppParams.getClosingComments());
            opp.setClosingCommentsForCandidate(oppParams.getClosingCommentsForCandidate());
            opp.setEmployerFeedback(oppParams.getEmployerFeedback());
        }
        return candidateOpportunityRepository.save(opp);
    }
}
