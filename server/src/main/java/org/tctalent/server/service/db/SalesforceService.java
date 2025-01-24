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

import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.sf.Account;
import org.tctalent.server.model.sf.Contact;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.model.sf.Opportunity.OpportunityType;
import org.tctalent.server.model.sf.OpportunityHistory;
import org.tctalent.server.request.candidate.EmployerCandidateFeedbackData;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.opportunity.UpdateEmployerOpportunityRequest;

/**
 * Access to Salesforce.
 * <p/>
 * Note that our Salesforce configuration supports different types of opportunity, including
 * Employer opportunities and Candidate opportunities. However, they are all just different
 * versions (defined by the Salesforce "RecordType") of the standard Salesforce Opportunity record.
 *
 * @author John Cameron
 */
public interface SalesforceService {

    /**
     * Fetches opportunities with fields populated from Salesforce.
     * <p/>
     * The opportunities fetched are those with the specified ids plus recently changed open opps.
     * @param sfIds Ids of requested Salesforce records
     * @return Opportunities
     */
    List<Opportunity> fetchOpportunitiesByIdOrOpenOnSF(Collection<String> sfIds, OpportunityType type);

    /**
     * Fetches opportunity with the given id from Salesforce.
     * @param id Salesforce id (not url)
     * @return Opportunity or null if none found
     * @throws SalesforceException if there is a problem accessing Salesforce
     */
    @Nullable
    Opportunity fetchJobOpportunity(String id) throws SalesforceException;

    /**
     * Searches Salesforce for all Contact records relating to TBB
     * candidates.
     * <p/>
     * Contact records of candidates are identified because they have a non zero TBBid.
     * @return List of Salesforce Contact records
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    List<Contact> findCandidateContacts() throws WebClientException;

    /**
     * Searches Salesforce for a candidate opportunity associated with the given candidate and job.
     * @param candidateNumber Candidate number
     * @param jobSfId SFId of job
     * @return Opportunity if one found, otherwise null.
     */
    @Nullable
    Opportunity findCandidateOpportunity(String candidateNumber, String jobSfId);

    /**
     * Searches Salesforce for all Candidate Opportunity records matching the given condition.
     *
     * @param condition Effectively the logical (predicate) part of a SOQL WHERE clause.
     * @return List of Salesforce Candidate Opportunity records
     * @throws WebClientException       if there is a problem connecting to Salesforce
     */
    @NonNull
    List<Opportunity> findCandidateOpportunities(String condition) throws WebClientException;

    /**
     * Searches Salesforce for Candidate Opportunity records matching the given condition.
     * Up to limit records are returned.
     * <p/>
     * Records are ordered by ascending Id. This can be used to page through large numbers of
     * records by changing the condition to take into account the last Id loaded.
     *
     * @param condition Effectively the logical (predicate) part of a SOQL WHERE clause.
     * @param limit Maximum number of records to be returned.
     * @return List of Salesforce Candidate Opportunity records
     * @throws WebClientException       if there is a problem connecting to Salesforce
     */
    List<Opportunity> findCandidateOpportunities(String condition, int limit);

    /**
     * Searches Salesforce for all Candidate Opportunity records associated with the given
     * Salesforce job ids.
     *
     * @param jobOpportunityIds One or more Salesforce job ids
     * @return List of Salesforce Candidate Opportunity records
     * @throws SalesforceException If there is a problem reported by Salesforce
     */
    @NonNull
    List<Opportunity> findCandidateOpportunitiesByJobOpps(String... jobOpportunityIds)
        throws SalesforceException;

    /**
     * Searches Salesforce for all Contact records matching the given condition.
     *
     * @param condition Effectively the logical (predicate) part of a SOQL WHERE clause. eg LastName
     *                  = 'Cameron'
     * @return List of Salesforce Contact records
     * @throws GeneralSecurityException If there are errors relating to keys and digital signing.
     * @throws WebClientException       if there is a problem connecting to Salesforce
     */
    @NonNull
    List<Contact> findContacts(String condition)
            throws GeneralSecurityException, WebClientException;

    /**
     * Searches for a Salesforce Contact record corresponding to the given
     * candidate
     * @param candidate Candidate - candidate number maps to TBBid in Salesforce
     * @return Salesforce contact, null if none
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    @Nullable
    Contact findContact(@NonNull Candidate candidate)
            throws GeneralSecurityException, WebClientException;

    /**
     * Searches for a Salesforce Account record (Employer)
     * corresponding to the given Salesforce id
     * @param sfId Salesforce id
     * @return Account, null if none
     * @throws SalesforceException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    @Nullable
    Account findAccount(String sfId)
            throws SalesforceException, WebClientException;

    /**
     * Searches for a Salesforce Opportunity record (Employer or Candidate opportunity)
     * corresponding to the given Salesforce id
     * @param sfId Salesforce id
     * @return Salesforce opportunity, null if none
     * @throws SalesforceException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    @Nullable
    Opportunity findOpportunity(String sfId)
            throws SalesforceException, WebClientException;

    /**
     * Searches for a Salesforce Opportunity History records corresponding to the given Salesforce
     * opportunity ids.
     * @param opportunityIds Salesforce ids
     * @return List of opportunity history records in descending order of
     * OpportunityId, SystemModstamp (ie time stamp). So for a given opportunity id, the most recent
     * history entries come first.
     * @throws SalesforceException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    @NonNull
    List<OpportunityHistory> findOpportunityHistories(List<String> opportunityIds)
            throws SalesforceException, WebClientException;

    /**
     * Searches for all active Salesforce Employer Job Opportunities.
     * <p/>
     * Employ job opportunities are identified as those with Record Type = "Employer job"
     * @return List of Salesforce Employer Job Opportunity records.
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    List<Opportunity> findJobOpportunities()
            throws GeneralSecurityException, WebClientException;

    /**
     * Returns an object containing the requested fields of the Salesforce
     * record with the given type and the given id.
     * <p/>
     * Note that fields can traverse relationships - eg Account.Name
     * See <a href="https://developer.salesforce.com/docs/atlas.en-us.226.0.soql_sosl.meta/soql_sosl/sforce_api_calls_soql_relationships_understanding.htm">...</a>
     * <p/>
     * Note that the object needs to match the returned Json. See above doc.
     * @param objectType Salesforce object. For example 'Contact',
     *                   'Opportunity', 'Account' etc
     * @param id Salesforce id.
     * @param fields Fields required
     * @param cl Class of object type T
     * @return Object containing the requested fields.
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    @Nullable
    <T> T findRecordFieldsFromId(
            String objectType, String id, String fields, Class<T> cl)
            throws GeneralSecurityException, WebClientException;

    /**
     * Generates a standard candidate opportunity name from the given candidate and job opportunity.
     * @param candidate Candidate who is going for the job
     * @param jobOpp Job opportunity
     * @return Generated candidate opportunity name
     */
    String generateCandidateOppName(@NonNull Candidate candidate, @NonNull SalesforceJobOpp jobOpp);

