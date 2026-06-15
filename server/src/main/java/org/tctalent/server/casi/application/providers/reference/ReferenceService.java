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

package org.tctalent.server.casi.application.providers.reference;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.importers.FileInventoryImporter;
import org.tctalent.server.casi.core.services.AbstractCandidateAssistanceService;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.model.db.TermsType;
import org.tctalent.server.service.db.SavedListService;

/**
 * Minimal reference implementation for a CASI provider.
 *
 * @author sadatmalik
 */
@Service
public class ReferenceService extends AbstractCandidateAssistanceService {

  private final FileInventoryImporter referenceImporter;
  private final ResourceAllocator referenceAllocator;

  public ReferenceService(ServiceAssignmentRepository assignmentRepo,
      ServiceResourceRepository resourceRepo,
      AssignmentEngine assignmentEngine,
      SavedListService savedListService,
      @Qualifier("referenceVoucherImporter") FileInventoryImporter referenceVoucherImporter,
      @Qualifier("referenceVoucherAllocator") ResourceAllocator referenceVoucherAllocator) {
    super(assignmentRepo, resourceRepo, assignmentEngine, savedListService);
    this.referenceImporter = referenceVoucherImporter;
    this.referenceAllocator = referenceVoucherAllocator;
  }

  @Override
  public ServiceProvider provider() {
    return ServiceProvider.REFERENCE;
  }

  @Override
  public ServiceCode serviceCode() {
    return ServiceCode.VOUCHER;
  }

  @Override
  public Optional<TermsType> agreementTermsType() {
    return Optional.of(TermsType.REFERENCE_SERVICE_TERMS);
  }

  @Override
  public Optional<String> opcDpaAcceptedTermsInfoId() {
    return Optional.of("OpcDataProcessingAgreementV1");
  }

  @Override
  protected ResourceAllocator allocator() {
    return referenceAllocator;
  }

  @Override
  protected FileInventoryImporter importer() {
    return referenceImporter;
  }
}
