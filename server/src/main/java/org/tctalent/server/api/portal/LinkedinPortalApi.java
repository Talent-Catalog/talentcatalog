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

package org.tctalent.server.api.portal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.casi.api.request.UpdateServiceResourceStatusRequest;
import org.tctalent.server.casi.application.providers.linkedin.LinkedInService;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portal/linkedin")
public class LinkedinPortalApi {

  private final LinkedInService linkedinService;
  private final UserService userService;

  /**
   * Checks if a candidate is eligible for the LinkedIn Premium membership upgrade offer.
   * @param candidateId - ID of candidate
   * @return true if the candidate is eligible
   */
  @GetMapping("{candidateId}/eligibility")
  public Boolean isEligible(@PathVariable Long candidateId) {
    return linkedinService.isEligible(candidateId);
  }

  /**
   * Returns the candidate's redeemed or assigned LinkedIn Premium membership coupon, if any.
   * @param candidateId - ID of candidate
   * @return {@link ServiceAssignment} or null if none found
   */
  @GetMapping("{candidateId}/assignment-check")
  public ServiceAssignment findRedeemedOrAssignedCoupon (@PathVariable Long candidateId) {
    return linkedinService.findRedeemedOrAssignedCoupon(candidateId);
  }

  /**
   * Assigns a single coupon for the LinkedIn Premium membership upgrade offer. Uses System Admin
   * as assigning user, since it's pre-set by list tagging.
   * @param candidateId - ID of assignee candidate
   * @return {@link ServiceAssignment} object showing new assignment status
   */
  @PostMapping("{candidateId}/assign")
  public ServiceAssignment assign(@PathVariable Long candidateId) {
    User user = userService.getSystemAdminUser();

    return linkedinService.assignToCandidate(candidateId, user);
  }

  /**
   * Updates status of a single coupon for the LinkedIn Premium membership upgrade offer.
   * @param request - {@link UpdateServiceResourceStatusRequest}
   */
  @PutMapping("update-coupon-status")
  public void updateCouponStatus(@RequestBody UpdateServiceResourceStatusRequest request) {
    linkedinService.updateResourceStatus(request.getResourceCode(), request.getStatus());
  }

}
