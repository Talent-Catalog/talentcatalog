/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


class OpportunityTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("convert json string to opportunity pojo succeeds")
    void jsonStringToOpportunityPojo() throws JsonProcessingException {
        String opportunityJson = """
                {
                  "Name": "Jalil",
                  "AccountId": "12345",
                  "AccountCountry__c": "UK",
                  "AccountName__c": "Rolls Royce",
                  "Candidate_TC_id__c": "67890",
                  "Closing_Comments__c": "Some closing comments",
                  "Employer_Feedback__c": "Employer feedback text",
                  "IsClosed": "false",
                  "NextStep": "Next step",
                  "Next_Step_Due_Date__c": "01-06-2023",
                  "OwnerId": "999",
                  "Parent_Opportunity__c": "23456",
                  "RecordTypeId": "Y",
                  "StageName": "Offer",
                  "TBBCandidateExternalId__c": "67890_ext_id",
                  "Hiring_Commitment__c": "25",
                  "AccountWebsite__c": "https://www.rolls-roycemotorcars.com",
                  "AccountHasHiredInternationally__c": "Yes",
                  "Id": "13579"
                }""";

        Opportunity opportunity = objectMapper.readValue(opportunityJson, Opportunity.class);

        assertThat(opportunity.getName()).isEqualTo("Jalil");
        assertThat(opportunity.getAccountId()).isEqualTo("12345");
        assertThat(opportunity.getAccountCountry()).isEqualTo("UK");
        assertThat(opportunity.getAccountName()).isEqualTo("Rolls Royce");
        assertThat(opportunity.getCandidateId()).isEqualTo("67890");
        assertThat(opportunity.getClosingComments()).isEqualTo("Some closing comments");
        assertThat(opportunity.getEmployerFeedback()).isEqualTo("Employer feedback text");
        assertFalse(opportunity.isClosed());
        assertThat(opportunity.getNextStep()).isEqualTo("Next step");
        assertThat(opportunity.getNextStepDueDate()).isEqualTo("01-06-2023");
        assertThat(opportunity.getOwnerId()).isEqualTo("999");
        assertThat(opportunity.getParentOpportunityId()).isEqualTo("23456");
        assertThat(opportunity.getRecordTypeId()).isEqualTo("Y");
        assertThat(opportunity.getStageName()).isEqualTo("Offer");
        assertThat(opportunity.getCandidateExternalId()).isEqualTo("67890_ext_id");
        assertThat(opportunity.getHiringCommitment()).isEqualTo(25L);
        assertThat(opportunity.getAccountWebsite()).isEqualTo("https://www.rolls-roycemotorcars.com");
        assertThat(opportunity.getAccountHasHiredInternationally()).isEqualTo("Yes");
        assertThat(opportunity.getId()).isEqualTo("13579");
    }

}
