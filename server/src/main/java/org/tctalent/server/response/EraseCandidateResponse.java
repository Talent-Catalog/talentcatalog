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

package org.tctalent.server.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tctalent.server.model.db.Candidate;

/**
 * Response returned after a candidate's personal data has been fully erased.
 *
 * <p>This response intentionally returns only safe, minimal data. It does not return personal
 * candidate information such as name, email, phone, documents, notes, or search text.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class EraseCandidateResponse {

  /**
   * Candidate database ID.
   */
  private Long id;

  /**
   * Candidate number kept for internal placeholder/reference purposes.
   */
  private String candidateNumber;

  /**
   * Candidate status after erasure. This should normally be {@code deleted}.
   */
  private String status;

  /**
   * True when the erasure request completed successfully.
   */
  private boolean erased;


  /**
   * Creates a safe response from the erased candidate.
   *
   * @param candidate erased candidate.
   * @return response containing only safe metadata.
   */
  public static EraseCandidateResponse fromCandidate(Candidate candidate) {
    EraseCandidateResponse response = new EraseCandidateResponse();

    response.setId(candidate.getId());
    response.setCandidateNumber(candidate.getCandidateNumber());
    response.setStatus(candidate.getStatus() == null ? null : candidate.getStatus().name());
    response.setErased(true);

    return response;
  }
}