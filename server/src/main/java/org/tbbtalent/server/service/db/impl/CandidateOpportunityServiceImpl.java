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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateOpportunity;
import org.tbbtalent.server.model.db.CandidateOpportunityStage;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.sf.Opportunity;
import org.tbbtalent.server.repository.db.CandidateOpportunityRepository;
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

    @Async
    @Override
    public void loadCandidateOpportunities(String... jobOppIds) throws SalesforceException {

        log.info("Updating candidate opportunities from Salesforce");

        //Remove duplicates
        final String[] ids = Arrays.stream(jobOppIds).distinct().toArray(String[]::new);
        List<Opportunity> ops = salesforceService.findCandidateOpportunitiesByJobOpps(ids);

        log.info("Loaded " + ops.size() + " candidate opportunities from Salesforce");
        int count = 0;
        int loads = 0;
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
            loads++;
            count++;
            if (count%100 == 0) {
                log.info("Processed " + count + " candidate opportunities from Salesforce");
            }
        }
        log.info("Loaded " + loads + " candidate opportunities from Salesforce");
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
        String jobOppSfid = op.getParent_Opportunity__c();
        SalesforceJobOpp jobOpp = salesforceJobOppService.getJobOppById(jobOppSfid);
        if (jobOpp == null) {
            log.error("Could not find job opp: " + jobOppSfid + " parent of " + op.getName());
        }
        candidateOpportunity.setJobOpp(jobOpp);

        //Look up candidate from id
        String candidateNumber = op.Candidate_TC_id__c;
        Candidate candidate = candidateService.findByCandidateNumber(candidateNumber);
        if (candidate == null) {
            log.error("Could not find candidate number: " + candidateNumber + " in candidate op " + op.getName());
        }
        candidateOpportunity.setCandidate(candidate);

        candidateOpportunity.setEmployerFeedback(op.getEmployer_Feedback__c());
        candidateOpportunity.setName(op.getName());
        candidateOpportunity.setNextStep(op.getNextStep());

        final String nextStepDueDate = op.getNext_Step_Due_Date__c();
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

}
