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

package org.tctalent.server.candidateservices.application;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.candidateservices.api.dto.ServiceAssignment;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.model.db.User;

/**
 * Management interface for candidate services (e.g., coupon / resource / licence providers).
 *
 * @author sadatmalik
 */
public interface CandidateService {
  /**
   * Assign a service resource (e.g., coupon / resource / licence) to a single candidate.
   * Implementations must be idempotent for the candidate+provider resource.
   */
  ServiceAssignment assignToCandidate(Long candidateId, User actor);

  /**
   * Assign resources to every candidate in a list (skips already assigned).
   */
  List<ServiceAssignment> assignToList(Long listId, User actor);

  /**
   * Optional: provider inventory import (CSV, API pull, etc.).
   */
  void importInventory(MultipartFile file) throws ImportFailedException;

  /**
   * Provider capacity/introspection.
   */
  int countAvailable();
}
