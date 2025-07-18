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
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.model.db.DuolingoTestType;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.DuolingoCouponRepository;
import org.tctalent.server.response.DuolingoCouponResponse;
import org.tctalent.server.service.db.DuolingoCouponService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskService;
import org.tctalent.server.service.db.email.EmailHelper;

@Service
@Slf4j
public class DuolingoCouponServiceImpl implements DuolingoCouponService {

  private final DuolingoCouponRepository couponRepository;
  private final CandidateRepository candidateRepository;
  private final EmailHelper emailHelper;
  private final SavedListService savedListService;
  private final TaskAssignmentService taskAssignmentService;
  private final TaskService taskService;

  private static final DateTimeFormatter FORMATTER1 = DateTimeFormatter.ofPattern(
      "yyyy/MM/dd HH:mm:ss");
  private static final DateTimeFormatter FORMATTER2 = DateTimeFormatter.ofPattern(
      "yyyy/MM/dd HH:mm");

  @Autowired
  public DuolingoCouponServiceImpl(DuolingoCouponRepository couponRepository,
      CandidateRepository candidateRepository,
      SavedListService savedListService,
      TaskAssignmentService taskAssignmentService,
      TaskService taskService,
      EmailHelper emailHelper) {
    this.couponRepository = couponRepository;
    this.candidateRepository = candidateRepository;
    this.emailHelper = emailHelper;
    this.savedListService = savedListService;
    this.taskAssignmentService = taskAssignmentService;
    this.taskService = taskService;
  }


