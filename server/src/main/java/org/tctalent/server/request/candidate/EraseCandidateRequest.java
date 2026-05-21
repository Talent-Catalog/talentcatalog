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

package org.tctalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * Request to fully erase a candidate's personally identifiable data.
 *
 * <p>This request does not support multiple erasure modes. The backend performs one full erasure
 * action only: the candidate is marked as deleted and personal data is removed or scrubbed.</p>
 *
 * <p>The candidate row itself is intentionally not physically deleted because it may be referenced
 * by saved lists, opportunities, task assignments, attachments, audit records, or other database
 * structures. The row is kept only as a deleted placeholder.</p>
 */
@Getter
@Setter
public class EraseCandidateRequest {

  /**
   * Candidate number typed by the admin as a safety confirmation.
   *
   * <p>If provided, it must match the candidate's current candidate number. This helps prevent
   * accidental erasure of the wrong candidate.</p>
   */
  @Nullable
  private String confirmationCandidateNumber;
}