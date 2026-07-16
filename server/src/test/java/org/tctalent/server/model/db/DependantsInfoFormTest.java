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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class DependantsInfoFormTest {

  private static final String DEPENDANTS_INFO = "TTH_IT$DEPENDANTS_INFO";
  private static final String NO_ELIGIBLE = "NO_ELIGIBLE_FAMILY_MEMBERS";
  private static final String NO_ELIGIBLE_NOTES = "NO_ELIGIBLE_NOTES";

  @Mock
  private AuthService authService;

  @Mock
  private CandidateService candidateService;

  @Mock
  private CandidatePropertyService propertyService;

  private Candidate candidate;
  private DependantsInfoForm form;

  @BeforeEach
  void setUp() {
    candidate = new Candidate();

    form = new DependantsInfoForm(
        "IgnoredFormName",
        authService,
        candidateService,
        propertyService
    );
    form.setCandidate(candidate);
  }

  @Test
  void getFormNameReturnsConstantDependantsInfoForm() {
    assertEquals("DependantsInfoForm", form.getFormName());
  }

  @Test
  void getDependantsInfoJsonReadsCandidateProperty() {
    String json = """
        [{"name":"Ali","relationship":"child"}]
        """;

    candidate.setCandidateProperties(Map.of(
        DEPENDANTS_INFO,
        property(json)
    ));

    assertEquals(json, form.getDependantsInfoJson());
  }

  @Test
  void setDependantsInfoJsonDelegatesToPropertyService() {
    String json = """
        [{"name":"Ali","relationship":"child"}]
        """;

    form.setDependantsInfoJson(json);

    verify(propertyService).createOrUpdateProperty(
        candidate,
        DEPENDANTS_INFO,
        json,
        null
    );
  }

  @Test
  void getNoEligibleDependantsReturnsTrueWhenStoredValueIsTrue() {
    candidate.setCandidateProperties(Map.of(
        NO_ELIGIBLE,
        property("true")
    ));

    assertTrue(form.getNoEligibleDependants());
  }

  @Test
  void getNoEligibleDependantsReturnsFalseWhenStoredValueIsFalse() {
    candidate.setCandidateProperties(Map.of(
        NO_ELIGIBLE,
        property("false")
    ));

    assertFalse(form.getNoEligibleDependants());
  }

  @Test
  void getNoEligibleDependantsReturnsNullWhenPropertyIsMissing() {
    candidate.setCandidateProperties(Map.of());

    assertNull(form.getNoEligibleDependants());
  }

  @Test
  void setNoEligibleDependantsStoresBooleanStringAndNull() {
    form.setNoEligibleDependants(Boolean.TRUE);
    form.setNoEligibleDependants(Boolean.FALSE);
    form.setNoEligibleDependants(null);

    verify(propertyService).createOrUpdateProperty(
        candidate,
        NO_ELIGIBLE,
        "true",
        null
    );
    verify(propertyService).createOrUpdateProperty(
        candidate,
        NO_ELIGIBLE,
        "false",
        null
    );
    verify(propertyService).createOrUpdateProperty(
        candidate,
        NO_ELIGIBLE,
        null,
        null
    );
  }

  @Test
  void getNoEligibleNotesReadsCandidateProperty() {
    candidate.setCandidateProperties(Map.of(
        NO_ELIGIBLE_NOTES,
        property("No eligible family members at this time")
    ));

    assertEquals(
        "No eligible family members at this time",
        form.getNoEligibleNotes()
    );
  }

  @Test
  void setNoEligibleNotesDelegatesToPropertyService() {
    form.setNoEligibleNotes("No eligible family members at this time");

    verify(propertyService).createOrUpdateProperty(
        candidate,
        NO_ELIGIBLE_NOTES,
        "No eligible family members at this time",
        null
    );
  }

  private CandidateProperty property(String value) {
    CandidateProperty property = new CandidateProperty();
    property.setValue(value);
    return property;
  }
}