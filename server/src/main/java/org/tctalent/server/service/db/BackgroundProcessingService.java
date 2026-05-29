/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db;

import java.util.List;
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.PageContext;

/**
 * Service for creating background processors
 */
public interface BackgroundProcessingService {

  /**
   * Creates a back processor to handle processing of potential duplicate candidates.
   */
  BackProcessor<PageContext> createPotentialDuplicatesBackProcessor(List<Long> candidateIds);

  /**
   * Daily check for candidates who may have more than one profile based on identical first name AND
   * last name AND DOB - sets potentialDuplicate to true. Can also be triggered manually from
   * SystemAdminApi stub.
   * <p>
   *   Calls {@link CandidateService#cleanUpResolvedDuplicates()} which sets same property to
   *   false if previously flagged candidates no longer meet the criteria.
   * </p>
   */
  void processPotentialDuplicateCandidates();

  /**
   * Creates a background processor and obtains candidate list and sets parameters for its
   * scheduled operations.
   */
  void initiateDuplicateProcessing();

  /**
   * Registers all users with a local Keycloak using the provided password.
   * <p>
   *     This is only intended for user on a developer local system connecting to a
   *     Keycloak instance. It will fail in a staging or production environment because no
   *     localhost Keycloak instance will be available.
   * </p>
   * <p>
   *     Normally, this will only need to be run once on each developer's machine to register
   *     users in their local database with their local Keycloak.
   * </p>
   * @param password All users will be registered with this password.
   */
  void registerUsersWithKeycloak(String password);

  /**
   * Adds publicID to any candidate that doesn't have one
   */
  void setCandidatePublicIds();

  /**
   * Adds publicID to any partner that doesn't have one
   */
  void setPartnerPublicIds();

  /**
   * Adds publicID to any saved list that doesn't have one
   */
  void setSavedListPublicIds();

  /**
   * Adds publicID to any saved search that doesn't have one
   */
  void setSavedSearchPublicIds();
}
