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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Semantic role of a service list. Defines the purpose a saved list serves within a
 * candidate assistance service project.
 * <p>
 * The {@code allowsMultiple} flag is a framework-level constraint: when {@code true}, a service
 * may declare more than one list of this role (e.g. segmented by region or cohort). When
 * {@code false}, only one list of this role may be registered per service.
 * <p>
 * Note: this enum carries no action information. The set of permitted actions for a given
 * role is declared by each {@link org.tctalent.server.casi.core.services.CandidateAssistanceService}
 * implementation and may differ between services.
 */
@Getter
@RequiredArgsConstructor
public enum ListRole {

  /**
   * Candidates who have reported a problem with their assigned service resource
   * (e.g. a coupon that does not work).
   */
  USER_ISSUE_REPORT(false),

  /**
   * Candidates for whom service resource assignment failed due to an exception,
   * and therefore have no assigned resource.
   */
  ASSIGNMENT_FAILURE(false),

  /**
   * Candidates who are eligible for the service. Multiple lists of this role are
   * permitted to allow segmentation by region, cohort, or other criteria.
   */
  SERVICE_ELIGIBILITY(true);

  private final boolean allowsMultiple;
}
