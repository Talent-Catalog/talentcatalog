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

package org.tctalent.server.casi.core.services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;

/**
 * Service interface for managing candidate assistance services from various providers.
 *
 * @author sadatmalik
 */
public interface CandidateAssistanceService {

  /**
   * Unique key for this provider and service, e.g., "DUOLINGO::PROCTORED_TEST"
   * @return the unique provider-service key
   */
  String providerKey(); // e.g., "DUOLINGO::PROCTORED_TEST"

  // CREATE

  /**
   * Import inventory from a file. The file format is provider-specific.
   * @param file the file to import
   * @throws ImportFailedException if the import fails
   */
  void importInventory(MultipartFile file) throws ImportFailedException;

  /**
   * Assign a service to a candidate by candidate ID.
   * @param candidateId the candidate ID
   * @param actor the user performing the assignment
   * @return the service assignment
   */
  ServiceAssignment assignToCandidate(Long candidateId, User actor);

  /**
   * Assign a service to all candidates in a saved list.
   * @param listId the saved list ID
   * @param actor the user performing the assignment
   * @return the list of service assignments
   */
  List<ServiceAssignment> assignToList(Long listId, User actor);

  /**
   * Reassign a service for a candidate, e.g., if the original resource was invalid or expired.
   * @param candidateNumber the candidate number
   * @param actor the user performing the reassignment
   * @return the new service assignment
   */
  ServiceAssignment reassignForCandidate(String candidateNumber, User actor);

  // READ

  /**
   * Get all assignments for a candidate and service code.
   * @param candidateId the candidate ID
   * @return the list of service assignments
   */
  List<ServiceAssignment> getAssignmentsForCandidate(Long candidateId);

  /**
   * Get all available (unassigned) resources for this provider and service.
   * @return the list of available service resources
   */
  List<ServiceResource> getAvailableResources();

  /**
   * Get all resources assigned to a candidate for a specific service.
   * @param candidateId the candidate ID
   * @return the list of service resources assigned to the candidate
   */
  List<ServiceResource> getResourcesForCandidate(Long candidateId);

  /**
   * Get a specific resource by its unique resource code.
   * @param resourceCode the unique resource code
   * @return the service resource
   */
  ServiceResource getResourceForResourceCode(String resourceCode);

  /**
   * Get the candidate associated with a specific resource code.
   * @param resourceCode the unique resource code
   * @return the candidate assigned to the resource
   */
  Candidate getCandidateForResourceCode(String resourceCode);

  // COUNT

  /**
   * Count of all resources for this provider.
   * @return the total count of resources
   */
  long countAvailableForProvider();

  /**
   * Count of available (unassigned) resources for this provider and service.
   * @return the count of available resources
   */
  long countAvailableForProviderAndService();

  // UPDATE

  /**
   * Update the status of a specific resource.
   * @param resourceCode the unique resource code
   * @param status the new status to set
   * @throws NoSuchObjectException if the resource with the given code is not found
   */
  void updateResourceStatus(String resourceCode, ResourceStatus status) throws NoSuchObjectException;
}
