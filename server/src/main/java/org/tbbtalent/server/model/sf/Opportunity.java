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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a Salesforce Opportunity.
 * <p/>
 * This is created from incoming JSON in the body of the response to a HTTP GET request for
 * opportunity details.
 * The problem with Salesforce fields is that they all start with upper case - so "Name" rather
 * that "name". This doesn't map well to Java bean objects where field values by convention start
 * with lower case.
 * <p/>
 * If you just code this as a standard Java Bean with private fields accessed by standard
 * getter and setters, the Salesforce JSON won't map to corresponding fields in the Java object
 * because "Name" does not map to "name".
 * <p/>
 * The (crappy) way around it is to make all the fields public and capitalized. Then the JSON
 * will map to the fields.
 *
 * NOTE: You can request fields using their names starting with lower case, but they are always
 * returned in the response with their standard upper case names.
 *
 * NOTE: The getters (added here automatically by Lombok) mean that utilities that process this
 * object as a normal bean, will see a normal (lower case) "name" attribute on the bean because
 * that is what they expect to be returned by a getName method.
 * The DtoBuilder is one such utility. In fact it won't find an attribute "Name"!
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString(callSuper = true)
public class Opportunity extends SalesforceObjectBase {
    public String Name;
    public String AccountId;
    public String AccountCountry__c;
    public String AccountName__c;
    public String Candidate_TC_id__c;
    public boolean IsClosed;
    public String OwnerId;
    public String RecordTypeId;
    public String StageName;
    public String TBBCandidateExternalId__c;
    public Long Hiring_Commitment__c;
    public String AccountWebsite__c;
    public boolean AccountHasHiredInternationally__c;

    @Override
    String getSfObjectName() {
        return "Opportunity";
    }

}
