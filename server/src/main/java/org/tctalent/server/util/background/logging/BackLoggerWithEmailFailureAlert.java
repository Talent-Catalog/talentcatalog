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

package org.tctalent.server.util.background.logging;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.service.db.email.EmailHelper;

@RequiredArgsConstructor
@Slf4j
public class BackLoggerWithEmailFailureAlert implements BackLogger {
  private final EmailHelper emailHelper;
  @NotNull private final String jobName;

  @Override
  public void logFailure(Exception e) {
    String message =
        "Background batch processing op '" + jobName + "' cancelled due to unchecked exception";

    LogBuilder.builder(log)
        .message(message + ": " + e.getMessage())
        .logError(e);

      emailHelper.sendAlert(message, e);
  }

}
