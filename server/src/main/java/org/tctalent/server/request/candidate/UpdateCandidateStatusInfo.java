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

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tctalent.server.model.db.CandidateNote;
import org.tctalent.server.model.db.CandidateStatus;

/**
 * Changing a candidate status includes more than just the new status.
 * In addition, a comment can be supplied which goes into a candidate note and also a
 * candidate message can be supplied which is emailed to the candidate.
 * <p/>
 * This class encapsulates all the above.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class UpdateCandidateStatusInfo {

  /**
   * New candidate status
   */
  @NotNull
  private CandidateStatus status;

  /**
   * Optional comment to be included in {@link CandidateNote}
   */
  private String comment;

  /**
   * Optional message to be sent to the associated candidate
   */
  private String candidateMessage;
}
