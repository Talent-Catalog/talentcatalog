/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.casi.api.request;

import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.casi.domain.model.ServiceAssignment;

/**
 * Request body for the issue-report endpoint. Wraps the candidate's current
 * {@link ServiceAssignment} together with an optional free-text comment describing the problem.
 */
@Getter
@Setter
public class IssueReportRequest {

  /** The assignment associated with the issue being reported. */
  private ServiceAssignment assignment;

  /** Free-text description of the issue provided by the candidate. */
  private String issueComment;

}
