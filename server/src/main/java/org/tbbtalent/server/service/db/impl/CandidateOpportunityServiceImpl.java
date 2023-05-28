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

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.tbbtalent.server.model.sf.Contact;
import org.tbbtalent.server.model.sf.Opportunity;
import org.tbbtalent.server.repository.db.CandidateOpportunityRepository;
import org.tbbtalent.server.request.candidate.SalesforceOppParams;
import org.tbbtalent.server.request.candidate.UpdateCandidateOppsRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateStatusInfo;
import org.tbbtalent.server.service.db.CandidateOpportunityService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.SalesforceJobOppService;
import org.tbbtalent.server.service.db.SalesforceService;

@Service
public class CandidateOpportunityServiceImpl implements CandidateOpportunityService {
    private static final Logger log = LoggerFactory.getLogger(SalesforceJobOppServiceImpl.class);
    private final CandidateOpportunityRepository candidateOpportunityRepository;
    private final CandidateService candidateService;
    private final SalesforceJobOppService salesforceJobOppService;
    private final SalesforceService salesforceService;

    public CandidateOpportunityServiceImpl(
        CandidateOpportunityRepository candidateOpportunityRepository,
        CandidateService candidateService, SalesforceJobOppService salesforceJobOppService, SalesforceService salesforceService) {
        this.candidateOpportunityRepository = candidateOpportunityRepository;
        this.candidateService = candidateService;
        this.salesforceJobOppService = salesforceJobOppService;
        this.salesforceService = salesforceService;
    }

    @Override
    public void createOrUpdateCandidateOpportunities(
        List<Candidate> candidates, SalesforceOppParams oppParams, SalesforceJobOpp jobOpp) {

        for (Candidate candidate : candidates) {
            //Find candidate opp, if any
            CandidateOpportunity opp = findOpp(candidate, jobOpp);
            boolean create = opp == null;
            if (create) {
                opp = new CandidateOpportunity();
                opp.setJobOpp(jobOpp);
                opp.setCandidate(candidate);
                opp.setName(salesforceService.generateCandidateOppName(candidate, jobOpp));
            }
            final CandidateOpportunityStage stage = oppParams.getStage();
            if (stage != null) {
                opp.setStage(stage);
            }

            opp.setNextStep(oppParams.getNextStep());
            opp.setNextStepDueDate(oppParams.getNextStepDueDate());
            opp.setClosingComments(oppParams.getClosingComments());
            opp.setEmployerFeedback(oppParams.getEmployerFeedback());
            candidateOpportunityRepository.save(opp);
        }
    }

    @Override
    public void createUpdateSalesforce(UpdateCandidateOppsRequest request)
        throws NoSuchObjectException, SalesforceException, WebClientException {

        List<Candidate> candidates = candidateService.findByIds(request.getCandidateIds());

        final String sfJobOppId = request.getSfJobOppId();
        final SalesforceJobOpp sfJobOpp =
            salesforceJobOppService.getOrCreateJobOppFromId(sfJobOppId);
        createUpdateSalesforce(candidates, sfJobOpp, request.getSalesforceOppParams());
    }

    public void createUpdateSalesforce(Collection<Candidate> candidates,
        @Nullable SalesforceJobOpp sfJobOpp, @Nullable SalesforceOppParams salesforceOppParams)
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
            final OffsetDateTime lastUpdate = sfJobOpp.getLastUpdate();
            if (lastUpdate == null ||
                Duration.between(lastUpdate, OffsetDateTime.now()).toMinutes() >= 3) {
                sfJobOpp = salesforceJobOppService.updateJob(sfJobOpp);
            }

            //Create/update opportunities on local database as well as on Salesforce
            createOrUpdateCandidateOpportunities(
                orderedCandidates, salesforceOppParams, sfJobOpp);
            salesforceService.createOrUpdateCandidateOpportunities(
                orderedCandidates, salesforceOppParams, sfJobOpp);

            //Detect any auto candidate status changes based on stage changes
            final CandidateOpportunityStage stage =
                salesforceOppParams == null ? null : salesforceOppParams.getStage();
            if (stage != null) {
                performAutoStageRelatedStatusUpdates(sfJobOpp, orderedCandidates, stage);
            }
        }
    }

    @Override
    public CandidateOpportunity findOpp(Candidate candidate, SalesforceJobOpp jobOpp) {
        return candidateOpportunityRepository.findByCandidateIdAndJobId(candidate.getId(), jobOpp.getId());
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
                String id = op.getId();
                //Fetch DB with id
                CandidateOpportunity candidateOpportunity = candidateOpportunityRepository.findBySfId(id)
                    .orElse(null);
                if (candidateOpportunity == null) {
                    candidateOpportunity = new CandidateOpportunity();
                }
                copyOpportunityToCandidateOpportunity(op, candidateOpportunity);
                candidateOpportunityRepository.save(candidateOpportunity);
            }

            startJobIndex = endJobIndex;
        }
    }

    /**
     * Copies a Salesforce opportunity record to a CandidateOpportunity
     * @param op Salesforce opportunity retrieved from Salesforce
     * @param candidateOpportunity Cached job opp on our DB
     */
    private void copyOpportunityToCandidateOpportunity(
        @NonNull Opportunity op, @NonNull CandidateOpportunity candidateOpportunity) {

        //Update DB with data from op

        //Look up job opp from parent
        String jobOppSfid = op.getParentOpportunityId();
        SalesforceJobOpp jobOpp = salesforceJobOppService.getJobOppById(jobOppSfid);
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

}
