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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a Salesforce Opportunity.
 * <p/>
 * This is created from incoming JSON in the body of the response to a HTTP GET request for
 * opportunity details.
 * The problem with Salesforce fields is that they all start with upper case - so "Name" rather
 * than "name". This doesn't map well to Java bean objects where field values by convention start
 * with lower case.
 * <p/>
 * If you just code this as a standard Java Bean with private fields accessed by standard
 * getter and setters, the Salesforce JSON won't map to corresponding fields in the Java object
 * because "Name" does not map to "name".
 * <p/>
 * Therefore, the Java properties are each annotated with the @JsonSetter annotation which provides
 * a convenient mapping from the SalesForce field names to standard Java field names.
 * <p/>
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

    @JsonSetter("Name")
    private String name;

    @JsonSetter("AccountId")
    private String accountId;

    @JsonSetter("Candidate_TC_id__c")
    private String candidateId;

    @JsonSetter("Closing_Comments__c")
    private String closingComments;

    @JsonSetter("Closing_Comments_For_Candidate__c")
    private String closingCommentsForCandidate;

    @JsonSetter("Employer_Feedback__c")
    private String employerFeedback;

    @JsonSetter("IsClosed")
    private boolean closed;

    @JsonSetter("IsWon")
    private boolean won;

    @JsonSetter("CreatedDate")
    private String createdDate;

    @JsonSetter("LastModifiedDate")
    private String lastModifiedDate;

    @JsonSetter("NextStep")
    private String nextStep;

    @JsonSetter("Next_Step_Due_Date__c")
    private String nextStepDueDate;

    @JsonSetter("OwnerId")
    private String ownerId;

    @JsonSetter("Opportunity_Score__c")
    private String opportunityScore;

    @JsonSetter("Parent_Opportunity__c")
    private String parentOpportunityId;

    @JsonSetter("RecordTypeId")
    private String recordTypeId;

    @JsonSetter("StageName")
    private String stageName;

    @JsonSetter("TBBCandidateExternalId__c")
    private String candidateExternalId;

    @JsonSetter("Hiring_Commitment__c")
    private Long hiringCommitment;

    @Override
    String getSfObjectName() {
        return "Opportunity";
    }

}
