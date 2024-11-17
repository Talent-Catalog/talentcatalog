/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.DuolingoCouponRepository;
import org.tctalent.server.response.DuolingoCouponResponse;
import org.tctalent.server.service.db.DuolingoCouponService;

@Service
public class DuolingoCouponServiceImpl implements DuolingoCouponService {

  private final DuolingoCouponRepository couponRepository;
  private final CandidateRepository candidateRepository;

  private static final DateTimeFormatter FORMATTER1 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
  private static final DateTimeFormatter FORMATTER2 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

  @Autowired
  public DuolingoCouponServiceImpl(DuolingoCouponRepository couponRepository, CandidateRepository candidateRepository) {
    this.couponRepository = couponRepository;
    this.candidateRepository = candidateRepository;
  }

  /**
   * Imports coupons from a CSV file, avoiding duplicates.
   *
   * @param file the uploaded CSV file containing coupon data
   */
  @Override
  @Transactional
  public void importCoupons(MultipartFile file) {
    List<DuolingoCoupon> newCoupons = new ArrayList<>();
    Set<String> seenCouponCodes = new HashSet<>();

    try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      reader.readNext(); // Skip header row
      String[] line;

      while ((line = reader.readNext()) != null) {
        if (line.length < 6) continue; // Skip rows with insufficient data

        String couponCode = line[0];
        if (seenCouponCodes.contains(couponCode)) continue; // Skip if already processed
        seenCouponCodes.add(couponCode); // Mark code as processed

        if (!couponRepository.existsByCouponCode(couponCode)) {
          DuolingoCoupon coupon = new DuolingoCoupon();
          coupon.setCouponCode(couponCode);
          coupon.setAssigneeEmail(getNullableValue(line[1]));
          coupon.setExpirationDate(parseDate(line[2], FORMATTER1, FORMATTER2));
          coupon.setDateSent(parseDate(line[3], FORMATTER1, FORMATTER2));
          coupon.setCouponStatus(getNullableValue(line[4]));
          coupon.setTestStatus(getNullableValue(line[5]));
          newCoupons.add(coupon);
        }
      }
      if (!newCoupons.isEmpty()) couponRepository.saveAll(newCoupons);

    } catch (Exception e) {
      throw new RuntimeException("Failed to import coupons from CSV file", e);
    }
  }

  /**
   * Assigns an available coupon to a candidate.
   *
   * @param candidateId the ID of the candidate
   * @return the assigned Coupon if successful, or an empty Optional if not
   */
  @Override
  @Transactional
  public Optional<DuolingoCoupon> assignCouponToCandidate(Long candidateId) {
    return candidateRepository.findById(candidateId).flatMap(candidate -> {
      Optional<DuolingoCoupon> availableCoupon = couponRepository.findTop1ByCandidateIsNullAndCouponStatus("AVAILABLE");
      availableCoupon.ifPresent(coupon -> {
        coupon.setCandidate(candidate);
        coupon.setDateSent(LocalDateTime.now());
        coupon.setAssigneeEmail(candidate.getUser().getEmail());
        coupon.setCouponStatus("SENT");
        couponRepository.save(coupon);
      });
      return availableCoupon;
    });
  }

  /**
   * Retrieves coupons assigned to a candidate.
   *
   * @param candidateId the candidate's ID
   * @return a list of CouponResponse objects representing the candidate's coupons
   */
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

  /**
   * Updates the status of a coupon identified by its code.
   *
   * @param couponCode the code of the coupon to update
   * @param status the new status to set
   */
  @Override
  @Transactional
  public void updateCouponStatus(String couponCode, String status) {
    couponRepository.findByCouponCode(couponCode).ifPresent(coupon -> {
      coupon.setCouponStatus(status);
      couponRepository.save(coupon);
    });
  }

  /**
   * Retrieves all available (unassigned) coupons.
   *
   * @return a list of available Coupon entities
   */
  @Override
  @Transactional(readOnly = true)
  public List<DuolingoCoupon> getAvailableCoupons() {
    return couponRepository.findByCandidateIsNullAndCouponStatus("AVAILABLE");
  }

  /**
   * Finds a coupon by its unique code and maps it to a CouponResponse.
   *
   * @param couponCode the code of the coupon to find
   * @return an Optional containing the mapped CouponResponse, or empty if not found
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<DuolingoCouponResponse> findByCouponCode(String couponCode) {
    return couponRepository.findByCouponCode(couponCode).map(this::mapToCouponResponse);
  }

  // Utility Methods

  /**
   * Maps a Coupon entity to a CouponResponse DTO.
   */
  private DuolingoCouponResponse mapToCouponResponse(DuolingoCoupon coupon) {
    return new DuolingoCouponResponse(
        coupon.getId(),
        coupon.getCouponCode(),
        coupon.getAssigneeEmail(),
        coupon.getExpirationDate(),
        coupon.getDateSent(),
        coupon.getCouponStatus(),
        coupon.getTestStatus()
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
        } catch (DateTimeParseException ignored) {}
      }
      throw new RuntimeException("Invalid date format: " + dateString);
    }
    return null;
  }
}
