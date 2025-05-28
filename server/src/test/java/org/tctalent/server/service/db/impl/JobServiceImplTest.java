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
import org.tctalent.server.data.JobTestData;
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
    private UpdateJobRequest emptyUpdateJobRequest;

    // TODO: import real user from test data once UserService tests merged
    @Mock private User user;
    @Mock private UserService userService;
    @Mock private SalesforceJobOppRepository salesforceJobOppRepository;
    @Mock private SalesforceService sfService;
    @Mock private SavedSearchService savedSearchService;
    @Mock private SavedListService savedListService;
    @Mock private NextStepProcessingService nextStepProcessingService;
    @Mock private OppNotificationService oppNotificationService;

    @InjectMocks
    private JobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        job = JobTestData.getJob();
        emptyUpdateJobRequest = new UpdateJobRequest();
    }

    @Test
    @DisplayName("updateJob sets new name on SF AND TC when provided in request")
    void updateJobSetsNewNameWhenChanged() {
        final String newName = "new name";
        final String oldName = job.getName();
        emptyUpdateJobRequest.setJobName(newName);

        given(userService.getLoggedInUser()).willReturn(user);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(job));

        jobService.updateJob(99L, emptyUpdateJobRequest);

        then(sfService).should().updateEmployerOpportunityName(job.getSfId(), newName);
        Assertions.assertEquals(job.getName(), newName);
        then(savedSearchService).should().updateSuggestedSearchesNames(job, oldName);
        then(savedListService).should().updateAssociatedListsNames(job);
    }

    @Test
    @DisplayName("updateJob sets Stage on SF AND TC when provided in request")
    void updateJobSetsStageOnSfAndTcWhenProvided() {
        final JobOpportunityStage newStage = JobOpportunityStage.jobOffer;
        emptyUpdateJobRequest.setStage(newStage);

        given(userService.getLoggedInUser()).willReturn(user);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(job));

        jobService.updateJob(99L, emptyUpdateJobRequest);

        then(sfService).should()
            .updateEmployerOpportunityStage(job, newStage, null, null);
        Assertions.assertEquals(job.getStage(), newStage);
    }

    @Test
    @DisplayName("updateJob sets Next Step on SF AND TC when provided in request")
    void updateJobSetsNextStepOnSfAndTcWhenProvided() {
        final String newNextStep = "do something";
        emptyUpdateJobRequest.setNextStep(newNextStep);

        given(userService.getLoggedInUser()).willReturn(user);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(job));
        given(nextStepProcessingService.processNextStep(job, newNextStep)).willReturn(newNextStep);

        jobService.updateJob(99L, emptyUpdateJobRequest);

        then(sfService).should()
            .updateEmployerOpportunityStage(job, null, newNextStep, null);
        Assertions.assertEquals(job.getNextStep(), newNextStep);
    }

    @Test
    @DisplayName("updateJob sets Next Step Due Date on SF AND TC when provided in request")
    void updateJobSetsNextStepDueDateOnSfAndTcWhenProvided() {
        final LocalDate newNextStepDueDate = LocalDate.of(1970, 1,1);
        emptyUpdateJobRequest.setNextStepDueDate(newNextStepDueDate);

        given(userService.getLoggedInUser()).willReturn(user);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(job));

        jobService.updateJob(99L, emptyUpdateJobRequest);

        then(sfService).should()
            .updateEmployerOpportunityStage(job, null, null, newNextStepDueDate);
        Assertions.assertEquals(job.getNextStepDueDate(), newNextStepDueDate);
    }

    @Test
    @DisplayName("updateJob - when SF-pertinent request values all null, no SF update "
        + "and corresponding TC Job values unchanged")
    void updateJobNoUnnecessaryUpdate() {
        emptyUpdateJobRequest.setEvergreen(true); // Minor update (not pertinent to SF)
        final String currentNextStep = job.getNextStep();
        final LocalDate currentDueDate = job.getNextStepDueDate();
        final String currentName = job.getName();
        final JobOpportunityStage currentStage = job.getStage();

        given(userService.getLoggedInUser()).willReturn(user);
        given(salesforceJobOppRepository.findById(anyLong())).willReturn(Optional.of(job));

        jobService.updateJob(99L, emptyUpdateJobRequest);

        then(sfService).shouldHaveNoInteractions();
        Assertions.assertEquals(job.getNextStep(), currentNextStep);
        Assertions.assertEquals(job.getNextStepDueDate(), currentDueDate);
        Assertions.assertEquals(job.getName(), currentName);
        Assertions.assertEquals(job.getStage(), currentStage);
    }

}
