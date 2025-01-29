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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.email.EmailHelper;
import org.tctalent.server.util.SalesforceHelper;

@Service
@AllArgsConstructor
@Slf4j
public class SalesforceJobOppServiceImpl implements SalesforceJobOppService {

    private static final OffsetDateTime FIRST_PUBLISHED_JOB_DATE =
        OffsetDateTime.parse("2022-11-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME);
    private final SalesforceJobOppRepository salesforceJobOppRepository;
    private final SalesforceService salesforceService;
    private final CountryService countryService;

    private final EmailHelper emailHelper;

    @NonNull
    @Override
    public SalesforceJobOpp getJobOpp(long jobId) throws NoSuchObjectException {
        final SalesforceJobOpp jobOpp = salesforceJobOppRepository.findById(jobId)
            .orElseThrow(() -> new NoSuchObjectException(SalesforceJobOpp.class, jobId));

        return jobOpp;
    }

    @Nullable
    @Override
    public SalesforceJobOpp getJobOppById(String sfId) {
        return salesforceJobOppRepository.findBySfId(sfId).orElse(null);
    }

    @Nullable
    @Override
    public SalesforceJobOpp getJobOppByUrl(String sfUrl) {
        return getJobOppById(SalesforceHelper.extractIdFromSfUrl(sfUrl));
    }

    @Override
    @NonNull
    public SalesforceJobOpp createJobOpp(String sfId)
        throws InvalidRequestException, SalesforceException {
        SalesforceJobOpp salesforceJobOpp = new SalesforceJobOpp();
        salesforceJobOpp.setSfId(sfId);

        Opportunity op = salesforceService.fetchJobOpportunity(sfId);
        if (op == null) {
            throw new InvalidRequestException("No Salesforce opportunity with id: " + sfId);
        }
        copyOpportunityToJobOpp(op, salesforceJobOpp);

        return salesforceJobOppRepository.save(salesforceJobOpp);
    }

    @Nullable
    @Override
    public SalesforceJobOpp getOrCreateJobOppFromId(String sfId) {
        SalesforceJobOpp jobOpp;
        if (sfId == null || sfId.trim().length() == 0) {
            jobOpp = null;
        } else {
            //Search for existing SalesforceJobOpp associated with this Salesforce record
            jobOpp = getJobOppById(sfId);
            if (jobOpp == null) {
                //Create one if none exists
                jobOpp = createJobOpp(sfId);

                LogBuilder.builder(log)
                    .action("CreateJobOppFromId")
                    .message("Created job " + jobOpp.getName() + "(" + jobOpp.getId() + ")")
                    .logInfo();
            }
        }
        return jobOpp;
    }

    @Override
    @Nullable
    public SalesforceJobOpp getOrCreateJobOppFromLink(String sfJoblink) {
        SalesforceJobOpp jobOpp;
        if (sfJoblink == null || sfJoblink.trim().length() == 0) {
            jobOpp = null;
        } else {
            String sfId = SalesforceHelper.extractIdFromSfUrl(sfJoblink);
            if (sfId == null) {
                throw new InvalidRequestException("Not a valid link to a Salesforce opportunity: " + sfJoblink);
            }
            jobOpp = getOrCreateJobOppFromId(sfId);
        }
        return jobOpp;
    }

    @Override
    public SalesforceJobOpp updateJob(SalesforceJobOpp sfJobOpp) {
        Opportunity op = salesforceService.fetchJobOpportunity(sfJobOpp.getSfId());
        if (op != null) {
            copyOpportunityToJobOpp(op, sfJobOpp);
        }
        return salesforceJobOppRepository.save(sfJobOpp);
    }

    /**
     * Copies a Salesforce opportunity record to a SalesforceJobOpp
     * @param op Salesforce opportunity retrieved from Salesforce
     * @param salesforceJobOpp Cached job opp on our DB
     */
    private void copyOpportunityToJobOpp(@NonNull Opportunity op, SalesforceJobOpp salesforceJobOpp) {
        //Update DB with data from op
        salesforceJobOpp.setName(op.getName());
        salesforceJobOpp.setAccountId(op.getAccountId());
        salesforceJobOpp.setOwnerId(op.getOwnerId());
        salesforceJobOpp.setClosed(op.isClosed());
        salesforceJobOpp.setClosingComments(op.getClosingComments());
        salesforceJobOpp.setNextStep(op.getNextStep());
        salesforceJobOpp.setWon(op.isWon());
        salesforceJobOpp.setHiringCommitment(op.getHiringCommitment());
        salesforceJobOpp.setOpportunityScore(op.getOpportunityScore());
        JobOpportunityStage stage;
        try {
            stage = JobOpportunityStage.textToEnum(op.getStageName());
        } catch (IllegalArgumentException e) {
            LogBuilder.builder(log)
                .action("UpdateJobOpp")
                .message("Error decoding stage in update: " + op.getStageName())
                .logError(e);

            stage = JobOpportunityStage.prospect;
        }
        salesforceJobOpp.setStage(stage);

        final String nextStepDueDate = op.getNextStepDueDate();
        if (nextStepDueDate != null) {
            try {
                salesforceJobOpp.setNextStepDueDate(LocalDate.parse(nextStepDueDate));
            } catch (DateTimeParseException ex) {
                LogBuilder.builder(log)
                    .action("UpdateJobOpp")
                    .message("Error decoding nextStepDueDate: " + nextStepDueDate + " in job op " + op.getName())
                    .logError(ex);
            }
        }

        final String createdDate = op.getCreatedDate();
        if (createdDate != null) {
            try {
                final OffsetDateTime date = SalesforceHelper.parseSalesforceOffsetDateTime(createdDate);
                salesforceJobOpp.setCreatedDate(date);

                final OffsetDateTime publishedDate = salesforceJobOpp.getPublishedDate();
                if (publishedDate == null) {
                    //Set published date to created date if job is an old one - pre publishing
                    if (date != null && date.isBefore(FIRST_PUBLISHED_JOB_DATE)) {
                        salesforceJobOpp.setPublishedDate(date);
                    }
                }
            } catch (DateTimeParseException ex) {
                LogBuilder.builder(log)
                    .action("UpdateJobOpp")
                    .message("Error decoding createdDate from SF: " + createdDate + " in job op " + op.getName())
                    .logError(ex);
            }
        }

        final String lastModifiedDate = op.getLastModifiedDate();
        if (lastModifiedDate != null) {
            try {
                //Parse special non-standard Salesforce offset date time format
                salesforceJobOpp.setUpdatedDate(SalesforceHelper.parseSalesforceOffsetDateTime(lastModifiedDate));
            } catch (DateTimeParseException ex) {
                LogBuilder.builder(log)
                    .action("UpdateJobOpp")
                    .message("Error decoding lastModifiedDate: " + lastModifiedDate + " in job op " + op.getName())
                    .logError(ex);
            }
        }
    }
}
