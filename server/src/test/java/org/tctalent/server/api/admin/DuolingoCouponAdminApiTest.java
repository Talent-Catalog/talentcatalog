package org.tctalent.server.api.admin;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.DuolingoTestData.getDuolingoCoupon;
import static org.tctalent.server.data.TaskTestData.getTask;
import static org.tctalent.server.data.TaskTestData.getTaskAssignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.request.duolingo.UpdateDuolingoCouponStatusRequest;
import org.tctalent.server.response.DuolingoCouponResponse;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.DuolingoCouponService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskService;

/**
 * Unit tests for Duolingo Coupon Admin Api endpoints.
 *
 * @author Ehsan Ehrari
 */
@WebMvcTest(DuolingoCouponAdminApi.class)
@AutoConfigureMockMvc
class DuolingoCouponAdminApiTest extends ApiTestBase {

  private static final long CANDIDATE_ID = 99L;
  private static final String COUPON_CODE = "COUPON123";
  private static final String BASE_PATH = "/api/admin/coupon";
  private static final String ASSIGN_COUPON_PATH = "/{candidateId}/assign";
  private static final String FIND_COUPON_PATH = "find/{couponCode}";
  private static final String UPDATE_STATUS_PATH = "status";
  private static final String ASSIGN_TO_LIST_PATH = "/assign-to-list";
  private static final String DUOLINGO_TEST_TASK_NAME = "duolingoTest";

  private final DuolingoCoupon coupon = getDuolingoCoupon();
  private static final Candidate candidate = getCandidate();
  private static final TaskImpl task = getTask();
  private static final TaskAssignmentImpl taskAssignment = getTaskAssignment();

  @MockBean
  AuthService authService;
  @MockBean
  DuolingoCouponService couponService;
  @MockBean
  SavedListService savedListService;
  @MockBean
  TaskAssignmentService taskAssignmentService;
  @MockBean
  TaskService taskService;
  @MockBean
  CandidateService candidateService;


  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  @DisplayName("Import coupons from CSV succeeds")
  void importCouponsFromCsvSucceeds() throws Exception {
    willDoNothing().given(couponService).importCoupons(any());

    // Create a mock file for testing
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "test-coupons.csv",
        "text/csv",
        "couponCode1,expirationDate,dateSent,couponStatus\nCOUPON123,2025-01-01,2024-12-01,AVAILABLE".getBytes()
    );

    mockMvc.perform(multipart(BASE_PATH + "/import")
            .file(file)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.message", is("Coupons imported successfully.")));

    // Verify that the importCoupons method was called once
    verify(couponService).importCoupons(any());
  }


