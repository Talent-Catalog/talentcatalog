/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.security.GeneralSecurityException;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.model.sf.Contact;

/**
 * Access to Salesforce.
 *
 * @author John Cameron
 */
public interface SalesforceService {

    /**
     * Searches for a Salesforce Contact record corresponding to the candidate
     * with the given candidate number
     * @param candidateNumber Number of candidate - maps to TBBid in Salesforce
     * @return Salesforce contact, null if none
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    @Nullable
    Contact findContact(@NonNull String candidateNumber) 
            throws GeneralSecurityException, WebClientException;

    /**
     * Creates a Salesforce Contact record corresponding to the candidate
     * with the given candidate number
     * @param candidateNumber Number of candidate - maps to TBBid in Salesforce
     * @return Created Salesforce contact
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    @NonNull
    Contact createContact(@NonNull String candidateNumber)
            throws GeneralSecurityException, WebClientException;
        
}
