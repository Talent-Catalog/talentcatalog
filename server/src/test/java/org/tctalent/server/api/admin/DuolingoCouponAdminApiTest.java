package org.tctalent.server.api.admin;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.response.DuolingoCouponResponse;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.DuolingoCouponService;
import org.tctalent.server.request.duolingocoupon.UpdateDuolingoCouponStatusRequest;
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

  private final DuolingoCoupon coupon = AdminApiTestUtil.getDuolingoCoupon();
  private static final SavedList savedList = AdminApiTestUtil.getSavedList();
  private static final Candidate candidate = AdminApiTestUtil.getCandidate();
  private static final TaskImpl task = AdminApiTestUtil.getTask();
  private static final TaskAssignmentImpl taskAssignment = AdminApiTestUtil.getTaskAssignment();

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
  @DisplayName("Assign coupon to candidate succeeds")
  void assignCouponToCandidateSucceeds() throws Exception {

    given(authService.getLoggedInUser()).willReturn(Optional.of(user));
    given(couponService.assignCouponToCandidate(anyLong())).willReturn(coupon);
    given(taskService.getByName(DUOLINGO_TEST_TASK_NAME)).willReturn(task);
    given(candidateService.getCandidate(anyLong())).willReturn(candidate);
    given(taskAssignmentService.assignTaskToCandidate(user, task, candidate, null, null))
        .willReturn(taskAssignment);

    mockMvc.perform(post(BASE_PATH + ASSIGN_COUPON_PATH, CANDIDATE_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.couponCode", is(COUPON_CODE)))
        .andExpect(jsonPath("$.duolingoCouponStatus", is("AVAILABLE")));

    verify(authService).getLoggedInUser();
    verify(taskAssignmentService).assignTaskToCandidate(user, task, candidate, null, null);
    verify(couponService).assignCouponToCandidate(anyLong());
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
    given(taskService.getByName(DUOLINGO_TEST_TASK_NAME)).willReturn(task);
    given(savedListService.get(anyLong())).willReturn(savedList);

    mockMvc.perform(post(BASE_PATH + ASSIGN_TO_LIST_PATH)
            .with(csrf())
            .header("Authorization", "Bearer jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(String.valueOf(testListId)))
        .andDo(print())
        .andExpect(status().isOk());

    verify(savedListService).get(testListId);
    verify(couponService).assignCouponsToList(savedList);
    verify(taskService).getByName(DUOLINGO_TEST_TASK_NAME);
    verify(savedListService).associateTaskWithList(user, task, savedList);

  }
}
