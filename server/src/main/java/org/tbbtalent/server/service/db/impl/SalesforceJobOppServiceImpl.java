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

import static org.tbbtalent.server.util.SalesforceHelper.parseSalesforceOffsetDateTime;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.JobOpportunityStage;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.sf.Opportunity;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.repository.db.SalesforceJobOppRepository;
import org.tbbtalent.server.service.db.SalesforceJobOppService;
import org.tbbtalent.server.service.db.SalesforceService;
import org.tbbtalent.server.service.db.email.EmailHelper;
import org.tbbtalent.server.util.SalesforceHelper;

@Service
public class SalesforceJobOppServiceImpl implements SalesforceJobOppService {
    private static final Logger log = LoggerFactory.getLogger(SalesforceJobOppServiceImpl.class);
    private static final OffsetDateTime FIRST_PUBLISHED_JOB_DATE =
        OffsetDateTime.parse("2022-11-01T00:00:00Z", DateTimeFormatter.ISO_DATE_TIME);
    private final SalesforceJobOppRepository salesforceJobOppRepository;
    private final SalesforceService salesforceService;
    private final CountryRepository countryRepository;

    private final EmailHelper emailHelper;

    public SalesforceJobOppServiceImpl(SalesforceJobOppRepository salesforceJobOppRepository,
        SalesforceService salesforceService, CountryRepository countryRepository,
        EmailHelper emailHelper) {
        this.salesforceJobOppRepository = salesforceJobOppRepository;
        this.salesforceService = salesforceService;
        this.countryRepository = countryRepository;
        this.emailHelper = emailHelper;
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
                log.info("Created job " + jobOpp.getName() + "(" + jobOpp.getId() + ")");
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

    @Override
    public void updateJobs(Collection<String> sfIds) throws SalesforceException {
        if (sfIds != null && !sfIds.isEmpty()) {
            log.info("Updating job opportunities from Salesforce");

            //Get SF opportunities from SF that we will use to do the updates
            List<Opportunity> ops = salesforceService.fetchJobOpportunitiesByIdOrOpenOnSF(sfIds);

            log.info("Loaded " + ops.size() + " job opportunities from Salesforce");
            int count = 0;
            int updates = 0;
            for (Opportunity op : ops) {
                String id = op.getId();
                //Fetch DB with id
                SalesforceJobOpp salesforceJobOpp = salesforceJobOppRepository.findBySfId(id)
                    .orElse(null);
                if (salesforceJobOpp != null) {
                    copyOpportunityToJobOpp(op, salesforceJobOpp);
                    salesforceJobOppRepository.save(salesforceJobOpp);
                    updates++;
                }
                count++;
                if (count%100 == 0) {
                    log.info("Processed " + count + " job opportunities from Salesforce");
                }
            }
            log.info("Updated " + updates + " job opportunities from Salesforce");
        }
    }

    /**
     * Copies a Salesforce opportunity record to a SalesforceJobOpp
     * @param op Salesforce opportunity retrieved from Salesforce
     * @param salesforceJobOpp Cached job opp on our DB
     */
    private void copyOpportunityToJobOpp(@NonNull Opportunity op, SalesforceJobOpp salesforceJobOpp) {
        //Update DB with data from op
        salesforceJobOpp.setName(op.getName());
        salesforceJobOpp.setEmployer(op.getAccountName());
        salesforceJobOpp.setAccountId(op.getAccountId());
        salesforceJobOpp.setOwnerId(op.getOwnerId());
        salesforceJobOpp.setClosed(op.isClosed());
        salesforceJobOpp.setClosingComments(op.getClosingComments());
        salesforceJobOpp.setNextStep(op.getNextStep());
        salesforceJobOpp.setWon(op.isWon());
        salesforceJobOpp.setHiringCommitment(op.getHiringCommitment());
        salesforceJobOpp.setOpportunityScore(op.getOpportunityScore());
        salesforceJobOpp.setEmployerWebsite(op.getAccountWebsite());
        salesforceJobOpp.setEmployerHiredInternationally(op.getAccountHasHiredInternationally());
        salesforceJobOpp.setEmployerDescription(op.getAccount() == null ? null : op.getAccount().getDescription());
        JobOpportunityStage stage;
        try {
            stage = JobOpportunityStage.textToEnum(op.getStageName());
        } catch (IllegalArgumentException e) {
            log.error("Error decoding stage in update: " + op.getStageName(), e);
            stage = JobOpportunityStage.prospect;
        }
        salesforceJobOpp.setStage(stage);

        final String nextStepDueDate = op.getNextStepDueDate();
        if (nextStepDueDate != null) {
            try {
                salesforceJobOpp.setNextStepDueDate(LocalDate.parse(nextStepDueDate));
            } catch (DateTimeParseException ex) {
                log.error("Error decoding nextStepDueDate: " + nextStepDueDate + " in job op " + op.getName());
            }
        }

        final String createdDate = op.getCreatedDate();
        if (createdDate != null) {
            try {
                final OffsetDateTime date = parseSalesforceOffsetDateTime(createdDate);
                salesforceJobOpp.setCreatedDate(date);

                final OffsetDateTime publishedDate = salesforceJobOpp.getPublishedDate();
                if (publishedDate == null) {
                    //Set published date to created date if job is an old one - pre publishing
                    if (date != null && date.isBefore(FIRST_PUBLISHED_JOB_DATE)) {
                        salesforceJobOpp.setPublishedDate(date);
                    }
                }
            } catch (DateTimeParseException ex) {
                log.error("Error decoding createdDate from SF: " + createdDate + " in job op " + op.getName());
            }
        }

        final String lastModifiedDate = op.getLastModifiedDate();
        if (lastModifiedDate != null) {
            try {
                //Parse special non-standard Salesforce offset date time format
                salesforceJobOpp.setUpdatedDate(parseSalesforceOffsetDateTime(lastModifiedDate));
            } catch (DateTimeParseException ex) {
                log.error("Error decoding lastModifiedDate: " + lastModifiedDate + " in job op " + op.getName());
            }
        }

        //Post-processing

        // Match a country object with the country name from Salesforce.
        final String sfCountryName = op.getAccountCountry();
        Country country = this.countryRepository.findByNameIgnoreCase(sfCountryName);
        salesforceJobOpp.setCountry(country);
        if (country == null ){
             emailHelper.sendAlert("Salesforce country " + sfCountryName +
                 " in Job Opp " + op.getName() + " not found in database.");
        }
    }
}
