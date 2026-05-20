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

package org.tctalent.server.service.db;

import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.request.candidate.EraseCandidateRequest;

/**
 * Service for erasing candidate personal data while preserving database integrity.
 *
 * <p>This service implements the "right to erasure" behaviour for candidates. It should
 * be used when a candidate has requested that their data be removed from the Talent Catalog.</p>
 *
 * <p>The implementation intentionally keeps a placeholder candidate row instead of physically
 * deleting the candidate because candidates are referenced by saved lists, candidate opportunities,
 * task assignments, and reporting structures. The placeholder row is stripped of identifying data
 * and marked as deleted.</p>
 */
public interface CandidateErasureService {

  /**
   * Erases the personally identifiable data associated with a candidate.
   *
   * @param candidateId ID of the candidate to erase.
   * @param request erasure options and confirmation data.
   * @return the erased placeholder candidate.
   */
  Candidate eraseCandidate(long candidateId, EraseCandidateRequest request);
}