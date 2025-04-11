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

package org.tctalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Changing a candidate preference on when they are notified of opportunities
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class UpdateCandidateNotificationPreferenceRequest {

  /**
   * True if candidate wants to receive all notifications.
   * If false, the candidate will only receive notifications when they are well progressed in
   * a job opportunity.
   */
  private boolean allNotifications;
}
