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

package org.tctalent.server.candidateservices.providers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.candidateservices.api.dto.ServiceAssignment;
import org.tctalent.server.candidateservices.application.CandidateService;
import org.tctalent.server.candidateservices.domain.events.ServiceAssignedEvent;
import org.tctalent.server.candidateservices.domain.events.ServiceExpiredEvent;
import org.tctalent.server.candidateservices.infrastructure.importers.DuolingoCouponImporter;
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
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.email.EmailHelper;

@Service
@Slf4j
@RequiredArgsConstructor
public class DuolingoService implements CandidateService {

  private final DuolingoCouponRepository couponRepository;
  private final CandidateRepository candidateRepository;
  private final SavedListService savedListService;
  private final ApplicationEventPublisher events;
  private final DuolingoCouponImporter importer;
  private final EmailHelper emailHelper; // TODO -- SM -- replace with NotificationListener later

  private static final String PROVIDER = "DUOLINGO"; // TODO -- SM -- enum? config?

  @Override
  @Transactional
  public void importInventory(MultipartFile file) throws ImportFailedException {
    importer.importFile(file);
  }

  @Override
  @Transactional
  public ServiceAssignment assignToCandidate(Long candidateId, User user) {
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
    // TODO -- SM -- keep for now; later move to NotificationListener
    emailHelper.sendDuolingoCouponEmail(candidate.getUser());

    ServiceAssignment assignment = ServiceAssignment.builder()
        .candidateId(candidate.getId())
        .provider(PROVIDER)
        .resourceId(coupon.getCouponCode())
        .assignedAt(coupon.getDateSent())
        .attributes(Map.of("testType", coupon.getTestType().name()))
        .build();

    events.publishEvent(new ServiceAssignedEvent(assignment));
    return assignment;
  }

  @Override
  @Transactional
  public List<ServiceAssignment> assignToList(Long listId, User user) {
    SavedList savedList = savedListService.get(listId);
    Set<Candidate> candidates = savedList.getCandidates();

    // Find available coupons
    List<DuolingoCoupon> availableCoupons = getAvailableCoupons();

    if (availableCoupons.isEmpty() || candidates.size() > availableCoupons.size()) {
      throw new NoSuchObjectException(
          "There are not enough available coupons to assign to all candidates in the list. Please import more coupons from the settings page.");
    }

    List<ServiceAssignment> done = new ArrayList<>();
    for (Candidate candidate : candidates) {
      boolean hasSentCoupon = couponRepository.findAllByCandidateId(candidate.getId())
          .stream().anyMatch(coupon -> coupon.getCouponStatus() == DuolingoCouponStatus.SENT);

      if (!hasSentCoupon) {
        done.add(assignToCandidate(candidate.getId(), user));
      }
    }
    return done;
  }

//  @Override
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

//  @Override
  @Transactional(readOnly = true)
  public List<DuolingoCoupon> getAvailableCoupons() {
    return couponRepository.findByCandidateIsNullAndCouponStatusAndTestType(DuolingoCouponStatus.AVAILABLE,DuolingoTestType.PROCTORED);
  }


  @Override
  @Transactional(readOnly = true)
  public int countAvailable() {
    return couponRepository.countByCandidateIsNullAndCouponStatusAndTestType(
        DuolingoCouponStatus.AVAILABLE, DuolingoTestType.PROCTORED);
  }

//  @Override
  @Transactional(readOnly = true)
  public int countAllAvailableCoupons() {
    return couponRepository.countByCandidateIsNullAndCouponStatus(DuolingoCouponStatus.AVAILABLE);
  }

//  @Override
  @Transactional(readOnly = true)
  public int countAvailableProctoredCoupons() {
    return couponRepository.countByCandidateIsNullAndCouponStatusAndTestType(DuolingoCouponStatus.AVAILABLE,DuolingoTestType.PROCTORED);
  }

  // scheduled expiry -> publish events, not tasks
  @Transactional
  @Scheduled(cron = "0 0 0 * * ?", zone = "GMT")
  @SchedulerLock(name = "DuolingoCoupons_Expire", lockAtLeastFor = "PT23H", lockAtMostFor = "PT23H")
  public void expireOldCoupons() {
    List<DuolingoCoupon> toExpire = couponRepository
        .findAllByExpirationDateBeforeAndCouponStatusNotIn(
            LocalDateTime.now(),
            List.of(DuolingoCouponStatus.EXPIRED, DuolingoCouponStatus.REDEEMED));

    if (toExpire.isEmpty()) return;

    toExpire.forEach(c -> c.setCouponStatus(DuolingoCouponStatus.EXPIRED));
    couponRepository.saveAll(toExpire);

    toExpire.forEach(c -> events.publishEvent(
        new ServiceExpiredEvent(ServiceAssignment.builder()
            .candidateId(c.getCandidate() != null ? c.getCandidate().getId() : null)
            .provider(PROVIDER)
            .resourceId(c.getCouponCode())
            .assignedAt(c.getDateSent())
            .attributes(Map.of("previousStatus", "EXPIRED"))
            .build())
    ));
  }





  // TODO

//  @Override
  @Transactional
  public void updateCouponStatus(String couponCode, DuolingoCouponStatus status) {
    couponRepository.findByCouponCode(couponCode).ifPresent(coupon -> {
      coupon.setCouponStatus(status);
      couponRepository.save(coupon);
    });
  }


//  @Override
  @Transactional(readOnly = true)
  public DuolingoCoupon findByCouponCode(String couponCode) throws NoSuchObjectException {
    return couponRepository.findByCouponCode(couponCode)
        .orElseThrow(() -> new NoSuchObjectException("Coupon with code " + couponCode + " not found"));
  }

//  @Override
  public Candidate findCandidateByCouponCode(String couponCode) throws NoSuchObjectException{
    return couponRepository.findByCouponCode(couponCode)
        .map(DuolingoCoupon::getCandidate)
        .orElse(null);
  }


//  @Override
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

//  @Override
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
//    TaskImpl duolingoTestTask = taskService.getByName("duolingoTest");
//    List<TaskAssignmentImpl> taskAssignments = taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(
//        duolingoTestTask.getId(), candidate.getId(), Status.active);
//    for (TaskAssignmentImpl taskAssignment : taskAssignments) {
//      taskAssignment.setStatus(Status.inactive);
//      taskAssignmentService.update(
//          taskAssignment,
//          null,
//          true,
//          "Coupon was uncertified and manually reassigned a new coupon.",
//          null
//      );
//
//      LogBuilder.builder(log)
//          .user(Optional.ofNullable(user))
//          .action("ReassignProctoredCoupon")
//          .message("Marked task assignment ID " + taskAssignment.getId() + " as inactive for candidate " + candidate.getId() + " due to coupon reassignment.")
//          .logInfo();
//    }

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
//    TaskImpl claimCouponButtonTask = taskService.getByName("claimCouponButton");
//    taskAssignmentService.assignTaskToCandidate(user, claimCouponButtonTask, candidate, null, null);

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
