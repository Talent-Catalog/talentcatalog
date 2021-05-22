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

package org.tbbtalent.server.model.sf;

import org.tbbtalent.server.model.db.Candidate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a Salesforce Contact corresponding to a TBB candidate.
 * Contains the relevant data returned from Salesforce - most importantly
 * the Salesforce id, from which the Salesforce url (the sflink) can be
 * computed.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class Contact {
    static final String urlRoot = "https://talentbeyondboundaries.lightning.force.com/lightning/r/Contact/";
    static final String urlSuffix = "/view";
    
    public String AccountId;
    public String Id;
    public Long TBBid__c;

    public Contact() {
    }

    public Contact(Candidate candidate) {
        TBBid__c = Long.valueOf(candidate.getCandidateNumber());
    }

    public String getUrl() {
        String url = null;
        if (Id != null) {
            url = urlRoot + Id + urlSuffix;
        }
        return url; 
    }
}
