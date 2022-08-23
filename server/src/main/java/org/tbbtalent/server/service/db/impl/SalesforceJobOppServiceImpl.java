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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.configuration.SalesforceConfig;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.JobOpportunityStage;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.sf.Opportunity;
import org.tbbtalent.server.repository.db.SalesforceJobOppRepository;
import org.tbbtalent.server.service.db.SalesforceJobOppService;
import org.tbbtalent.server.service.db.SalesforceService;

@Service
public class SalesforceJobOppServiceImpl implements SalesforceJobOppService {
    private static final Logger log = LoggerFactory.getLogger(SalesforceJobOppServiceImpl.class);
    private final SalesforceJobOppRepository salesforceJobOppRepository;
    private final SalesforceService salesforceService;
    private final SalesforceConfig salesforceConfig;

    public SalesforceJobOppServiceImpl(SalesforceJobOppRepository salesforceJobOppRepository,
        SalesforceService salesforceService, SalesforceConfig salesforceConfig) {
        this.salesforceJobOppRepository = salesforceJobOppRepository;
        this.salesforceService = salesforceService;
        this.salesforceConfig = salesforceConfig;
    }

    @Nullable
    @Override
    public SalesforceJobOpp getJobOppById(String sfId) {
        return salesforceJobOppRepository.findById(sfId).orElse(null);
    }

    @Nullable
    @Override
    public SalesforceJobOpp getJobOppByUrl(String sfUrl) {
        return getJobOppById(SalesforceServiceImpl.extractIdFromSfUrl(sfUrl));
    }

    @Override
    public SalesforceJobOpp createExpiringOpp(String sfId) {
        SalesforceJobOpp salesforceJobOpp = new SalesforceJobOpp();
        salesforceJobOpp.setId(sfId);
        return salesforceJobOppRepository.save(salesforceJobOpp);
    }

    @Override
    @NonNull
    public SalesforceJobOpp createJobOpp(String sfId)
        throws InvalidRequestException, SalesforceException {
        SalesforceJobOpp salesforceJobOpp = new SalesforceJobOpp();
        salesforceJobOpp.setId(sfId);

        Opportunity op = salesforceService.fetchOpportunity(sfId);
        if (op == null) {
            throw new InvalidRequestException("No Salesforce opportunity with id: " + sfId);
        }
        copyOpportunityToJobOpp(op, salesforceJobOpp);

        return salesforceJobOppRepository.save(salesforceJobOpp);
    }

    @Override
    @Nullable
    public SalesforceJobOpp getOrCreateJobOppFromLink(String sfJoblink) {
        SalesforceJobOpp jobOpp;
        if (sfJoblink == null) {
            jobOpp = null;
        } else {
            String sfId = SalesforceServiceImpl.extractIdFromSfUrl(sfJoblink);
            if (sfId == null) {
                throw new InvalidRequestException("Not a valid link to a Salesforce opportunity: " + sfJoblink);
            }
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
    public void update(List<String> sfIds) throws SalesforceException {

        //Use ids to look up existing records in DB
        //If record has expired (TTL), add it to list of opp ids to be fetched
        List<String> expiredSfIds = new ArrayList<>();
        for (String sfId : sfIds) {
            final SalesforceJobOpp salesforceJobOpp = salesforceJobOppRepository.findById(sfId)
                .orElse(null);
            if (salesforceJobOpp == null) {
                //Ignore id's if we don't already have a cached entry for them.
                //New cached entries are not created here.
                log.error("Unexpected missing cache entry for Salesforce id: " + sfId);
            } else {
                if (isExpired(salesforceJobOpp)) {
                    expiredSfIds.add(sfId);
                }
            }
        }

        if (!expiredSfIds.isEmpty()) {
            List<Opportunity> ops = salesforceService.fetchOpportunities(expiredSfIds);

            for (Opportunity op : ops) {
                String id = op.getId();
                //Fetch DB with id
                SalesforceJobOpp salesforceJobOpp = salesforceJobOppRepository.findById(id)
                    .orElse(null);
                if (salesforceJobOpp != null) {
                    copyOpportunityToJobOpp(op, salesforceJobOpp);
                    salesforceJobOppRepository.save(salesforceJobOpp);
                }
            }
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
        salesforceJobOpp.setCountry(op.getAccountCountry__c());
        salesforceJobOpp.setEmployer(op.getAccountName__c());
        JobOpportunityStage stage;
        try {
            stage = JobOpportunityStage.textToEnum(op.getStageName());
        } catch (IllegalArgumentException e) {
            log.error("Error decoding stage in update: " + op.getStageName(), e);
            stage = JobOpportunityStage.prospect;
        }
        salesforceJobOpp.setStage(stage);
        salesforceJobOpp.setLastUpdate(OffsetDateTime.now());
    }

    private boolean isExpired(SalesforceJobOpp salesforceJobOpp) {
        OffsetDateTime lastUpdate = salesforceJobOpp.getLastUpdate();
        boolean expired;
        if (lastUpdate == null) {
            expired = true;
        } else {
            Duration difference = Duration.between(lastUpdate, OffsetDateTime.now());
            long seconds = difference.getSeconds();
            expired = seconds > salesforceConfig.getTimeToLive();
        }
        return expired;
    }
}
