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

package org.tbbtalent.server.service.db;

import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateOpportunity;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.request.candidate.UpdateCandidateOppsRequest;
import org.tbbtalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tbbtalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;

public interface CandidateOpportunityService {
    
    /**
     * Creates or updates Contact records on Salesforce for the given candidates and, if sfJobOpp
     * is not null, indicating that these candidates are associated with a job opportunity,
     * this will also create/update the associated candidate opportunities associated with that
     * job.
     *
     * @param candidates Candidates to update
     * @param sfJobOpp If not null the candidate opportunities are created/updated
     * @param candidateOppParams Used to create/update candidate opportunities
     * @throws SalesforceException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    void createUpdateSalesforce(Collection<Candidate> candidates,
        @Nullable SalesforceJobOpp sfJobOpp, @Nullable CandidateOpportunityParams candidateOppParams)
        throws SalesforceException, WebClientException;

    /**
     * Creates/updates Salesforce records corresponding to the given candidates.
     * <p/>
     * This could involve creating or updating contact records and/or
     * creating or updating opportunity records.
     * <p/>
     * Salesforce links may be created and stored in candidate records.
     *
     * @param request Identifies candidates as well as optional Salesforce fields to set on
     *                candidate opportunities
     * @throws SalesforceException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    void createUpdateSalesforce(UpdateCandidateOppsRequest request)
        throws SalesforceException, WebClientException;

    CandidateOpportunity findOpp(Candidate candidate, SalesforceJobOpp jobOpp);

    /**
     * Get the CandidateOpportunity with the given id.
     * @param id Id of opportunity to get
     * @return CandidateOpportunity
     * @throws NoSuchObjectException if there is no opportunity with this id.
     */
    @NonNull
    CandidateOpportunity getCandidateOpportunity(long id) throws NoSuchObjectException;

    /**
     * Creates or updates CandidateOpportunities associated with given jobs from Salesforce.
     * @param jobOpportunityIds IDs of Jobs whose associated CandidateOpportunities should be
     *                          updated from Salesforce
     * @throws SalesforceException if there are issues contacting Salesforce
     */
    void loadCandidateOpportunities(String... jobOpportunityIds) throws SalesforceException;


    /**
     * Get candidate opportunities from a paged search request
     * @param request - Paged Search Request
     * @return Page of candidate opportunities
     */
    Page<CandidateOpportunity> searchCandidateOpportunities(SearchCandidateOpportunityRequest request);

    /**
     * Updates the CandidateOpportunity with the given id with the data contained in the request
     * @param id Id of opportunity to get
     * @param request Candidate opportunity data
     * @return CandidateOpportunity
     * @throws NoSuchObjectException if there is no opportunity with this id.
     */
    CandidateOpportunity updateCandidateOpportunity(long id, CandidateOpportunityParams request)
        throws NoSuchObjectException;
}
