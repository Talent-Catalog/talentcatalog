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
import org.tctalent.server.service.db.OppNotificationService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    SalesforceJobOpp job;
    UpdateJobRequest updateJobRequest;

    private static final String JOB_SF_ID = "LKJH66446GGFDFSA";
    private static final String JOB_NAME = "test job";
    private static final String JOB_NEXT_STEP = "do something";
    private static final JobOpportunityStage JOB_STAGE = JobOpportunityStage.candidateSearch;

    @Mock User user;
    @Mock UserService userService;
    @Mock SalesforceJobOppRepository salesforceJobOppRepository;
    @Mock Employer employerEntity;
    @Mock SalesforceService sfService;
    @Mock SavedSearchService savedSearchService;
    @Mock SavedListService savedListService;
    @Mock OppNotificationService oppNotificationService;

    @InjectMocks
    JobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        job = new SalesforceJobOpp();
        job.setId(1L);
        job.setName(JOB_NAME);
        job.setSfId(JOB_SF_ID);
        job.setNextStep(JOB_NEXT_STEP);
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

    // TODO:
    @Test
    @DisplayName("updateJob sets Next Step on SF AND TC when provided in request")
    void updateJobSetsNextStepOnSfAndTcWhenProvided() {}

    @Test
    @DisplayName("updateJob sets Next Step Due Date on SF AND TC when provided in request")
    void updateJobSetsNextStepDueDateOnSfAndTcWhenProvided() {}

    @Test
    @DisplayName("updateJob no SF update when Stage, Next Step info or name not in request")
    void updateJobNoUnnecessarySfUpdate() {}

}
