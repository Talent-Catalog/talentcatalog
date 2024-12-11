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

package org.tctalent.server.model.sf;

import com.fasterxml.jackson.annotation.JsonSetter;
import org.tctalent.server.model.db.Candidate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a Salesforce Contact corresponding to a TBB candidate.
 * Contains the relevant data returned from Salesforce - most importantly
 * the Salesforce id, from which the Salesforce url (the sflink) can be
 * computed.
 * <p/>
 * See notes on {@link Opportunity} for the reason for the public fields.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class Contact extends SalesforceObjectBase {

    @JsonSetter("AccountId")
    private String accountId;

    @JsonSetter("TBBid__c")
    private Long tbbId;

    public Contact() {
    }

    public Contact(Candidate candidate) {
        tbbId = Long.valueOf(candidate.getCandidateNumber());
    }

    @Override
    String getSfObjectName() {
        return "Contact";
    }

}
