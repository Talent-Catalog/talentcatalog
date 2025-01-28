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

package org.tctalent.server.service.db;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.request.candidate.UpdateCandidateOppsRequest;
import org.tctalent.server.request.candidate.dependant.UpdateRelocatingDependantIds;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;

public interface CandidateOpportunityService {

    /**
     * Creates or updates Contact records on Salesforce for the given candidates and, if sfJobOpp
     * is not null, indicating that these candidates are associated with a job opportunity,
     * this will also create/update the associated candidate opportunities associated with that
     * job on both Salesforce and on the local database.
     *
     * @param candidates Candidates to update
     * @param sfJobOpp If not null candidate opportunities are created/updated
     * @param candidateOppParams Used to create/update candidate opportunities.
     *                           Can be null in which case no changes are made to existing opps,
     *                           but new opps will be created if needed with the stage defaulting
     *                           to "prospect".
     * @throws SalesforceException If there are errors relating to keys and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    void createUpdateCandidateOpportunities(Collection<Candidate> candidates,
        @Nullable SalesforceJobOpp sfJobOpp, @Nullable CandidateOpportunityParams candidateOppParams)
        throws SalesforceException, WebClientException;

    /**
     * See {@link #createUpdateCandidateOpportunities(Collection, SalesforceJobOpp, CandidateOpportunityParams)}
     * <p/>
     * This method extracts the appropriate data from the given request and then calls the
     * above method.
     *
     * @param request Identifies candidates as well as optional Salesforce fields to set on
     *                candidate opportunities
     * @throws SalesforceException If there are errors relating to keys and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    void createUpdateCandidateOpportunities(UpdateCandidateOppsRequest request)
        throws SalesforceException, WebClientException;

    /**
     * From Salesforce fetch the Salesforce id, if any, of the SF opp corresponding to the given
     * TC candidate opp.
     *
     * @param opp Candidate opp on the TC
     * @return Salesforce id if one found
     */
    @Nullable
    String fetchSalesforceId(@NonNull CandidateOpportunity opp);

    /**
     * Finds the candidate opportunity associated with the given candidate and job
     *
     * @param candidate Candidate
     * @param jobOpp    Job
     * @return Candidate opportunity, may be null if none found.
     */
    @Nullable
    CandidateOpportunity findOpp(Candidate candidate, SalesforceJobOpp jobOpp);

    /**
     * Finds all opps associated with the given job creator partner
     * @param partner Job creator partner
     * @return Candidate opps associated with partner. Empty if none or if partner is null or not a job creator.
     */
    @NonNull
    List<CandidateOpportunity> findJobCreatorPartnerOpps(@Nullable Partner partner);

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
     * Creates or updates a CandidateOpportunity on the TC associated with given opportunity
     * retrieved from Salesforce.
     * <p/>
     * This will also create the associated job opportunity on the TC if it is not already present.
     * @param op Candidate opportunity data retrieved from Salesforce
     * @return Updated or created CandidateOpportunity
     * @throws SalesforceException if there are issues contacting Salesforce
     */
    @NonNull
    CandidateOpportunity loadCandidateOpportunity(Opportunity op) throws SalesforceException;

    /**
     * This loads the last active stages of all cases from Salesforce.
     */
    void loadCandidateOpportunityLastActiveStages();

    /**
     * Returns the ids of chats not fully read by the currently logged in user, which are
     * associated with candidate opportunities returned from the given search request.
     * @param request - Search Request
     * @return Ids of unread chats
     */
    List<Long> findUnreadChatsInOpps(SearchCandidateOpportunityRequest request);

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

    /**
     * Uploads the given file to the Candidate folder associated with the
     * candidate opportunity with the given id.
     * <p/>
     * @param id ID of candidate opportunity
     * @param file File containing the job offer contract
     * @return Updated candidate opportunity
     * @throws NoSuchObjectException if there is no Job with this id.
     * @throws IOException           if there is a problem uploading the file.
     * @throws InvalidRequestException if the job does not have a submission list
     */
    CandidateOpportunity uploadOffer(long id, MultipartFile file)
        throws InvalidRequestException, NoSuchObjectException, IOException;

    /**
     * Updates the CandidateOpportunity with the relocating dependants
     * @param request relocating dependant ids
     * @return CandidateOpportunity
     * @throws NoSuchObjectException if there is no opportunity with this id.
     */
    CandidateOpportunity updateRelocatingDependants(long id, UpdateRelocatingDependantIds request)
        throws NoSuchObjectException;

    /**
     * Processes a batch update of TC Opps from their Salesforce equivalents. Iterates through the
     * provided Salesforce Opps, fetching the TC equivalent and updating accordingly.
     * @param oppBatch List of Opportunities fetched from Salesforce
     */
    void processCaseUpdateBatch(List<Opportunity> oppBatch);

    /**
     * Finds all open TC Candidate Opps with a linked SF Opp
     * @return List of sfIds for all Candidate Opps matching the query criteria
     */
    List<String> findAllNonNullSfIdsByClosedFalse();
}
