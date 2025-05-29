/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.data;

import static org.tctalent.server.api.admin.AdminApiTestUtil.getJobOppIntake;
import static org.tctalent.server.data.SavedSearchTestData.getSavedSearch;
import static org.tctalent.server.api.admin.AdminApiTestUtil.getUserMinimal;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;

public class SalesforceJobOppTestData {

    public static SalesforceJobOpp getSalesforceJobOppMinimal() {
        SalesforceJobOpp job = new SalesforceJobOpp();
        job.setId(135L);
        job.setSfId("sales-force-job-opp-id");
        job.setAccountId("JKDHUT0000JJJGGG");
        job.setName("Test Job");
        job.setNextStep("Do something");
        job.setNextStepDueDate(LocalDate.of(1901, 1, 1));
        job.setStage(JobOpportunityStage.candidateSearch);
        job.setEmployerEntity(getEmployer());
        return job;
    }

    public static SalesforceJobOpp getSalesforceJobOppExtended() {
        SalesforceJobOpp job = new SalesforceJobOpp();
        job.setContactUser(new User("contact_user",
            "contact",
            "user",
            "test.contact@tbb.org",
            Role.admin));
        job.setSfId("123456");
        job.setStage(JobOpportunityStage.cvReview);
        job.setSubmissionDueDate(LocalDate.parse("2020-01-01"));
        job.setNextStep("This is the next step.");
        job.setNextStepDueDate(LocalDate.parse("2020-01-01"));
        job.setClosingComments("These are some closing comments.");
        job.setId(99L);
        job.setAccountId("789");
        job.setCandidateOpportunities(Set.of(CandidateOpportunityTestData.getCandidateOpportunity()));
        job.setCountry(new Country("Australia", Status.active));
        job.setDescription("This is a description.");
        job.setEmployerEntity(getEmployer());
        job.setExclusionList(SavedListTestData.getSavedList());
        job.setJobSummary("This is a job summary.");
        job.setOwnerId("321");
        job.setPublishedBy(getUserMinimal());
        job.setPublishedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        job.setJobCreator(PartnerImplTestData.getDestinationPartner());
        job.setStage(JobOpportunityStage.cvReview);
        job.setStarringUsers(Set.of(getUserMinimal()));
        job.setSubmissionDueDate(LocalDate.parse("2020-01-01"));
        job.setSubmissionList(SavedListTestData.getSavedList());
        job.setSuggestedList(SavedListTestData.getSavedList());
        job.setSuggestedSearches(Set.of(getSavedSearch()));
        job.setJobOppIntake(getJobOppIntake());
        job.setHiringCommitment(1L);
        job.setOpportunityScore("Opp Score");
        job.setClosed(false);
        job.setWon(false);
        job.setClosingComments(null);
        job.setName("Opp Name");
        job.setNextStep("Next Step");
        job.setNextStepDueDate(LocalDate.parse("2020-01-01"));
        job.setStageOrder(1);
        job.setCreatedBy(getUserMinimal());
        job.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        job.setUpdatedBy(getUserMinimal());
        job.setUpdatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        return job;
    }

    public static Employer getEmployer() {
        Employer employer = new Employer();
        employer.setName("ABC Accounts");
        employer.setWebsite("www.ABCAccounts.com");
        employer.setDescription("This is an employer description.");
        employer.setHasHiredInternationally(true);
        return employer;
    }

}
