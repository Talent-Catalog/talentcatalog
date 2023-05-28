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
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateOpportunity;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.request.candidate.SalesforceOppParams;
import org.tbbtalent.server.request.candidate.UpdateCandidateOppsRequest;

public interface CandidateOpportunityService {

    /**
     * Creates or updates CandidateOpportunities associated with the given candidates going for
     * the given job, using the given opportunity data.
     * @param candidates Candidates whose opportunities are going to be created or updated
     * @param oppParams Opportunity data common to all opportunities
     * @param jobOpp Job associated with candidate opportunities
     */
    void createOrUpdateCandidateOpportunities(
        List<Candidate> candidates, SalesforceOppParams oppParams, SalesforceJobOpp jobOpp);

    /**
     * Creates or updates Contact records on Salesforce for the given candidates and, if sfJobOpp
     * is not null, indicating that these candidates are associated with a job opportunity,
     * this will also create/update the associated candidate opportunities associated with that
     * job.
     *
     * @param candidates Candidates to update
     * @param sfJobOpp If not null the candidate opportunities are created/updated
     * @param salesforceOppParams Used to create/update candidate opportunities
     * @throws SalesforceException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    void createUpdateSalesforce(Collection<Candidate> candidates,
        @Nullable SalesforceJobOpp sfJobOpp, @Nullable SalesforceOppParams salesforceOppParams)
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
     * Creates or updates CandidateOpportunities associated with given jobs from Salesforce.
     * @param jobOpportunityIds IDs of Jobs whose associated CandidateOpportunities should be
     *                          updated from Salesforce
     * @throws SalesforceException if there are issues contacting Salesforce
     */
    void loadCandidateOpportunities(String... jobOpportunityIds) throws SalesforceException;
}
