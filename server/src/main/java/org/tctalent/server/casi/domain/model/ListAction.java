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

package org.tctalent.server.casi.domain.model;

/**
 * Enumeration of actions that can be performed on a candidate in a service list.
 * <p>
 * The set of actions available for a given list is declared by each
 * {@link org.tctalent.server.casi.core.services.CandidateAssistanceService} implementation
 * via its {@code serviceListSpecs()} method, and stored on the
 * {@link org.tctalent.server.casi.domain.persistence.ServiceListEntity}.
 */
public enum ListAction {

  /**
   * Sets the existing {@link ServiceAssignment} {@link AssignmentStatus} to REASSIGNED and its
   * associated {@link ServiceResource} {@link ResourceStatus} to DISABLED.
   */
  REASSIGN
}
