/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.sf.Contact;

/**
 * Access to Salesforce.
 *
 * @author John Cameron
 */
public interface SalesforceService {

    /**
     * Searches Salesforce for all Contact records relating to a TBB 
     * candidate.
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
    List<Contact> createOrUpdateContacts(@NonNull List<Candidate> candidates)
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
