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

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.configuration.SalesforceConfig;
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
    private final SalesforceJobOppRepository salesforceJobOppRepository;
    private final SalesforceService salesforceService;
    private final SalesforceConfig salesforceConfig;
    private final CountryRepository countryRepository;
    
    private final EmailHelper emailHelper;

    public SalesforceJobOppServiceImpl(SalesforceJobOppRepository salesforceJobOppRepository,
        SalesforceService salesforceService, SalesforceConfig salesforceConfig, CountryRepository countryRepository,
        EmailHelper emailHelper) {
        this.salesforceJobOppRepository = salesforceJobOppRepository;
        this.salesforceService = salesforceService;
        this.salesforceConfig = salesforceConfig;
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
        final String sfCountryName = op.getAccountCountry();
        salesforceJobOpp.setCountry(sfCountryName);
        salesforceJobOpp.setEmployer(op.getAccountName());
        salesforceJobOpp.setAccountId(op.getAccountId());
        salesforceJobOpp.setOwnerId(op.getOwnerId());
        salesforceJobOpp.setClosed(op.isClosed());
        salesforceJobOpp.setHiringCommitment(op.getHiringCommitment());
        salesforceJobOpp.setEmployerWebsite(op.getAccountWebsite());
        salesforceJobOpp.setEmployerHiredInternationally(op.getAccountHasHiredInternationally());
        JobOpportunityStage stage;
        try {
            stage = JobOpportunityStage.textToEnum(op.getStageName());
        } catch (IllegalArgumentException e) {
            log.error("Error decoding stage in update: " + op.getStageName(), e);
            stage = JobOpportunityStage.prospect;
        }
        salesforceJobOpp.setStage(stage);
        salesforceJobOpp.setLastUpdate(OffsetDateTime.now());

        //Post processing
        
        // Match a country object with the country name from Salesforce.
        Country country = this.countryRepository.findByNameIgnoreCase(sfCountryName);
        salesforceJobOpp.setCountryObject(country);
        if (country == null ){
             emailHelper.sendAlert("Salesforce country " + sfCountryName + "not found in database.");
        } 
    }
}
