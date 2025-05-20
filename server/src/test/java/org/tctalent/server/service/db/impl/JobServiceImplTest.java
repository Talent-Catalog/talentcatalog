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

package org.tctalent.server.service.db.impl;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.request.job.UpdateJobRequest;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.OppNotificationService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    private SalesforceJobOpp job;
    private UpdateJobRequest updateJobRequest;

    private static final String JOB_SF_ID = "LKJH66446GGFDFSA";
    private static final String JOB_NAME = "test job";
    private static final String JOB_NEXT_STEP = "do something";
    private static final LocalDate JOB_NEXT_STEP_DUE_DATE =
        LocalDate.of(1901, 1, 1);
    private static final JobOpportunityStage JOB_STAGE = JobOpportunityStage.candidateSearch;

    @Mock private User user;
    @Mock private UserService userService;
    @Mock private SalesforceJobOppRepository salesforceJobOppRepository;
    @Mock private Employer employerEntity;
    @Mock private SalesforceService sfService;
    @Mock private SavedSearchService savedSearchService;
    @Mock private SavedListService savedListService;
    @Mock private OppNotificationService oppNotificationService;
    @Mock private NextStepProcessingService nextStepProcessingService;

    @InjectMocks
    private JobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        job = new SalesforceJobOpp();
        job.setId(1L);
        job.setName(JOB_NAME);
        job.setSfId(JOB_SF_ID);
        job.setNextStep(JOB_NEXT_STEP);
        job.setNextStepDueDate(JOB_NEXT_STEP_DUE_DATE);
        job.setEmployerEntity(employerEntity);
        job.setStage(JOB_STAGE);
        updateJobRequest = new UpdateJobRequest();
    }

    @Test
    @DisplayName("updateJob sets new name on SF AND TC when provided in request")
    void updateJobSetsNewNameWhenChanged() {
        final String newName = "new name";
        updateJobRequest.setJobName(newName);

        given(userService.getLoggedInUser()).willReturn(user);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(job));

        jobService.updateJob(99L, updateJobRequest);

        then(sfService).should().updateEmployerOpportunityName(JOB_SF_ID, newName);
        Assertions.assertEquals(job.getName(), newName);
        then(savedSearchService).should().updateSuggestedSearchesNames(job, JOB_NAME);
        then(savedListService).should().updateAssociatedListsNames(job);
    }

    @Test
    @DisplayName("updateJob sets Stage on SF AND TC when provided in request")
    void updateJobSetsStageOnSfAndTcWhenProvided() {
        final JobOpportunityStage newStage = JobOpportunityStage.jobOffer;
        updateJobRequest.setStage(newStage);

        given(userService.getLoggedInUser()).willReturn(user);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(job));

        jobService.updateJob(99L, updateJobRequest);

        then(sfService).should()
            .updateEmployerOpportunityStage(job, newStage, null, null);
        Assertions.assertEquals(job.getStage(), newStage);
    }

    @Test
    @DisplayName("updateJob sets Next Step on SF AND TC when provided in request")
    void updateJobSetsNextStepOnSfAndTcWhenProvided() {
        final String newNextStep = "do something";
        updateJobRequest.setNextStep(newNextStep);

        given(userService.getLoggedInUser()).willReturn(user);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(job));
        given(nextStepProcessingService.processNextStep(job, newNextStep)).willReturn(newNextStep);

        jobService.updateJob(99L, updateJobRequest);

        then(sfService).should()
            .updateEmployerOpportunityStage(job, null, newNextStep, null);
        Assertions.assertEquals(job.getNextStep(), newNextStep);
    }

    @Test
    @DisplayName("updateJob sets Next Step Due Date on SF AND TC when provided in request")
    void updateJobSetsNextStepDueDateOnSfAndTcWhenProvided() {
        final LocalDate newNextStepDueDate = LocalDate.of(1970, 1,1);
        updateJobRequest.setNextStepDueDate(newNextStepDueDate);

        given(userService.getLoggedInUser()).willReturn(user);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(job));

        jobService.updateJob(99L, updateJobRequest);

        then(sfService).should()
            .updateEmployerOpportunityStage(job, null, null, newNextStepDueDate);
        Assertions.assertEquals(job.getNextStepDueDate(), newNextStepDueDate);
    }

    @Test
    @DisplayName("updateJob - when SF-pertinent request values all null, no SF update "
        + "and corresponding TC Job values unchanged")
    void updateJobNoUnnecessaryUpdate() {
        updateJobRequest.setEvergreen(true); // Set something to evade potential process skip

        given(userService.getLoggedInUser()).willReturn(user);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(job));

        jobService.updateJob(99L, updateJobRequest);

        then(sfService).shouldHaveNoInteractions();
        Assertions.assertEquals(job.getNextStep(), JOB_NEXT_STEP);
        Assertions.assertEquals(job.getNextStepDueDate(), JOB_NEXT_STEP_DUE_DATE);
        Assertions.assertEquals(job.getName(), JOB_NAME);
        Assertions.assertEquals(job.getStage(), JOB_STAGE);
    }

}
