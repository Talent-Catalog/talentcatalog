/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.CandidateService;

@ExtendWith(MockitoExtension.class)
class RefugeeStatusInfoFormTest {

  private static final String REFUGEE_STATUS_PROPERTY = "TTH_IT$REFUGEE_STATUS";
  private static final String REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE_PROPERTY =
      "TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE";
  private static final String REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER_PROPERTY =
      "TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER";
  private static final String REFUGEE_STATUS_COMMENT_PROPERTY = "TTH_IT$REFUGEE_STATUS_COMMENT";

  @Mock
  private AuthService authService;

  @Mock
  private CandidateService candidateService;

  @Mock
  private CandidatePropertyService propertyService;

  private Candidate candidate;
  private RefugeeStatusInfoForm form;

  @BeforeEach
  void setUp() {
    candidate = new Candidate();

    form = new RefugeeStatusInfoForm(
        "IgnoredFormName",
        authService,
        candidateService,
        propertyService
    );
    form.setCandidate(candidate);
  }

  @Test
  void getFormNameReturnsConstantRefugeeStatusInfoForm() {
    assertEquals("RefugeeStatusInfoForm", form.getFormName());
  }

  @Test
  void getRefugeeStatusConvertsStoredEnumNameAndReturnsNullWhenMissing() {
    candidate.setCandidateProperties(Map.of(
        REFUGEE_STATUS_PROPERTY,
        property(RsdRefugeeStatus.RECOGNIZED_BY_UNHCR.name())
    ));

    assertEquals(RsdRefugeeStatus.RECOGNIZED_BY_UNHCR, form.getRefugeeStatus());

    candidate.setCandidateProperties(Map.of());

    assertNull(form.getRefugeeStatus());
  }

  @Test
  void setRefugeeStatusStoresEnumNameAndNull() {
    form.setRefugeeStatus(RsdRefugeeStatus.RECOGNIZED_BY_HOST_COUNTRY);
    form.setRefugeeStatus(null);

    verify(propertyService).createOrUpdateProperty(
        candidate,
        REFUGEE_STATUS_PROPERTY,
        RsdRefugeeStatus.RECOGNIZED_BY_HOST_COUNTRY.name(),
        null
    );
    verify(propertyService).createOrUpdateProperty(
        candidate,
        REFUGEE_STATUS_PROPERTY,
        null,
        null
    );
  }

  @Test
  void getDocumentTypeConvertsStoredEnumNameAndReturnsNullWhenMissing() {
    candidate.setCandidateProperties(Map.of(
        REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE_PROPERTY,
        property(RefugeeStatusEvidenceDocumentType.UNHCR_CERTIFICATE.name())
    ));

    assertEquals(RefugeeStatusEvidenceDocumentType.UNHCR_CERTIFICATE, form.getDocumentType());

    candidate.setCandidateProperties(Map.of());

    assertNull(form.getDocumentType());
  }

  @Test
  void setDocumentTypeStoresEnumNameAndNull() {
    form.setDocumentType(RefugeeStatusEvidenceDocumentType.HOST_COUNTRY_ID);
    form.setDocumentType(null);

    verify(propertyService).createOrUpdateProperty(
        candidate,
        REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE_PROPERTY,
        RefugeeStatusEvidenceDocumentType.HOST_COUNTRY_ID.name(),
        null
    );
    verify(propertyService).createOrUpdateProperty(
        candidate,
        REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE_PROPERTY,
        null,
        null
    );
  }

  @Test
  void getRefugeeStatusCommentReadsCandidateProperty() {
    candidate.setCandidateProperties(Map.of(
        REFUGEE_STATUS_COMMENT_PROPERTY,
        property("Recognized by UNHCR and document verified")
    ));

    assertEquals(
        "Recognized by UNHCR and document verified",
        form.getRefugeeStatusComment()
    );
  }

  @Test
  void setRefugeeStatusCommentDelegatesToPropertyService() {
    form.setRefugeeStatusComment("Recognized by UNHCR and document verified");

    verify(propertyService).createOrUpdateProperty(
        candidate,
        REFUGEE_STATUS_COMMENT_PROPERTY,
        "Recognized by UNHCR and document verified",
        null
    );
  }

  @Test
  void getDocumentNumberReadsCandidateProperty() {
    candidate.setCandidateProperties(Map.of(
        REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER_PROPERTY,
        property("UNHCR-123456")
    ));

    assertEquals("UNHCR-123456", form.getDocumentNumber());
  }

  @Test
  void setDocumentNumberDelegatesToPropertyService() {
    form.setDocumentNumber("UNHCR-123456");

    verify(propertyService).createOrUpdateProperty(
        candidate,
        REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER_PROPERTY,
        "UNHCR-123456",
        null
    );
  }

  private CandidateProperty property(String value) {
    CandidateProperty property = new CandidateProperty();
    property.setValue(value);
    return property;
  }
}