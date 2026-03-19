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

package org.tctalent.server.casi.application.providers.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.importers.FileInventoryImporter;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.service.db.SavedListService;

class ReferenceServiceTest {

  @Mock private ServiceAssignmentRepository assignmentRepository;
  @Mock private ServiceResourceRepository resourceRepository;
  @Mock private AssignmentEngine assignmentEngine;
  @Mock private SavedListService savedListService;
  @Mock private FileInventoryImporter importer;
  @Mock private ResourceAllocator allocator;

  private ReferenceService service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    service = new ReferenceService(
        assignmentRepository,
        resourceRepository,
        assignmentEngine,
        savedListService,
        importer,
        allocator);
  }

  @Test
  @DisplayName("provider key is REFERENCE::VOUCHER")
  void providerKeyIsReferenceVoucher() {
    assertEquals("REFERENCE::VOUCHER", service.providerKey());
  }

  @Test
  @DisplayName("import inventory delegates to importer with VOUCHER service code")
  void importInventoryDelegatesToImporter() {
    MockMultipartFile file = new MockMultipartFile("file", "reference.csv", "text/csv", "x".getBytes());
    service.importInventory(file);
    verify(importer).importFile(file, ServiceCode.VOUCHER);
  }
}
