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

package org.tctalent.server.service.db.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.TaskAssignmentRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.DuolingoCouponRepository;
import org.tctalent.server.response.DuolingoCouponResponse;
import org.tctalent.server.service.db.DuolingoCouponService;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.TaskService;

@Service
@Slf4j
public class DuolingoCouponServiceImpl implements DuolingoCouponService {

  private final AuthService authService;
  private final DuolingoCouponRepository couponRepository;
  private final CandidateRepository candidateRepository;
  private final TaskAssignmentRepository taskAssignmentRepository;
  private final TaskService taskService;

  private static final DateTimeFormatter FORMATTER1 = DateTimeFormatter.ofPattern(
      "yyyy/MM/dd HH:mm:ss");
  private static final DateTimeFormatter FORMATTER2 = DateTimeFormatter.ofPattern(
      "yyyy/MM/dd HH:mm");

  @Autowired
  public DuolingoCouponServiceImpl(DuolingoCouponRepository couponRepository,
      CandidateRepository candidateRepository,
      TaskAssignmentRepository taskAssignmentRepository,
      AuthService authService,
      TaskService taskService) {
    this.authService = authService;
    this.couponRepository = couponRepository;
    this.candidateRepository = candidateRepository;
    this.taskAssignmentRepository = taskAssignmentRepository;
    this.taskService = taskService;
  }


  @Override
  @Transactional
  public void importCoupons(MultipartFile file) {
    List<DuolingoCoupon> newCoupons = new ArrayList<>();
    Set<String> seenCouponCodes = new HashSet<>();

    try (CSVReader reader = new CSVReader(
        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      reader.readNext(); // Skip header row
      String[] line;

      while ((line = reader.readNext()) != null) {
        if (line.length < 6) {
          continue; // Skip rows with insufficient data
        }

        String couponCode = line[0];
        if (seenCouponCodes.contains(couponCode)) {
          continue; // Skip if already processed
        }
        seenCouponCodes.add(couponCode); // Mark code as processed

        if (!couponRepository.existsByCouponCode(couponCode)) {
          DuolingoCoupon coupon = new DuolingoCoupon();
          coupon.setCouponCode(couponCode);
          coupon.setExpirationDate(parseDate(line[2], FORMATTER1, FORMATTER2));
          coupon.setDateSent(parseDate(line[3], FORMATTER1, FORMATTER2));
          coupon.setCouponStatus(DuolingoCouponStatus.valueOf(getNullableValue(line[4]).toUpperCase()));
          newCoupons.add(coupon);
        }
      }
      if (!newCoupons.isEmpty()) {
        couponRepository.saveAll(newCoupons);
      }
    } catch (IOException | CsvValidationException e) {
      throw new ImportFailedException(e);
    }
  }


  @Override
  @Transactional
  public DuolingoCoupon assignCouponToCandidate(Long candidateId)
      throws NoSuchObjectException, EntityExistsException {
    return candidateRepository.findById(candidateId).map(candidate -> {
      // Find the first available coupon
      Optional<DuolingoCoupon> availableCoupon = couponRepository.findTop1ByCandidateIsNullAndCouponStatus(
          DuolingoCouponStatus.AVAILABLE);

      if (availableCoupon.isEmpty()) {
        // Throw exception if no coupon is available
        throw new NoSuchObjectException("No available coupons for candidate ID " + candidateId);
      }

      // Assign the coupon to the candidate and update its status
      DuolingoCoupon coupon = availableCoupon.get();
      coupon.setCandidate(candidate);
      coupon.setDateSent(LocalDateTime.now());
      coupon.setCouponStatus(DuolingoCouponStatus.SENT);
      couponRepository.save(coupon);

      List<TaskAssignmentImpl> taskAssignments = candidate.getTaskAssignments();

      // Check if there's any "duolingoTest" task with an active status
      boolean foundDuolingoTest = taskAssignments.stream()
          .anyMatch(taskAssignment ->
              "duolingoTest".equals(taskAssignment.getTask().getName()) &&
                  Status.active.equals(taskAssignment.getStatus())
          );

      if (!foundDuolingoTest) {
        TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();
        TaskImpl task = taskService.getByName("duolingoTest");

        // Get the logged-in user or throw an exception if not logged in
        User user = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("User is not logged in"));

        // Set task assignment properties
        taskAssignment.setTask(task);
        taskAssignment.setActivatedBy(user);
        taskAssignment.setActivatedDate(OffsetDateTime.now());
        taskAssignment.setCandidate(candidate);
        taskAssignment.setStatus(Status.active);

        // Save the task assignment
        taskAssignmentRepository.save(taskAssignment);
      }

      return coupon;
    }).orElseThrow(() -> new NoSuchObjectException("Candidate with ID " + candidateId + " not found"));
  }



