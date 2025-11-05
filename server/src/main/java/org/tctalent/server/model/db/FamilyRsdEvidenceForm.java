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
 * Candidate portal form that captures refugee status documentation for
 * relocating family members. The payload is stored as JSON in a candidate
 * property so it can flexibly represent the per-member data set and any
 * associated uploads.
 */
public class FamilyRsdEvidenceForm extends CandidateFormInstanceHelper {

  private static final String FAMILY_RSD_JSON = "FAMILY_RSD_EVIDENCE_INFO";

  public FamilyRsdEvidenceForm(String formName, AuthService authService,
      CandidateService candidateService,
      CandidatePropertyService propertyService) {
    super(formName, authService, candidateService, propertyService);
  }

  public String getFormName() {
    return "FamilyRsdEvidenceForm";
  }

  public String getFamilyRsdEvidenceJson() {
    return getProperty(FAMILY_RSD_JSON);
  }

  public void setFamilyRsdEvidenceJson(String json) {
    setProperty(FAMILY_RSD_JSON, json);
  }
}
