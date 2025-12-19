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

package org.tctalent.server.integration.api.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateSavedListRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.security.JwtTokenProvider;
import org.tctalent.server.security.TcUserDetails;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PrivacyPolicyAgreementIntegrationTest extends BaseDBIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CandidateRepository candidateRepository;
  @Autowired private SavedListRepository savedListRepository;
  @Autowired private CandidateSavedListRepository candidateSavedListRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private JwtTokenProvider jwtTokenProvider;

  private Candidate candidate;
  private SavedList pendingTermsList;
  private String jwtToken;
  private User user;
  private final String privacyPolicyId = "policy123";

  @BeforeEach
  void setUp() {
    user = TestDataFactory.createUser(null);
    user.setUsername("user_" + UUID.randomUUID());
    user.setEmail("user_" + UUID.randomUUID() + "@example.com");
    user = userRepository.saveAndFlush(user);

    User systemUser = TestDataFactory.createUser(null);
    systemUser.setUsername("system_" + UUID.randomUUID());
    systemUser.setEmail("system_" + UUID.randomUUID() + "@example.com");
    systemUser = userRepository.saveAndFlush(systemUser);

    candidate = TestDataFactory.createCandidate();
    candidate.setUser(user);
    candidate.setCreatedBy(systemUser);
    candidate.setContactConsentRegistration(true);
    candidate.setTaskAssignments(Collections.emptyList());
    user.setCandidate(candidate);

    candidate = candidateRepository.saveAndFlush(candidate);
    userRepository.saveAndFlush(user);

    List<SavedList> existing = savedListRepository.findAll().stream()
        .filter(list -> "PendingTermsAcceptance".equals(list.getName()))
        .toList();

    pendingTermsList = existing.isEmpty()
        ? savedListRepository.saveAndFlush(createPendingTermsList(systemUser))
        : existing.get(0);

    CandidateSavedList candidateSavedList = TestDataFactory.createCandidateSavedList(candidate, pendingTermsList);
    candidateSavedListRepository.saveAndFlush(candidateSavedList);
  }

  private SavedList createPendingTermsList(User createdBy) {
    SavedList list = new SavedList();
    list.setName("PendingTermsAcceptance");
    list.setCreatedBy(createdBy);
    list.setStatus(Status.active);
    return list;
  }

  private void authenticateUser(User user) {
    TcUserDetails userDetails = new TcUserDetails(user);
    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    jwtToken = jwtTokenProvider.generateToken(authentication);
  }

  @Test
  void testUpdateAcceptedPrivacyPolicy_Success() throws Exception {
    authenticateUser(user);

    mockMvc.perform(put("/api/portal/candidate/privacy/" + privacyPolicyId)
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists());

    Candidate updated = candidateRepository.findById(candidate.getId()).orElseThrow();
    assertEquals(privacyPolicyId, updated.getAcceptedPrivacyPolicyId());
    assertNotNull(updated.getAcceptedPrivacyPolicyDate());
    assertTrue(updated.getAcceptedPrivacyPolicyDate().isBefore(OffsetDateTime.now().plusSeconds(2)));

    boolean stillPending = candidateSavedListRepository.findAll().stream()
        .anyMatch(csl ->
            csl.getCandidate().getId().equals(candidate.getId()) &&
                csl.getSavedList().getId().equals(pendingTermsList.getId())
        );

    assertFalse(stillPending, "Pending terms tag should be removed");
  }

  @Test
  void testUpdateAcceptedPrivacyPolicy_Unauthenticated() throws Exception {
    SecurityContextHolder.clearContext();

    mockMvc.perform(put("/api/portal/candidate/privacy/" + privacyPolicyId))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testUpdateAcceptedPrivacyPolicy_NullPolicyId() throws Exception {
    authenticateUser(user);

    mockMvc.perform(put("/api/portal/candidate/privacy/null")
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isBadRequest());

    Candidate unchanged = candidateRepository.findById(candidate.getId()).orElseThrow();
    assertNull(unchanged.getAcceptedPrivacyPolicyId());
    assertNull(unchanged.getAcceptedPrivacyPolicyDate());
  }

  @Test
  void testAcceptDifferentPolicyUpdatesDateAndId() throws Exception {
    authenticateUser(user);

    String firstPolicyId = "policy123";
    String secondPolicyId = "policy456";

    // Accept first policy
    mockMvc.perform(put("/api/portal/candidate/privacy/" + firstPolicyId)
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk());

    Candidate afterFirst = candidateRepository.findById(candidate.getId()).orElseThrow();
    OffsetDateTime firstDate = afterFirst.getAcceptedPrivacyPolicyDate();

    Thread.sleep(1000); // wait 1 sec

    // Accept second (different) policy
    mockMvc.perform(put("/api/portal/candidate/privacy/" + secondPolicyId)
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk());

    Candidate afterSecond = candidateRepository.findById(candidate.getId()).orElseThrow();
    OffsetDateTime secondDate = afterSecond.getAcceptedPrivacyPolicyDate();

    assertEquals(secondPolicyId, afterSecond.getAcceptedPrivacyPolicyId());
    assertTrue(secondDate.isAfter(firstDate), "Acceptance date should update on new policy");
  }

  @Test
  void testBlankPolicyIdReturnsBadRequest() throws Exception {
    authenticateUser(user);
    mockMvc.perform(put("/api/portal/candidate/privacy/")
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().is4xxClientError());
  }
}
