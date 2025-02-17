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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.DuolingoCouponRepository;
import org.tctalent.server.response.DuolingoCouponResponse;
import org.tctalent.server.service.db.DuolingoCouponService;
import org.tctalent.server.service.db.email.EmailHelper;

@Service
@Slf4j
public class DuolingoCouponServiceImpl implements DuolingoCouponService {

  private final DuolingoCouponRepository couponRepository;
  private final CandidateRepository candidateRepository;
  private final EmailHelper emailHelper;

  private static final DateTimeFormatter FORMATTER1 = DateTimeFormatter.ofPattern(
      "yyyy/MM/dd HH:mm:ss");
  private static final DateTimeFormatter FORMATTER2 = DateTimeFormatter.ofPattern(
      "yyyy/MM/dd HH:mm");

  @Autowired
  public DuolingoCouponServiceImpl(DuolingoCouponRepository couponRepository,
      CandidateRepository candidateRepository,
      EmailHelper emailHelper) {
    this.couponRepository = couponRepository;
    this.candidateRepository = candidateRepository;
    this.emailHelper = emailHelper;
  }


  @Override
  @Transactional
  public void importCoupons(MultipartFile file) throws ImportFailedException {
    // List to hold the new coupons to be saved
    List<DuolingoCoupon> newCoupons = new ArrayList<>();
    // Set to track already processed coupon codes to avoid duplicates
    Set<String> seenCouponCodes = new HashSet<>();

    try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      // Read the header row of the CSV and map column names to their indices
      String[] headers = reader.readNext();
      if (headers == null) {
        // Throw an exception if the CSV file does not have a header row
        throw new ImportFailedException("CSV header is missing");
      }

      // Map column names (after cleaning) to their respective indices
      Map<String, Integer> columnIndex = new HashMap<>();
      for (int i = 0; i < headers.length; i++) {
        // Normalize the header (lowercase, strip whitespaces and BOM characters)
        String normalizedHeader = headers[i].toLowerCase().replace("\uFEFF", "").strip();
        columnIndex.put(normalizedHeader, i);
      }

      // Validate that all required columns are present
      String[] requiredColumns = {"coupon code", "expiration date", "date sent", "coupon status"};
      for (String column : requiredColumns) {
        if (!columnIndex.containsKey(column)) {
          // Throw exception if any required column is missing
          throw new ImportFailedException("Missing required column: " + column);
        }
      }

      // Get the indices for each relevant column based on the header mapping
      int couponCodeIndex = columnIndex.get("coupon code");
      int expirationDateIndex = columnIndex.get("expiration date");
      int dateSentIndex = columnIndex.get("date sent");
      int couponStatusIndex = columnIndex.get("coupon status");

      // Read each line in the CSV
      String[] line;
      while ((line = reader.readNext()) != null) {
        // Ensure the line has the required number of columns
        if (line.length >= headers.length) {
          String couponCode = line[couponCodeIndex];
          // Check if the coupon code is already processed (avoid duplicates)
          if (!seenCouponCodes.contains(couponCode)) {
            seenCouponCodes.add(couponCode);

            // Check if the coupon code already exists in the database
            if (!couponRepository.existsByCouponCode(couponCode)) {
              // Create a new DuolingoCoupon object
              DuolingoCoupon coupon = new DuolingoCoupon();
              coupon.setCouponCode(couponCode);
              coupon.setExpirationDate(parseDate(line[expirationDateIndex], FORMATTER1, FORMATTER2));
              coupon.setDateSent(parseDate(line[dateSentIndex], FORMATTER1, FORMATTER2));
              coupon.setCouponStatus(DuolingoCouponStatus.valueOf(getNullableValue(line[couponStatusIndex]).toUpperCase()));
              newCoupons.add(coupon);
            }
          }
        }
      }

      // Save all new coupons to the database if there are any
      if (!newCoupons.isEmpty()) {
        couponRepository.saveAll(newCoupons);
      }

    } catch (Exception e) {
      throw new ImportFailedException("An error occurred during the import process: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public DuolingoCoupon assignCouponToCandidate(Long candidateId)
      throws NoSuchObjectException {
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
      emailHelper.sendDuolingoCouponEmail(candidate.getUser());

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

  @Override
  @Transactional
  @Scheduled(cron = "0 0 0 * * ?", zone = "GMT")
  @SchedulerLock(name = "CouponSchedulerTask_markCouponsAsExpired", lockAtLeastFor = "PT23H", lockAtMostFor = "PT23H")
  public void markCouponsAsExpired() {
    // Exclude both EXPIRED and REDEEMED statuses
    List<DuolingoCoupon> expiredCoupons = couponRepository.findAllByExpirationDateBeforeAndCouponStatusNotIn(
        LocalDateTime.now(),
        List.of(DuolingoCouponStatus.EXPIRED, DuolingoCouponStatus.REDEEMED)
    );

    if (!expiredCoupons.isEmpty()) {
      expiredCoupons.forEach(coupon -> coupon.setCouponStatus(DuolingoCouponStatus.EXPIRED));
      couponRepository.saveAll(expiredCoupons);
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
