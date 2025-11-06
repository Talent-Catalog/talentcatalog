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

package org.tctalent.server.model.db;

import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.CandidateService;

/**
 * Candidate form that captures refugee status determination evidence details.
 */
public class RsdEvidenceForm extends CandidateFormInstanceHelper {
  private static final String REFUGEE_STATUS_PROPERTY = "refugeeStatus";
  private static final String DOCUMENT_TYPE_PROPERTY = "documentType";
  private static final String DOCUMENT_NUMBER_PROPERTY = "documentNumber";

  public RsdEvidenceForm(String formName, AuthService authService,
      CandidateService candidateService, CandidatePropertyService propertyService) {
    super(formName, authService, candidateService, propertyService);
  }
  @Override
  public String getFormName() {
    return "RsdEvidenceForm";
  }

  public RsdRefugeeStatus getRefugeeStatus() {
    String value = getProperty(REFUGEE_STATUS_PROPERTY);
    return value != null ? RsdRefugeeStatus.valueOf(value) : null;
  }

  public void setRefugeeStatus(RsdRefugeeStatus refugeeStatus) {
    setProperty(REFUGEE_STATUS_PROPERTY, refugeeStatus != null ? refugeeStatus.name() : null);
  }

  public RsdEvidenceDocumentType getDocumentType() {
    String value = getProperty(DOCUMENT_TYPE_PROPERTY);
    return value != null ? RsdEvidenceDocumentType.valueOf(value) : null;
  }

  public void setDocumentType(RsdEvidenceDocumentType documentType) {
    setProperty(DOCUMENT_TYPE_PROPERTY, documentType != null ? documentType.name() : null);
  }

  public String getDocumentNumber() {
    return getProperty(DOCUMENT_NUMBER_PROPERTY);
  }

  public void setDocumentNumber(String documentNumber) {
    setProperty(DOCUMENT_NUMBER_PROPERTY, documentNumber);
  }
}

