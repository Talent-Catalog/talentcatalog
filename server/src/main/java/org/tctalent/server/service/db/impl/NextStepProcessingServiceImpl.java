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

import static org.tctalent.server.util.NextStepHelper.auditStampNextStep;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.AbstractOpportunity;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.UserService;

@RequiredArgsConstructor
@Service
public class NextStepProcessingServiceImpl implements NextStepProcessingService {
    private final UserService userService;

    public String processNextStep(@Nullable AbstractOpportunity opp, @Nullable String nextStep) {
        // Some updates may be automated, so we attribute these to SystemAdmin
        User userForAttribution = userService.getLoggedInUser();
        if (userForAttribution == null) {
            userForAttribution = userService.getSystemAdminUser();
        }

        String currentNextStep =
            opp == null ? null : opp.getNextStep();

        final String processedNextStep = auditStampNextStep(
            userForAttribution.getUsername(),
            LocalDate.now(),
            currentNextStep,
            nextStep
        );

        return processedNextStep;
    }

}