  @Test
  @DisplayName("Get coupons for candidate succeeds")
  void getCouponsForCandidateSucceeds() throws Exception {
    given(couponService.getCouponsForCandidate(anyLong())).willReturn(List.of(new DuolingoCouponResponse(
        coupon.getId(),
        coupon.getCouponCode(),
        coupon.getExpirationDate(),
        coupon.getDateSent(),
        coupon.getCouponStatus()
    )));

    mockMvc.perform(get(BASE_PATH + "/{candidateId}", CANDIDATE_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].couponCode", is(COUPON_CODE)))
        .andExpect(jsonPath("$[0].duolingoCouponStatus", is("AVAILABLE")));

    verify(couponService).getCouponsForCandidate(anyLong());
  }

  @Test
  @DisplayName("Update coupon status succeeds")
  void updateCouponStatusSucceeds() throws Exception {
    UpdateDuolingoCouponStatusRequest request = new UpdateDuolingoCouponStatusRequest();
    request.setCouponCode(COUPON_CODE);
    request.setStatus(DuolingoCouponStatus.REDEEMED);

    mockMvc.perform(put(BASE_PATH + "/" + UPDATE_STATUS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());

    verify(couponService).updateCouponStatus(COUPON_CODE, DuolingoCouponStatus.REDEEMED);
  }

  @Test
  @DisplayName("Get coupon by code succeeds")
  void getCouponByCodeSucceeds() throws Exception {
    given(couponService.findByCouponCode(any(String.class))).willReturn(coupon);
    System.out.println(BASE_PATH + "/" +  FIND_COUPON_PATH);
    mockMvc.perform(get(BASE_PATH + "/" +  FIND_COUPON_PATH, COUPON_CODE)
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.couponCode", is(COUPON_CODE)))
        .andExpect(jsonPath("$.duolingoCouponStatus", is("AVAILABLE")));

    verify(couponService).findByCouponCode(COUPON_CODE);
  }

  @Test
  @DisplayName("Assign coupons to list succeeds")
  void assignCouponsToListSucceeds() throws Exception {
    Long testListId = 1L;

    given(authService.getLoggedInUser()).willReturn(Optional.of(user));

    mockMvc.perform(post(BASE_PATH + ASSIGN_TO_LIST_PATH)
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testListId)))
        .andDo(print())
        .andExpect(status().isOk());

    verify(authService).getLoggedInUser();
    verify(couponService).assignCouponsToList(testListId, user);
  }


  @Test
  @DisplayName("Assign coupon to candidate succeeds")
  void assignCouponToCandidateSucceeds() throws Exception {
    // Mock objects
    DuolingoCouponResponse couponResponse = new DuolingoCouponResponse(
        123L,
        "ABC123",
        LocalDateTime.of(2025, 3, 1, 12, 0),
        LocalDateTime.now(),
        DuolingoCouponStatus.AVAILABLE
    );


    given(authService.getLoggedInUser()).willReturn(Optional.of(user));
    given(couponService.assignCouponToCandidate(anyLong(), eq(user))).willReturn(couponResponse);
    given(taskService.getByName(DUOLINGO_TEST_TASK_NAME)).willReturn(task);
    given(candidateService.getCandidate(anyLong())).willReturn(candidate);
    given(taskAssignmentService.assignTaskToCandidate(user, task, candidate, null, null))
        .willReturn(taskAssignment);

    mockMvc.perform(post(BASE_PATH + ASSIGN_COUPON_PATH, CANDIDATE_ID)
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.couponCode").value("ABC123"))
        .andExpect(jsonPath("$.duolingoCouponStatus").value("AVAILABLE"));

    // Verifications
    verify(authService).getLoggedInUser();
    verify(couponService).assignCouponToCandidate(anyLong(), eq(user));
  }

  @Test
  @DisplayName("Import coupons from CSV returns failure when service throws")
  void importCouponsFromCsvReturnsFailureWhenServiceThrows() throws Exception {
    willThrow(new RuntimeException("csv error"))
        .given(couponService)
        .importCoupons(any());

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "bad-coupons.csv",
        "text/csv",
        "bad-data".getBytes()
    );

    mockMvc.perform(multipart(BASE_PATH + "/import")
            .file(file)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status", is("failure")))
        .andExpect(jsonPath("$.message", is("Failed to import coupons from CSV file.")));

    verify(couponService).importCoupons(any());
  }

  @Test
  @DisplayName("Get available coupons succeeds")
  void getAvailableCouponsSucceeds() throws Exception {
    given(couponService.getAvailableCoupons()).willReturn(List.of(coupon));

    mockMvc.perform(get(BASE_PATH + "/available")
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].couponCode", is(COUPON_CODE)))
        .andExpect(jsonPath("$[0].couponStatus", is("AVAILABLE")));

    verify(couponService).getAvailableCoupons();
  }

  @Test
  @DisplayName("Count all available coupons succeeds")
  void countAllAvailableCouponsSucceeds() throws Exception {
    given(couponService.countAllAvailableCoupons()).willReturn(12);

    mockMvc.perform(get(BASE_PATH + "/count-all")
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.count", is(12)));

    verify(couponService).countAllAvailableCoupons();
  }

  @Test
  @DisplayName("Count all available coupons returns failure when service throws")
  void countAllAvailableCouponsReturnsFailureWhenServiceThrows() throws Exception {
    given(couponService.countAllAvailableCoupons())
        .willThrow(new RuntimeException("count error"));

    mockMvc.perform(get(BASE_PATH + "/count-all")
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status", is("failure")))
        .andExpect(jsonPath("$.message", is("Failed to count available coupons.")));

    verify(couponService).countAllAvailableCoupons();
  }

  @Test
  @DisplayName("Count available proctored coupons succeeds")
  void countAvailableProctoredCouponsSucceeds() throws Exception {
    given(couponService.countAvailableProctoredCoupons()).willReturn(7);

    mockMvc.perform(get(BASE_PATH + "/count-proctored")
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.count", is(7)));

    verify(couponService).countAvailableProctoredCoupons();
  }

  @Test
  @DisplayName("Count available proctored coupons returns failure when service throws")
  void countAvailableProctoredCouponsReturnsFailureWhenServiceThrows() throws Exception {
    given(couponService.countAvailableProctoredCoupons())
        .willThrow(new RuntimeException("count error"));

    mockMvc.perform(get(BASE_PATH + "/count-proctored")
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status", is("failure")))
        .andExpect(jsonPath("$.message", is("Failed to count available proctored coupons.")));

    verify(couponService).countAvailableProctoredCoupons();
  }

  @Test
  @DisplayName("Assign coupon to candidate fails when user is not logged in")
  void assignCouponToCandidateFailsWhenUserNotLoggedIn() throws Exception {
    given(authService.getLoggedInUser()).willReturn(Optional.empty());

    mockMvc.perform(post(BASE_PATH + ASSIGN_COUPON_PATH, CANDIDATE_ID)
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code", is("invalid_session")))
        .andExpect(jsonPath("$.message", is("Not logged in")));

    verify(authService).getLoggedInUser();
    verify(couponService, never()).assignCouponToCandidate(anyLong(), any());
  }

  @Test
  @DisplayName("Assign coupons to list fails when user is not logged in")
  void assignCouponsToListFailsWhenUserNotLoggedIn() throws Exception {
    Long testListId = 1L;

    given(authService.getLoggedInUser()).willReturn(Optional.empty());

    mockMvc.perform(post(BASE_PATH + ASSIGN_TO_LIST_PATH)
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testListId)))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code", is("invalid_session")))
        .andExpect(jsonPath("$.message", is("Not logged in")));

    verify(authService).getLoggedInUser();
    verify(couponService, never()).assignCouponsToList(anyLong(), any());
  }
}
