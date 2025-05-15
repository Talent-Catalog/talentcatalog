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

package org.tctalent.server.util.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.service.db.email.EmailHelper;

/**
 * A {@link StepListener} implementation that handles failures in background batch processing jobs.
 * <p>
 * When a job step fails due to an unchecked exception, this listener logs the failure
 * and sends an email alert using {@link EmailHelper}.
 * </p>
 * <p>
 * This listener is designed to be used with the background batch processing infrastructure.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BackgroundBatchProcessingListener implements StepListener {
    private final EmailHelper emailHelper;

    /**
     * Logs and sends an email alert re the cancellation of a background batch processing job due to
     * an unchecked exception.
     *
     * @param jobName Identifies the job that failed.
     * @param e The exception that caused the failure.
     */
    @Override
    public void onStepFailure(String jobName, Exception e) {
        String message =
            "Background batch processing op '" + jobName + "' cancelled due to unchecked exception";

        LogBuilder.builder(log)
            .message(message + ": " + e.getMessage())
            .logError(e);

        emailHelper.sendAlert(message, e);
    }

}
