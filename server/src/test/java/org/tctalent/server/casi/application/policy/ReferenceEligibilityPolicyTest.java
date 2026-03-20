/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.casi.application.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.service.db.CandidateService;

@ExtendWith(MockitoExtension.class)
class ReferenceEligibilityPolicyTest {

  @Mock
  private CandidateService candidateService;

  private ReferenceEligibilityPolicy policy;

  @BeforeEach
  void setUp() {
    policy = new ReferenceEligibilityPolicy(candidateService);
  }

  @Test
  @DisplayName("returns REFERENCE provider")
  void providerReturnsReference() {
    assertThat(policy.provider()).isEqualTo(ServiceProvider.REFERENCE);
  }

  @Test
  @DisplayName("eligible when candidate exists and status is not inactive")
  void eligibleWhenCandidateExistsAndStatusNotInactive() throws NoSuchObjectException {
    Candidate candidate = CandidateWithStatus(CandidateStatus.active);
    when(candidateService.getCandidate(1L)).thenReturn(candidate);

    assertThat(policy.isEligible(1L)).isTrue();
  }

  @Test
  @DisplayName("not eligible when candidate has inactive status")
  void notEligibleWhenCandidateHasInactiveStatus() throws NoSuchObjectException {
    Candidate candidate = CandidateWithStatus(CandidateStatus.deleted);
    when(candidateService.getCandidate(2L)).thenReturn(candidate);

    assertThat(policy.isEligible(2L)).isFalse();
  }

  @Test
  @DisplayName("not eligible when candidate not found")
  void notEligibleWhenCandidateNotFound() throws NoSuchObjectException {
    when(candidateService.getCandidate(999L))
        .thenThrow(new NoSuchObjectException(Candidate.class, 999L));

    assertThat(policy.isEligible(999L)).isFalse();
  }

  @Test
  @DisplayName("not eligible when candidate id is null")
  void notEligibleWhenCandidateIdNull() {
    assertThat(policy.isEligible(null)).isFalse();
  }

  private static Candidate CandidateWithStatus(CandidateStatus status) {
    Candidate c = new Candidate();
    c.setStatus(status);
    return c;
  }
}
