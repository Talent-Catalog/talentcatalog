/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.api.admin;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.duolingo.UpdateDuolingoCouponStatusRequest;
import org.tctalent.server.response.DuolingoCouponResponse;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.DuolingoCouponService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskService;

@RestController
@RequestMapping("/api/admin/coupon")
@Slf4j
public class DuolingoCouponAdminApi {

  private final AuthService authService;
  private final DuolingoCouponService couponService;
  private final SavedListService savedListService;
  private final TaskAssignmentService taskAssignmentService;
  private final TaskService taskService;
  private final CandidateService candidateService;

  @Autowired
  public DuolingoCouponAdminApi(DuolingoCouponService couponService,
      SavedListService savedListService,
      AuthService authService,
      TaskAssignmentService taskAssignmentService,
      TaskService taskService,
      CandidateService candidateService) {
    this.authService = authService;
    this.couponService = couponService;
    this.savedListService = savedListService;
    this.taskAssignmentService = taskAssignmentService;
    this.taskService = taskService;
    this.candidateService = candidateService;
  }

  // Endpoint to import coupons from CSV data
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String, Object> importCouponsFromCsv(@RequestParam("file") MultipartFile file) {
    try {
      // Call the service to import coupons
      couponService.importCoupons(file);

      // Log success message
      LogBuilder.builder(log)
          .action("importCouponsFromCsv")
          .message("Coupons imported successfully from CSV")
          .logInfo();

      return Map.of("status", "success", "message", "Coupons imported successfully.");
    } catch (RuntimeException e) {
      LogBuilder.builder(log)
          .action("importCouponsFromCsv")
          .message("Failed to import coupons from CSV")
          .logError();
      // Return error response
      return Map.of("status", "failure", "message", "Failed to import coupons from CSV file.");
    }
  }

  // Endpoint to assign an available coupon to a candidate
  @PostMapping("{candidateId}/assign")
  public DuolingoCouponResponse assignCouponToCandidate(
      @PathVariable("candidateId") Long candidateId)
      throws InvalidSessionException, NoSuchObjectException, EntityExistsException {

    User user = authService.getLoggedInUser()
        .orElseThrow(() -> new InvalidSessionException("Not logged in"));

    List<DuolingoCouponResponse> coupons = couponService.getCouponsForCandidate(candidateId);

    for (DuolingoCouponResponse coupon : coupons) {
      if (coupon.getDuolingoCouponStatus().equals(DuolingoCouponStatus.SENT)) {
        throw new EntityExistsException("coupon", "for this candidate");
      }
    }

    DuolingoCoupon coupon = couponService.assignCouponToCandidate(candidateId);

    TaskImpl task = taskService.getByName("duolingoTest");
    Candidate candidate = candidateService.getCandidate(candidateId);
    taskAssignmentService.assignTaskToCandidate(user, task, candidate, null,
        null);

    return new DuolingoCouponResponse(
        coupon.getId(),
        coupon.getCouponCode(),
        coupon.getExpirationDate(),
        coupon.getDateSent(),
        coupon.getCouponStatus()
    );
  }

  // Endpoint to retrieve all coupons assigned to a candidate
  @GetMapping("{candidateId}")
  public List<DuolingoCouponResponse> getCouponsForCandidate(
      @PathVariable("candidateId") Long candidateId) {
    return couponService.getCouponsForCandidate(candidateId);
  }

  // Endpoint to update the status of a specific coupon
  @PutMapping("status")
  public void updateCouponStatus(@RequestBody UpdateDuolingoCouponStatusRequest request) {
    couponService.updateCouponStatus(request.getCouponCode(), request.getStatus());
  }

  // Endpoint to list all available coupons
  @GetMapping("available")
  public List<DuolingoCoupon> getAvailableCoupons() {
    return couponService.getAvailableCoupons();
  }

  // Endpoint to get a single coupon by code
  @GetMapping("find/{couponCode}")
  public DuolingoCouponResponse getCouponByCode(@PathVariable("couponCode") String couponCode) {
    DuolingoCoupon coupon = couponService.findByCouponCode(couponCode);
    return new DuolingoCouponResponse(
        coupon.getId(),
        coupon.getCouponCode(),
        coupon.getExpirationDate(),
        coupon.getDateSent(),
        coupon.getCouponStatus()
    );
  }

  // Endpoint to assign available coupons to a saved list candidate
  @PostMapping("assign-to-list")
  public void assignCouponsToList(@Valid @RequestBody Long listId)
      throws NoSuchObjectException, InvalidSessionException {

    User user = authService.getLoggedInUser()
        .orElseThrow(() -> new InvalidSessionException("Not logged in"));

    SavedList savedList = savedListService.get(listId);

    couponService.assignCouponsToList(savedList);

    TaskImpl task = taskService.getByName("duolingoTest");

    savedListService.associateTaskWithList(user, task, savedList);
  }

}
