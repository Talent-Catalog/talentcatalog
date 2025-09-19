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

package org.tctalent.server.candidateservices.core.services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.candidateservices.domain.model.ResourceStatus;
import org.tctalent.server.candidateservices.domain.model.ServiceAssignment;
import org.tctalent.server.candidateservices.domain.model.ServiceResource;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;

/**
 * Management interface for candidate services (e.g., coupon / resource / licence providers).
 *
 * @author sadatmalik
 */
public interface CandidateService {

  // CREATE
  void importInventory(MultipartFile file, String serviceCode) throws ImportFailedException;
  ServiceAssignment assignToCandidate(Long candidateId, User actor, String serviceCode);
  List<ServiceAssignment> assignToList(Long listId, String serviceCode, User actor);
  ServiceAssignment reassignForCandidate(String candidateNumber, String serviceCode, User user);

  // READ
  List<ServiceAssignment> getAssignmentsForCandidate(Long candidateId, String serviceCode); // TODO - SM - would get resources be more natural?
  List<ServiceResource> getAvailableResources();
  ServiceResource getResourceForResourceCode(String resourceCode);
  Candidate getCandidateForResourceCode(String resourceCode);

  // COUNT
  long countAvailableForProvider();
  long countAvailableForProviderAndService();

  // UPDATE
  void updateResourceStatus(String resourceCode, ResourceStatus status);
}