    /**
     * Creates or updates the Salesforce Contact record corresponding to the
     * given candidate.
     * @param candidate Candidate - candidate number maps to TBBId__c in Salesforce
     * @return Created/updated Salesforce contact (contains the SF id)
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    @NonNull
    Contact createOrUpdateContact(@NonNull Candidate candidate)
            throws WebClientException, SalesforceException;

    /**
     * Creates or updates the Salesforce Contact records corresponding to the
     * given candidates.
     * @param candidates Candidates - candidate number maps to TBBId__c in Salesforce
     * @return Created/updated Salesforce contacts (containing the SF id unless
     * there were problems in which case the id is null).
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    @NonNull
    List<Contact> createOrUpdateContacts(@NonNull Collection<Candidate> candidates)
            throws WebClientException, SalesforceException;

    /**
     * Creates or updates the Salesforce job opportunity on Salesforce.
     * @param job TC job opportunity.
     * @return Salesforce id (sfId) corresponding to created/updates Salesforce record
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    @NonNull
    String createOrUpdateJobOpportunity(SalesforceJobOpp job);

    /**
     * Creates or updates the Salesforce Candidate Opportunity records corresponding to the
     * given candidates for the given Employer job opportunity.
     * <p/>
     * Note the candidate job opportunities are identified by the unique
     * external id TBBCandidateExternalId__c (constructed from the candidate number and the
     * SF job id)
     *
     * @param candidates Candidates
     * @param candidateOppParams Optional Salesforce fields to set on all given candidates'
     *                            opportunities
     * @param jobOpp Employer job opportunity on Salesforce
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data,
     * including if sfJoblink is not a valid link to a Salesforce employer job opportunity.
     */
    void createOrUpdateCandidateOpportunities(List<Candidate> candidates,
        @Nullable CandidateOpportunityParams candidateOppParams, SalesforceJobOpp jobOpp)
            throws WebClientException, SalesforceException;

    /**
     * Updates the Salesforce Contact record corresponding to the given candidate.
     * @param candidate Candidate - candidate number maps to TBBid in Salesforce
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    void updateContact(Candidate candidate) throws GeneralSecurityException;

    /**
     * Updates the Salesforce Candidate Opportunity records based on the given employer feedback
     * on each candidate.
     * <p/>
     * Note the candidate job opportunities are identified by the unique
     * external id TBBCandidateExternalId__c
     *
     * @param feedbacks Employer feedback on candidates
     * @param sfJobOpp Employer job opportunity on Salesforce
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data,
     * including if sfJoblink is not a valid link to a Salesforce employer job opportunity.
     */
    void updateCandidateOpportunities(
        List<EmployerCandidateFeedbackData> feedbacks, SalesforceJobOpp sfJobOpp)
        throws WebClientException, SalesforceException;

    /**
     * Updates the Salesforce Employer opportunity record corresponding to the sfJoblink in the
     * given request.
     *
     * @param request Contains a link to the opportunity to be updated, plus the data to update.
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    void updateEmployerOpportunity(UpdateEmployerOpportunityRequest request) throws SalesforceException;

    /**
     * Updates the Salesforce Employer opportunity record corresponding to the given Salesforce id
     * to the given stage, next step and due date.
     *
     * @param sfId Salesforce id of opportunity.
     * @param stage New stage
     * @param nextStep New next step
     * @param dueDate Next step due date
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    void updateEmployerOpportunityStage(
        String sfId, JobOpportunityStage stage, String nextStep, LocalDate dueDate)
        throws SalesforceException, WebClientException;

    /**
     * Updates a SF candidate opportunity's relocation information based on info from the
     * candidate opportunity. This section on the SF record reports the age and gender
     * breakdown of all individuals relocating as a result of that opportunity, including
     * the candidate.
     * @param candidateOpportunity the candidate opportunity that is source of the info
     * @throws NoSuchObjectException if there's no candidate dependant with a given id
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    void updateSfCaseRelocationInfo(CandidateOpportunity candidateOpportunity)
        throws NoSuchObjectException, SalesforceException, WebClientException;
}
