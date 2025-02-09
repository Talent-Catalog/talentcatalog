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

package org.tctalent.server.service.db.impl;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.NextStepHelper;

/**
 * @author samschlicht
 */
@ExtendWith(MockitoExtension.class)
class NextStepProcessingServiceImplTest {

    private static final String REQUESTED_NEXT_STEP = "New Step";
    private static final String CURRENT_NEXT_STEP = "Current Step";
    private static final String USERNAME = "LoggedInUser";

    private static final SalesforceJobOpp jobOpp = new SalesforceJobOpp();
    private static final CandidateOpportunity candidateOpp = new CandidateOpportunity();
    private static final User user = new User();

    @Mock UserService userService;

    @InjectMocks
    NextStepProcessingServiceImpl nextStepProcessingService;

    @BeforeEach
    void setUp() {
        user.setUsername(USERNAME);
        candidateOpp.setNextStep(CURRENT_NEXT_STEP);
        jobOpp.setNextStep(CURRENT_NEXT_STEP);
    }

    @Test
    @DisplayName("should use SystemAdmin when no logged in user")
    void processNextStep_noLoggedInUser_usesSystemAdmin() {
        User adminUser = new User();
        adminUser.setUsername("SystemAdmin");

        when(userService.getLoggedInUser()).thenReturn(null);
        when(userService.getSystemAdminUser()).thenReturn(adminUser);

        try (var mockedHelper = mockStatic(NextStepHelper.class)) {
            nextStepProcessingService.processNextStep(jobOpp, REQUESTED_NEXT_STEP);

            mockedHelper.verify(() -> NextStepHelper.auditStampNextStep(
                eq("SystemAdmin"),
                eq(LocalDate.now()),
                eq(CURRENT_NEXT_STEP),
                eq(REQUESTED_NEXT_STEP)
            ));
        }
    }

    @Test
    @DisplayName("uses logged-in user when available")
    void processNextStep_userLoggedIn_usesLoggedInUser() {
        when(userService.getLoggedInUser()).thenReturn(user);

        try (var mockedHelper = mockStatic(NextStepHelper.class)) {
            nextStepProcessingService.processNextStep(jobOpp, REQUESTED_NEXT_STEP);

            mockedHelper.verify(() -> NextStepHelper.auditStampNextStep(
                eq("LoggedInUser"),
                eq(LocalDate.now()),
                eq(CURRENT_NEXT_STEP),
                eq(REQUESTED_NEXT_STEP)
            ));
        }
    }

    @Test
    @DisplayName("also handles candidate opps")
    void processNextStep_candidateOppUsed_handlesCandidateOpp() {
        when(userService.getLoggedInUser()).thenReturn(user);

        try (var mockedHelper = mockStatic(NextStepHelper.class)) {
            nextStepProcessingService.processNextStep(candidateOpp, REQUESTED_NEXT_STEP);

            mockedHelper.verify(() -> NextStepHelper.auditStampNextStep(
                eq("LoggedInUser"),
                eq(LocalDate.now()),
                eq(CURRENT_NEXT_STEP),
                eq(REQUESTED_NEXT_STEP)
            ));
        }
    }

    @Test
    @DisplayName("opp has a null next step")
    void processNextStep_oppHasNullNextStep_handlesNullNextStep() {
        candidateOpp.setNextStep(null);

        when(userService.getLoggedInUser()).thenReturn(user);

        try (var mockedHelper = mockStatic(NextStepHelper.class)) {
            nextStepProcessingService.processNextStep(candidateOpp, REQUESTED_NEXT_STEP);

            mockedHelper.verify(() -> NextStepHelper.auditStampNextStep(
                eq("LoggedInUser"),
                eq(LocalDate.now()),
                eq(null),
                eq(REQUESTED_NEXT_STEP)
            ));
        }
    }

}
