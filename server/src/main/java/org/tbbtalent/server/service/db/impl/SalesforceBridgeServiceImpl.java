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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.sf.Opportunity;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.SalesforceBridgeService;
import org.tbbtalent.server.service.db.SalesforceService;
import org.tbbtalent.server.service.db.SavedListService;

@Service
public class SalesforceBridgeServiceImpl implements SalesforceBridgeService {
    private static final Logger log = LoggerFactory.getLogger(SalesforceBridgeServiceImpl.class);

    private final CandidateService candidateService;
    private final SalesforceService salesforceService;
    private final SavedListService savedListService;

    public SalesforceBridgeServiceImpl(CandidateService candidateService, SalesforceService salesforceService, SavedListService savedListService) {
        this.candidateService = candidateService;
        this.salesforceService = salesforceService;
        this.savedListService = savedListService;
    }

    @Override
    @NonNull
    public SavedList findSeenCandidates(String listName, String accountId)
        throws NoSuchObjectException, SalesforceException, InvalidRequestException {
        List<Opportunity> opps;
        opps = salesforceService.findCandidateOpportunities("AccountId='" + accountId + "'");

        UpdateSavedListInfoRequest req = new UpdateSavedListInfoRequest();
        req.setName(listName);
        SavedList list = savedListService.createSavedList(req);

        for (Opportunity opp : opps) {
            String candidateNumber = opp.getCandidateId();
            Candidate candidate = candidateService.findByCandidateNumber(candidateNumber);
            if (candidate == null) {
                log.error("Candidate number " + candidateNumber +
                    " referred to in Salesforce opp " + opp.getName() + " not found on TC");
            } else {
                String contextNote = "Considered for role: " + opp.getName();
                savedListService.addCandidateToList(list, candidate, contextNote);
            }
        }
        return savedListService.saveIt(list);
    }

}
