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

package org.tctalent.server.api.portal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.casi.api.request.IssueReportRequest;
import org.tctalent.server.casi.api.request.UpdateServiceResourceStatusRequest;
import org.tctalent.server.casi.application.providers.linkedin.LinkedInService;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.UserService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portal/linkedin")
public class LinkedinPortalApi {

  private final LinkedInService linkedinService;
  private final UserService userService;

  /**
   * Checks if a candidate is eligible for the LinkedIn Premium membership upgrade offer.
   *
   * @param candidateId
   * @return true if the candidate is eligible
   */
  @GetMapping("{candidateId}/eligibility")
  public Boolean isEligible(@PathVariable Long candidateId) {
    return linkedinService.isEligible(candidateId);
  }

  /**
   * Returns the candidate's assignment to an RESERVED resource, or a REDEEMED resource
   * if no RESERVED one exists, or null if neither is found.
   *
   * @param candidateId
   * @return {@link ServiceAssignment} or null if none found
   */
  @GetMapping("{candidateId}/find-assignment")
  public ServiceAssignment findAssignmentWithReservedOrRedeemedResource(
      @PathVariable Long candidateId) {
    return linkedinService.findAssignmentWithReservedOrRedeemedResource(candidateId);
  }

  /**
   * Assigns a single coupon for the LinkedIn Premium membership upgrade offer. Uses System Admin
   * as assigning user - pre-set by list-tagging with #LinkedInEligible1/2/etc.
   * <p>
   * Catches unexpected exceptions, logged and also recorded by adding the candidate to the
   * #LinkedInAssignmentFailure List for admin action.
   *
   * @param candidateId
   * @return {@link ServiceAssignment} object showing new assignment status or null if failed
   */
  @PostMapping("{candidateId}/assign")
  public ServiceAssignment assign(@PathVariable Long candidateId) {
    try {
      User user = userService.getSystemAdminUser();
      return linkedinService.assignToCandidate(candidateId, user);

    } catch (Exception e) {
      linkedinService.addCandidateToAssignmentFailureList(candidateId, e);

      LogBuilder.builder(log)
          .action("AssignLinkedInCoupon")
          .message("Candidate " + candidateId + ": Unexpected assignment failure")
          .logError(e);

      return null;
    }
  }

  /**
   * Updates status of a single coupon for the LinkedIn Premium membership upgrade offer.
   *
   * @param request - {@link UpdateServiceResourceStatusRequest}
   */
  @PutMapping("update-coupon-status")
  public void updateCouponStatus(@RequestBody UpdateServiceResourceStatusRequest request) {
    linkedinService.updateResourceStatus(request.getResourceCode(), request.getStatus());
  }

  /**
   * Adds the candidate associated with the given {@link ServiceAssignment} to the
   * #LinkedInIssueReport List, along with a note containing the coupon code, assignment status,
   * assignment date and the candidate's description of the issue.
   *
   * @param request contains the {@link ServiceAssignment} and the candidate's issue comment
   */
  @PostMapping("issue-report")
  public void addCandidateToIssueReportList(@RequestBody IssueReportRequest request) {
    linkedinService.addCandidateToIssueReportList(request.getAssignment(), request.getIssueComment());
  }

  /**
   * Checks if the candidate is on the #LinkedInIssueReport List.
   *
   * @param candidateId
   * @return true if the candidate is on the List
   */
  @GetMapping("{candidateId}/issue-report")
  public Boolean isOnIssueReportList(@PathVariable Long candidateId) {
    return linkedinService.isOnIssueReportList(candidateId);
  }

  /**
   * Checks if the candidate is on the #LinkedInAssignmentFailure List.
   *
   * @param candidateId
   * @return true if the candidate is on the List
   */
  @GetMapping("{candidateId}/assignment-failure")
  public Boolean isOnAssignmentFailureList(@PathVariable Long candidateId) {
    return linkedinService.isOnAssignmentFailureList(candidateId);
  }

}
