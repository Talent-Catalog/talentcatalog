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

import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.CountryTestData.CANADA;
import static org.tctalent.server.data.CountryTestData.UNITED_KINGDOM;
import static org.tctalent.server.data.SavedListTestData.getSavedList;
import static org.tctalent.server.data.SavedSearchTestData.getSavedSearch;
import static org.tctalent.server.data.UserTestData.getAdminUser;
import static org.tctalent.server.data.UserTestData.getAuditUser;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.JobOppIntake;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.job.UpdateJobRequest;

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
        job.setCreatedBy(getAuditUser());
        job.setContactUser(getAuditUser());
        job.setAccountId("123456");
        job.setCountry(UNITED_KINGDOM);
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
        job.setCountry(CANADA);
        job.setDescription("This is a description.");
        job.setEmployerEntity(getEmployer());
        job.setExclusionList(getSavedList());
        job.setJobSummary("This is a job summary.");
        job.setOwnerId("321");
        job.setPublishedBy(getAuditUser());
        job.setPublishedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        job.setJobCreator(PartnerImplTestData.getDestinationPartner());
        job.setStarringUsers(Set.of(getAuditUser()));
        job.setSubmissionList(getSavedList());
        job.setSuggestedList(getSavedList());
        job.setSuggestedSearches(Set.of(getSavedSearch()));
        job.setJobOppIntake(getJobOppIntake());
        job.setHiringCommitment(1L);
        job.setOpportunityScore("Opp Score");
        job.setClosed(false);
        job.setWon(false);
        job.setName("Opp Name");
        job.setStageOrder(1);
        job.setCreatedBy(getAuditUser());
        job.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        job.setUpdatedBy(getAuditUser());
        job.setUpdatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        job.setCandidateOpportunities(Set.of(getChildCandidateOpp(job)));
        return job;
    }

    public static Employer getEmployer() {
        Employer employer = new Employer();
        employer.setName("ABC Accounts");
        employer.setWebsite("www.ABCAccounts.com");
        employer.setCountry(CountryTestData.AUSTRALIA);
        employer.setDescription("This is an employer description.");
        employer.setHasHiredInternationally(true);
        return employer;
    }

    public static JobOppIntake getJobOppIntake() {
        JobOppIntake joi = new JobOppIntake();
        joi.setId(99L);
        joi.setSalaryRange("80-90k");
        joi.setRecruitmentProcess("The recruitment process.");
        joi.setEmployerCostCommitment("Employer cost commitments.");
        joi.setLocation("Melbourne");
        joi.setLocationDetails("Western suburbs");
        joi.setBenefits("These are the benefits.");
        joi.setLanguageRequirements("These are the language reqs.");
        joi.setEducationRequirements("These are the education reqs.");
        joi.setSkillRequirements("These are the skill reqs.");
        joi.setEmploymentExperience("This is the employment experience.");
        joi.setOccupationCode("Occupation code");
        joi.setMinSalary("80k");
        joi.setVisaPathways("The visa pathways");
        return joi;
    }

    private static CandidateOpportunity getChildCandidateOpp(SalesforceJobOpp job) {
        CandidateOpportunity co = new CandidateOpportunity();
        co.setId(99L);
        co.setJobOpp(job);
        co.setStage(CandidateOpportunityStage.cvReview);
        co.setNextStep("Review CVs");
        co.setNextStepDueDate(LocalDate.parse("2020-01-01"));
        co.setCandidate(getCandidate());
        return co;
    }

    /**
     * Holds an {@link UpdateJobRequest} along with the expected {@link SalesforceJobOpp} that
     * should result from applying the request.
     */
    public record UpdateJobTestData(UpdateJobRequest request, SalesforceJobOpp expectedJob) {}

    /**
     * Constructs a {@link UpdateJobTestData record containing an {@link UpdateJobRequest}
     * and the expected {@link SalesforceJobOpp} that should result from using it.
     */
    public static UpdateJobTestData createUpdateJobRequestAndExpectedJob() {
        final String nextStep = "next step";
        final LocalDate nextStepDueDate = LocalDate.parse("2026-01-01");
        final long contactUserId = 1L;
        final String jobName = "The name of the job";
        final LocalDate submissionDueDate = LocalDate.parse("2026-03-01");

        UpdateJobRequest request = new UpdateJobRequest();
        request.setNextStep(nextStep);
        request.setNextStepDueDate(nextStepDueDate);
        request.setEvergreen(true);
        request.setSkipCandidateSearch(true);
        request.setContactUserId(contactUserId);
        request.setJobName(jobName);
        request.setStage(JobOpportunityStage.identifyingRoles);
        request.setSubmissionDueDate(submissionDueDate);


        SalesforceJobOpp expectedJob = getSalesforceJobOppMinimal();
        expectedJob.setNextStep(nextStep);
        expectedJob.setNextStepDueDate(nextStepDueDate);
        expectedJob.setEvergreen(true);
        expectedJob.setSkipCandidateSearch(true);
        expectedJob.setContactUser(getAdminUser());
        expectedJob.setName(jobName);
        expectedJob.setStage(JobOpportunityStage.identifyingRoles);
        expectedJob.setSubmissionDueDate(submissionDueDate);

        return new UpdateJobTestData(request, expectedJob);
    }

}
