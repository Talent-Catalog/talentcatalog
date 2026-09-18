/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.api.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.data.PartnerImplTestData.getSourcePartner;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.repository.db.read.dto.PartnerReadDto;
import org.tctalent.server.repository.db.read.dto.UserReadDto;

class PartnerAndRoleBasedDtoPropertyFilterTest {

  @Test
  @DisplayName("Source partner admin can view private fields for its CandidateReadDto")
  void sourcePartnerAdmin_doesNotIgnorePhone_onOwnPartnerCandidateReadDto() {
    PartnerImpl viewerPartner = getSourcePartner();

    PartnerReadDto candidatePartner = new PartnerReadDto();
    candidatePartner.setId(viewerPartner.getId());
    UserReadDto candidateUser = UserReadDto.builder()
        .partner(candidatePartner)
        .build();
    CandidateReadDto candidate = CandidateReadDto.builder()
        .id(99L)
        .user(candidateUser)
        .build();

    PartnerAndRoleBasedDtoPropertyFilter filter =
        new PartnerAndRoleBasedDtoPropertyFilter(
            viewerPartner, Role.admin, Set.of(), Set.of(), Set.of("id"), Set.of());

    assertFalse(filter.ignoreProperty(candidate, "phone"));
  }

  @Test
  @DisplayName("Source partner admin can view private fields for its UserReadDto")
  void sourcePartnerAdmin_doesNotIgnoreEmail_onOwnPartnerUserReadDto() {
    PartnerImpl viewerPartner = getSourcePartner();

    PartnerReadDto candidatePartner = new PartnerReadDto();
    candidatePartner.setId(viewerPartner.getId());
    UserReadDto candidateUser = UserReadDto.builder()
        .partner(candidatePartner)
        .build();

    PartnerAndRoleBasedDtoPropertyFilter filter =
        new PartnerAndRoleBasedDtoPropertyFilter(
            viewerPartner, Role.admin, Set.of(), Set.of(), Set.of("id"), Set.of());

    assertFalse(filter.ignoreProperty(candidateUser, "email"));
  }

  @Test
  @DisplayName("Source partner admin can still view private fields for its Candidate entity")
  void sourcePartnerAdmin_doesNotIgnorePhone_onOwnPartnerCandidateEntity() {
    PartnerImpl viewerPartner = getSourcePartner();
    User candidateUser = new User("candidate", "Test", "Candidate",
        "candidate@example.com", Role.user);
    candidateUser.setPartner(viewerPartner);
    Candidate candidate = new Candidate(candidateUser, "123456789", "123456789", candidateUser);

    PartnerAndRoleBasedDtoPropertyFilter filter =
        new PartnerAndRoleBasedDtoPropertyFilter(
            viewerPartner, Role.admin, Set.of(), Set.of(), Set.of("id"), Set.of());

    assertFalse(filter.ignoreProperty(candidate, "phone"));
  }

  @Test
  @DisplayName("Source partner admin cannot view private fields for another partner's CandidateReadDto")
  void sourcePartnerAdmin_ignoresPhone_onOtherPartnerCandidateReadDto() {
    PartnerImpl viewerPartner = getSourcePartner();

    PartnerReadDto candidatePartner = new PartnerReadDto();
    candidatePartner.setId(456L);
    UserReadDto candidateUser = UserReadDto.builder()
        .partner(candidatePartner)
        .build();
    CandidateReadDto candidate = CandidateReadDto.builder()
        .id(99L)
        .user(candidateUser)
        .build();

    PartnerAndRoleBasedDtoPropertyFilter filter =
        new PartnerAndRoleBasedDtoPropertyFilter(
            viewerPartner, Role.admin, Set.of(), Set.of(), Set.of("id"), Set.of());

    assertTrue(filter.ignoreProperty(candidate, "phone"));
  }

  @Test
  @DisplayName("Job creator admin can view private fields for a fully visible CandidateReadDto")
  void jobCreatorAdmin_doesNotIgnorePhone_onFullyVisibleCandidateReadDto() {
    PartnerImpl viewerPartner = new PartnerImpl();
    viewerPartner.setId(456L);
    viewerPartner.setJobCreator(true);
    CandidateReadDto candidate = CandidateReadDto.builder()
        .id(99L)
        .build();

    PartnerAndRoleBasedDtoPropertyFilter filter =
        new PartnerAndRoleBasedDtoPropertyFilter(
            viewerPartner, Role.admin, Set.of(99L), Set.of(), Set.of("id"), Set.of());

    assertFalse(filter.ignoreProperty(candidate, "phone"));
  }

  @Test
  @DisplayName("Limited user cannot view private fields for its partner's CandidateReadDto")
  void limitedUser_ignoresPhone_onOwnPartnerCandidateReadDto() {
    PartnerImpl viewerPartner = getSourcePartner();

    PartnerReadDto candidatePartner = new PartnerReadDto();
    candidatePartner.setId(viewerPartner.getId());
    UserReadDto candidateUser = UserReadDto.builder()
        .partner(candidatePartner)
        .build();
    CandidateReadDto candidate = CandidateReadDto.builder()
        .id(99L)
        .user(candidateUser)
        .build();

    PartnerAndRoleBasedDtoPropertyFilter filter =
        new PartnerAndRoleBasedDtoPropertyFilter(
            viewerPartner, Role.limited, Set.of(), Set.of(), Set.of("id"), Set.of());

    assertTrue(filter.ignoreProperty(candidate, "phone"));
  }
}