  @Override
  @Transactional(readOnly = true)
  public List<DuolingoCouponResponse> getCouponsForCandidate(Long candidateId) {
    List<DuolingoCoupon> coupons = couponRepository.findAllByCandidateId(candidateId);
    List<DuolingoCouponResponse> couponResponses = new ArrayList<>();
    for (DuolingoCoupon coupon : coupons) {
      couponResponses.add(mapToCouponResponse(coupon));
    }
    return couponResponses;
  }


  @Override
  @Transactional
  public void updateCouponStatus(String couponCode, DuolingoCouponStatus status) {
    couponRepository.findByCouponCode(couponCode).ifPresent(coupon -> {
      coupon.setCouponStatus(status);
      couponRepository.save(coupon);
    });
  }


  @Override
  @Transactional(readOnly = true)
  public List<DuolingoCoupon> getAvailableCoupons() {
    return couponRepository.findByCandidateIsNullAndCouponStatus(DuolingoCouponStatus.AVAILABLE);
  }

  @Override
  @Transactional(readOnly = true)
  public DuolingoCoupon findByCouponCode(String couponCode) throws NoSuchObjectException {
    return couponRepository.findByCouponCode(couponCode)
        .orElseThrow(() -> new NoSuchObjectException("Coupon with code " + couponCode + " not found"));
  }

  @Override
  public Candidate findCandidateByCouponCode(String couponCode) throws NoSuchObjectException{
    return couponRepository.findByCouponCode(couponCode)
        .map(DuolingoCoupon::getCandidate)
        .orElse(null);
  }

  @Override
  @Transactional
  public void assignCouponsToList(SavedList list) throws NoSuchObjectException {
    Set<Candidate> candidates = list.getCandidates();

    // Find if there is available coupons
    List<DuolingoCoupon> availableCoupons = getAvailableCoupons();

    if (availableCoupons.isEmpty() || candidates.size() > availableCoupons.size()) {
      // Throw exception if no coupon are available, or if there are more candidates than coupons
      throw new NoSuchObjectException("No available coupons for list ID " + list.getId());
    }

    for (Candidate candidate : candidates) {
      //Assign coupon to candidate if they do not have one
      List<DuolingoCoupon> coupons = couponRepository.findAllByCandidateId(candidate.getId());
      // Assign a coupon if no coupons exist or none are in the SENT status
      boolean hasSentCoupon = coupons.stream()
          .anyMatch(coupon -> coupon.getCouponStatus() == DuolingoCouponStatus.SENT);
      if (!hasSentCoupon) {
        assignCouponToCandidate(candidate.getId());
      }
    }
  }
  // Utility Methods

  /**
   * Maps a Coupon entity to a CouponResponse DTO.
   */
  private DuolingoCouponResponse mapToCouponResponse(DuolingoCoupon coupon) {
    return new DuolingoCouponResponse(
        coupon.getId(),
        coupon.getCouponCode(),
        coupon.getExpirationDate(),
        coupon.getDateSent(),
        coupon.getCouponStatus()
    );
  }

  /**
   * Returns a nullable string, or null if the value is empty.
   */
  private String getNullableValue(String value) {
    return value != null && !value.trim().isEmpty() ? value : null;
  }

  /**
   * Attempts to parse a date using multiple formatters.
   */
  private LocalDateTime parseDate(String dateString, DateTimeFormatter... formatters) {
    if (dateString != null && !dateString.trim().isEmpty()) {
      for (DateTimeFormatter formatter : formatters) {
        try {
          return LocalDateTime.parse(dateString, formatter);
        } catch (DateTimeParseException ex) {
          LogBuilder.builder(log)
              .action("parseDate")
              .message(String.format("Failed to parse date '%s' with formatter '%s'. Exception: %s",
                  dateString, formatter, ex.getMessage()))
              .logError(ex);

        }
      }
      throw new RuntimeException("Invalid date format: " + dateString);
    }
    return null;
  }
}
