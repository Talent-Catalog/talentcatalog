/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.sf.Contact;
import org.tbbtalent.server.model.sf.Opportunity;

import java.security.GeneralSecurityException;
import java.util.List;
import org.tbbtalent.server.request.candidate.SalesforceOppParams;

/**
 * Access to Salesforce.
 *
 * @author John Cameron
 */
public interface SalesforceService {

    /**
     * Searches Salesforce for all Contact records relating to TBB 
     * candidates.
     * @return List of Salesforce Contact records
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    List<Contact> findCandidateContacts()
            throws GeneralSecurityException, WebClientException;

    /**
     * Searches Salesforce for all Contact records matching the given condition.
     * @param condition Effectively the logical (predicate) part of a
     *                  SOQL WHERE clause. eg LastName = 'Cameron'
     * @return List of Salesforce Contact records
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
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
     * Searches for a Salesforce Opportunity record corresponding to the given 
     * Salesforce id
     * @param sfId Salesforce id
     * @return Salesforce opportunity, null if none
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    @Nullable
    Opportunity findOpportunity(String sfId)
            throws GeneralSecurityException, WebClientException;

    /**
     * Returns an object containing the requested fields of the Salesforce 
     * record with the given type and the given id.
     * <p/>
     * Note that fields can traverse relationships - eg Account.Name
     * See https://developer.salesforce.com/docs/atlas.en-us.226.0.soql_sosl.meta/soql_sosl/sforce_api_calls_soql_relationships_understanding.htm
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
     * Creates a Salesforce Contact record corresponding to the given candidate.
     * @param candidate Candidate - candidate number maps to TBBid in Salesforce
     * @return Created Salesforce contact
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    @NonNull
    Contact createContact(@NonNull Candidate candidate)
            throws GeneralSecurityException, WebClientException, SalesforceException;
    /**
     * Creates or updates the Salesforce Contact record corresponding to the 
     * given candidate.
     * @param candidate Candidate - candidate number maps to TBBId__c in Salesforce
     * @return Created/updated Salesforce contact (contains the SF id)
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    @NonNull
    Contact createOrUpdateContact(@NonNull Candidate candidate)
            throws GeneralSecurityException, WebClientException, SalesforceException;

    /**
     * Creates or updates the Salesforce Contact records corresponding to the 
     * given candidates.
     * @param candidates Candidates - candidate number maps to TBBId__c in Salesforce
     * @return Created/updated Salesforce contacts (containing the SF id unless
     * there were problems in which case the id is null).
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    @NonNull
    List<Contact> createOrUpdateContacts(@NonNull Collection<Candidate> candidates)
            throws GeneralSecurityException, WebClientException, SalesforceException;

    /**
     * Creates or updates the Salesforce Opportunity records corresponding to the 
     * given candidates for the given job opportunity.
     * <p/>
     * Note the candidate job opportunities are identified by the unique
     * external id TBBCandidateExternalId__c
     * 
     * @param candidates Candidates
     * @param salesforceOppParams Optional Salesforce fields to set on candidate opportunities           
     * @param sfJoblink url link to Job opportunity on Salesforce
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data,
     * including if sfJoblink is not a valid link to a Salesforce job opportunity.
     */
    void createOrUpdateJobOpportunities(
            List<Candidate> candidates, SalesforceOppParams salesforceOppParams, String sfJoblink)
            throws GeneralSecurityException, WebClientException, SalesforceException;
    
    /**
     * Updates the Salesforce Contact record corresponding to the given candidate.
     * @param candidate Candidate - candidate number maps to TBBid in Salesforce
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    void updateContact(Candidate candidate) throws GeneralSecurityException;
}
