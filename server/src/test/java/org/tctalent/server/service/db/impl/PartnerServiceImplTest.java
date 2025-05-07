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

package org.tctalent.server.service.db.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.api.admin.AdminApiTestUtil;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.repository.db.PartnerRepository;
import org.tctalent.server.request.partner.UpdatePartnerRequest;

@ExtendWith(MockitoExtension.class)
class PartnerServiceImplTest {
  UpdatePartnerRequest updateRequest;

  @Captor ArgumentCaptor<PartnerImpl> partnerCaptor;

  @Mock PartnerRepository partnerRepository;

  @Spy PartnerImpl partner;
  @Spy PartnerImpl partner2;

  @Spy
  @InjectMocks
  PartnerServiceImpl partnerService;

  @BeforeEach
  void setUp() {
    partner = AdminApiTestUtil.getPartner();
    partner2.setName("TC Partner 2");
    partner2.setRedirectPartner(partner);
    updateRequest = new UpdatePartnerRequest();
    updateRequest.setName("TC Partner"); // Evades NullPointerException
  }

  @Test
  @DisplayName("update sets redirect partner - unchanged if target redirectPartner is null already")
  void updateSetsRedirectPartner() {
    partner2.setRedirectPartner(null);
    updateRequest.setRedirectPartnerId(1L);

    doReturn(partner).when(partnerService).getPartner(99L); // Partner being updated
    doReturn(partner2).when(partnerService).getPartner(1L); // Target redirectPartner

    partnerService.update(99L, updateRequest); // Act

    verify(partnerRepository).save(partnerCaptor.capture());
    PartnerImpl updatedPartner = partnerCaptor.getValue();
    Assertions.assertEquals(partner2, updatedPartner.getRedirectPartner());
  }

  // This test is specifically to safeguard against scenario where the target redirectPartner itself
  // has a redirectPartner, which could cause a recursive loop.
  @Test
  @DisplayName("update sets redirect partner - with its redirectPartner set to null if needed")
  void updateSetsRedirectPartnerWithItsRedirectPartnerSetToNull() {
    updateRequest.setRedirectPartnerId(1L);

    doReturn(partner).when(partnerService).getPartner(99L); // Partner being updated
    doReturn(partner2).when(partnerService).getPartner(1L); // Target redirectPartner
    doReturn(partner2).when(partnerRepository).save(partner2);

    partnerService.update(99L, updateRequest); // Act

    verify(partner2).setRedirectPartner(null); // Target partner redirectPartner set to null

    // partnerRepository.save() called twice and updated partner has correct redirectPartner set:
    verify(partnerRepository, times(2)).save(partnerCaptor.capture());
    List<PartnerImpl> savedPartners = partnerCaptor.getAllValues();
    PartnerImpl secondSavedPartner = savedPartners.get(1);
    Assertions.assertEquals(partner2, secondSavedPartner.getRedirectPartner());
  }

}
