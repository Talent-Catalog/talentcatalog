package org.tctalent.server.service.db;

import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.response.DuolingoCouponResponse;

/**
 * Service interface for managing Coupon-related operations.
 * <p/>
 * This interface defines various methods for importing, assigning, and retrieving
 * coupons, as well as updating their statuses within the system.
 */
public interface DuolingoCouponService {

  /**
   * Imports coupons from a CSV file.
   * <p/>
   * This method processes a CSV file containing coupon data and
   * adds the coupons to the system database. It is useful for bulk
   * importing coupons.
   *
   * @param file the MultipartFile representing the CSV file to import.
   */
  void importCoupons(MultipartFile file) throws ImportFailedException;

  /**
   * Assigns an available coupon to a specified candidate.
   * <p/>
   * This method finds an unassigned coupon and associates it with
   * the candidate identified by the provided ID.
   *
   * @param candidateId the ID of the candidate to whom the coupon will be assigned.
   * @return an Optional containing the assigned Coupon if successful, or empty if no coupons are available.
   */
  Optional<DuolingoCoupon> assignCouponToCandidate(Long candidateId);

  /**
   * Retrieves all coupons assigned to a specific candidate.
   * <p/>
   * This method returns a list of coupons associated with the candidate,
   * providing details in the form of CouponResponse objects.
   *
   * @param candidateId the ID of the candidate whose coupons are being retrieved.
   * @return a List of CouponResponse objects representing the assigned coupons.
   */
  List<DuolingoCouponResponse> getCouponsForCandidate(Long candidateId);

  /**
   * Updates the status of a specific coupon.
   * <p/>
   * This method modifies the status of a coupon based on its unique code.
   * It can be used to set statuses like "ACTIVE", "EXPIRED", etc.
   *
   * @param couponCode the unique code identifying the coupon to update.
   * @param status the new status to be assigned to the coupon.
   */
  void updateCouponStatus(String couponCode, DuolingoCouponStatus status);

  /**
   * Retrieves all available (unassigned) coupons.
   * <p/>
   * This method provides a list of coupons that are not currently assigned
   * to any candidate, allowing these coupons to be available for assignment.
   *
   * @return a List of Coupons that are unassigned and available.
   */
  List<DuolingoCoupon> getAvailableCoupons();

  /**
   * Finds a coupon by its unique coupon code.
   * <p/>
   * This method retrieves a coupon using its unique code and returns its details
   * wrapped in a CouponResponse object if found.
   *
   * @param couponCode the unique code identifying the coupon to retrieve.
   * @return an Optional containing the CouponResponse if found, otherwise empty.
   */
  Optional<DuolingoCouponResponse> findByCouponCode(String couponCode);
}