  @Override
  @Transactional
  public void importCoupons(MultipartFile file) throws ImportFailedException {
    List<DuolingoCoupon> newCoupons = new ArrayList<>();
    // Set to track coupon codes that have already been processed (avoids duplicates)
    Set<String> seenCouponCodes = new HashSet<>();

    try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      // Parse the header of the CSV to ensure it is valid
      String[] headers = parseCsvHeader(reader);

      // Map column names to their respective indices for easy access
      Map<String, Integer> columnIndex = mapColumnsToIndex(headers);

      // Validate that all required columns are present in the CSV
      validateRequiredColumns(columnIndex);

      // Read each line of the CSV and process coupon data
      String[] line;
      while ((line = reader.readNext()) != null) {
        processCouponLine(line, headers, columnIndex, seenCouponCodes, newCoupons);
      }

      // Save all the new coupons to the repository if there are any
      saveCoupons(newCoupons);

    } catch (ImportFailedException | IOException | CsvValidationException e) {
      // Catch any exceptions related to the import process
      throw new ImportFailedException(e);
    }
  }

    // Parses the CSV header row to ensure the file has valid headers.
   private String[] parseCsvHeader(CSVReader reader)
      throws ImportFailedException, CsvValidationException, IOException {
    // Read the first row (header) from the CSV
    String[] headers = reader.readNext();

    // If no headers are found, throw an exception
    if (headers == null) {
      throw new ImportFailedException("CSV header is missing");
    }
    return headers;
  }

  // Maps column names to their respective indices based on the CSV header.
  private Map<String, Integer> mapColumnsToIndex(String[] headers) {
    Map<String, Integer> columnIndex = new HashMap<>();

    // Normalize header names (lowercase, strip whitespace, remove BOM) and map them to their indices
    for (int i = 0; i < headers.length; i++) {
      String normalizedHeader = headers[i].toLowerCase().replace("\uFEFF", "").strip();
      columnIndex.put(normalizedHeader, i);
    }
    return columnIndex;
  }
    // Validates that all required columns are present in the CSV file.
  private void validateRequiredColumns(Map<String, Integer> columnIndex) throws ImportFailedException {
    // List of required columns that must be present in the CSV
    String[] requiredColumns = {"coupon code", "expiration date", "date sent", "coupon status"};

    // Check if all required columns are in the CSV
    for (String column : requiredColumns) {
      if (!columnIndex.containsKey(column)) {
        throw new ImportFailedException("Missing required column: " + column);
      }
    }
  }

  /**
   * Processes each coupon line from the CSV and adds it to the newCoupons list if it's valid.
   * @param line the current line from the CSV
   * @param headers the array of headers from the CSV
   * @param columnIndex the map of column names to their indices
   * @param seenCouponCodes a set to track processed coupon codes
   * @param newCoupons the list where valid coupons will be added
   */
  private void processCouponLine(String[] line, String[] headers, Map<String, Integer> columnIndex, Set<String> seenCouponCodes, List<DuolingoCoupon> newCoupons) {
    // Ensure the line has enough columns to process
    if (line.length >= headers.length) {
      // Extract the coupon code from the current line
      String couponCode = line[columnIndex.get("coupon code")];

      // Check if the coupon code has already been processed
      if (!seenCouponCodes.contains(couponCode)) {
        seenCouponCodes.add(couponCode); // Mark this coupon code as processed

        // If the coupon code does not exist in the database, create a new coupon
        if (!couponRepository.existsByCouponCode(couponCode)) {
          DuolingoCoupon coupon = new DuolingoCoupon();
          coupon.setCouponCode(couponCode);
          coupon.setExpirationDate(parseDate(line[columnIndex.get("expiration date")], FORMATTER1, FORMATTER2));
          coupon.setDateSent(parseDate(line[columnIndex.get("date sent")], FORMATTER1, FORMATTER2));
          coupon.setCouponStatus(DuolingoCouponStatus.valueOf(getNullableValue(line[columnIndex.get("coupon status")]).toUpperCase()));
          // Set test type based on coupon code prefix
          if (couponCode.startsWith("ACCNONPROC") || couponCode.startsWith("NONP")) {
            coupon.setTestType(DuolingoTestType.NON_PROCTORED);
          } else if (couponCode.startsWith("ACC") || couponCode.startsWith("PROC")) {
            coupon.setTestType(DuolingoTestType.PROCTORED);
          }
          newCoupons.add(coupon);
        }
      }
    }
  }

  // Saves the new coupons to the database if any valid coupons exist.
  private void saveCoupons(List<DuolingoCoupon> newCoupons) {
    if (!newCoupons.isEmpty()) {
      couponRepository.saveAll(newCoupons);
    }
  }

  @Override
  @Transactional
  public DuolingoCouponResponse assignCouponToCandidate(Long candidateId, User user)
      throws NoSuchObjectException, EntityExistsException {

    List<DuolingoCouponResponse> coupons = getCouponsForCandidate(candidateId);

    for (DuolingoCouponResponse coupon : coupons) {
      if (coupon.getDuolingoCouponStatus().equals(DuolingoCouponStatus.SENT)) {
        throw new EntityExistsException("coupon", "for this candidate");
      }
    }

    Candidate candidate = candidateRepository.findById(candidateId)
        .orElseThrow(() -> new NoSuchObjectException("Candidate with ID " + candidateId + " not found"));
    Optional<DuolingoCoupon> availableCoupon = couponRepository.findTop1ByCandidateIsNullAndCouponStatusAndTestType(
        DuolingoCouponStatus.AVAILABLE, DuolingoTestType.PROCTORED);

    if (availableCoupon.isEmpty()) {
      throw new NoSuchObjectException(
          "There are no available coupons to assign to the candidate. Please import more coupons from the settings page.");
    }

    DuolingoCoupon coupon = availableCoupon.get();
    coupon.setCandidate(candidate);
    coupon.setDateSent(LocalDateTime.now());
    coupon.setCouponStatus(DuolingoCouponStatus.SENT);
    couponRepository.save(coupon);
    emailHelper.sendDuolingoCouponEmail(candidate.getUser());

    TaskImpl claimCouponButtonTask = taskService.getByName("claimCouponButton");
    taskAssignmentService.assignTaskToCandidate(user, claimCouponButtonTask, candidate, null, null);

    return new DuolingoCouponResponse(
        coupon.getId(),
        coupon.getCouponCode(),
        coupon.getExpirationDate(),
        coupon.getDateSent(),
        coupon.getCouponStatus()
    );
  }

  @Override
  @Transactional
  public void assignCouponsToList(Long listId, User user) throws NoSuchObjectException {
    SavedList savedList = savedListService.get(listId);
    Set<Candidate> candidates = savedList.getCandidates();

    // Find available coupons
    List<DuolingoCoupon> availableCoupons = getAvailableCoupons();

    if (availableCoupons.isEmpty() || candidates.size() > availableCoupons.size()) {
      throw new NoSuchObjectException(
          "There are not enough available coupons to assign to all candidates in the list. Please import more coupons from the settings page.");
    }

    for (Candidate candidate : candidates) {
      boolean hasSentCoupon = couponRepository.findAllByCandidateId(candidate.getId())
          .stream().anyMatch(coupon -> coupon.getCouponStatus() == DuolingoCouponStatus.SENT);

      if (!hasSentCoupon) {
        assignCouponToCandidate(candidate.getId(), user);
      }
    }
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
    return couponRepository.findByCandidateIsNullAndCouponStatusAndTestType(DuolingoCouponStatus.AVAILABLE,DuolingoTestType.PROCTORED);
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

  @Override
  @Transactional(readOnly = true)
  public int countAllAvailableCoupons() {
    return couponRepository.countByCandidateIsNullAndCouponStatus(DuolingoCouponStatus.AVAILABLE);
  }

  @Override
  @Transactional(readOnly = true)
  public int countAvailableProctoredCoupons() {
    return couponRepository.countByCandidateIsNullAndCouponStatusAndTestType(DuolingoCouponStatus.AVAILABLE,DuolingoTestType.PROCTORED);
  }

  @Override
  @Transactional
  public DuolingoCouponResponse reassignProctoredCouponToCandidate(String candidateNumber, User user)
      throws NoSuchObjectException {
    // Find the candidate
    Candidate candidate = candidateRepository.findByCandidateNumber(candidateNumber);
    if(candidate==null){
      throw  new NoSuchObjectException("Candidate with Number " + candidateNumber + " not found");
    }

    // Find all existing coupons for the candidate and mark them as REDEEMED
    List<DuolingoCoupon> existingCoupons = couponRepository.findAllByCandidateId(candidate.getId());
    for (DuolingoCoupon existingCoupon : existingCoupons) {
      existingCoupon.setCouponStatus(DuolingoCouponStatus.REDEEMED);
      couponRepository.save(existingCoupon);
    }

    // Find and resolve existing Duolingo-related task assignments
    TaskImpl duolingoTestTask = taskService.getByName("duolingoTest");
    List<TaskAssignmentImpl> taskAssignments = taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(
        duolingoTestTask.getId(), candidate.getId(), Status.active);
    for (TaskAssignmentImpl taskAssignment : taskAssignments) {
      taskAssignment.setStatus(Status.inactive);
      taskAssignmentService.update(
          taskAssignment,
          null,
          true,
          "Coupon was uncertified and manually reassigned a new coupon.",
          null
      );

      LogBuilder.builder(log)
          .user(Optional.ofNullable(user))
          .action("ReassignProctoredCoupon")
          .message("Marked task assignment ID " + taskAssignment.getId() + " as inactive for candidate " + candidate.getId() + " due to coupon reassignment.")
          .logInfo();
    }

    // Find an available coupon
    Optional<DuolingoCoupon> availableCoupon = couponRepository.findTop1ByCandidateIsNullAndCouponStatusAndTestType(
        DuolingoCouponStatus.AVAILABLE, DuolingoTestType.PROCTORED);

    if (availableCoupon.isEmpty()) {
      throw new NoSuchObjectException(
          "There are no available coupons to assign to the candidate. Please import more coupons from the settings page.");
    }

    // Assign the new coupon to the candidate
    DuolingoCoupon newCoupon = availableCoupon.get();
    newCoupon.setCandidate(candidate);
    newCoupon.setDateSent(LocalDateTime.now());
    newCoupon.setCouponStatus(DuolingoCouponStatus.SENT);
    couponRepository.save(newCoupon);

    // Send email with the new coupon
    emailHelper.sendDuolingoCouponEmail(candidate.getUser());

    // Assign the claim coupon task
    TaskImpl claimCouponButtonTask = taskService.getByName("claimCouponButton");
    taskAssignmentService.assignTaskToCandidate(user, claimCouponButtonTask, candidate, null, null);

    // Log the reassignment
    LogBuilder.builder(log)
        .user(Optional.ofNullable(user))
        .action("ReassignProctoredCoupon")
        .message("Reassigned new coupon " + newCoupon.getCouponCode() + " to candidate " + candidate.getId())
        .logInfo();
    // Return the response for the new coupon
    return new DuolingoCouponResponse(
        newCoupon.getId(),
        newCoupon.getCouponCode(),
        newCoupon.getExpirationDate(),
        newCoupon.getDateSent(),
        newCoupon.getCouponStatus()
    );
  }
}
