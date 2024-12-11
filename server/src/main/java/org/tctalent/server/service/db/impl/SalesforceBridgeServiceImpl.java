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

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SalesforceBridgeService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;

@Service
@Slf4j
public class SalesforceBridgeServiceImpl implements SalesforceBridgeService {

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
                LogBuilder.builder(log)
                    .action("SalesforceBridgeServiceImpl")
                    .message("Candidate number " + candidateNumber +
                        " referred to in Salesforce opp " + opp.getName() + " not found on TC")
                    .logError();
            } else {
                String contextNote = "Considered for role: " + opp.getName();
                savedListService.addCandidateToList(list, candidate, contextNote);
            }
        }
        return savedListService.saveIt(list);
    }

}
