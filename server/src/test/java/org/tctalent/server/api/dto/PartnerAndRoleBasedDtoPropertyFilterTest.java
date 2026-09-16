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
import static org.tctalent.server.data.PartnerImplTestData.getSourcePartner;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Role;
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
}
