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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
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
class TravelInfoFormTest {

  private static final String PLACE_OF_BIRTH_PROP = "placeOfBirth";
  private static final String DOC_TYPE_PROP = "TTH_IT$TRAVEL_DOC_TYPE";
  private static final String DOC_NUMBER_PROP = "TTH_IT$TRAVEL_DOC_NUMBER";
  private static final String DOC_ISSUED_BY_PROP = "TTH_IT$TRAVEL_DOC_ISSUED_BY";
  private static final String DOC_ISSUE_DATE_PROP = "TTH_IT$TRAVEL_DOC_ISSUE_DATE";
  private static final String DOC_EXPIRY_DATE_PROP = "TTH_IT$TRAVEL_DOC_EXPIRY_DATE";
  private static final String INFO_COMMENT_PROP = "TTH_IT$TRAVEL_INFO_COMMENT";

  @Mock
  private AuthService authService;

  @Mock
  private CandidateService candidateService;

  @Mock
  private CandidatePropertyService propertyService;

  private Candidate candidate;
  private TravelInfoForm form;

  @BeforeEach
  void setUp() {
    User user = new User();
    user.setFirstName("Old First");
    user.setLastName("Old Last");

    candidate = new Candidate();
    candidate.setUser(user);

    form = new TravelInfoForm(
        "IgnoredFormName",
        authService,
        candidateService,
        propertyService
    );
    form.setCandidate(candidate);
  }

  @Test
  void getFormNameReturnsConstantTravelInfoForm() {
    assertEquals("TravelInfoForm", form.getFormName());
  }

  @Test
  void candidateBackedFieldsReadAndWriteCandidateData() {
    LocalDate dob = LocalDate.of(1995, 4, 12);
    Country birthCountry = new Country("Afghanistan", Status.active);

    form.setFirstName("Ehsan");
    form.setLastName("Ehrari");
    form.setDateOfBirth(dob);
    form.setGender(Gender.male);
    form.setBirthCountry(birthCountry);

    assertEquals("Ehsan", form.getFirstName());
    assertEquals("Ehrari", form.getLastName());
    assertEquals(dob, form.getDateOfBirth());
    assertEquals(Gender.male, form.getGender());
    assertSame(birthCountry, form.getBirthCountry());
  }

  @Test
  void propertyBackedStringGettersReadCandidateProperties() {
    candidate.setCandidateProperties(Map.of(
        PLACE_OF_BIRTH_PROP, property("Herat"),
        DOC_NUMBER_PROP, property("P123456"),
        DOC_ISSUED_BY_PROP, property("Afghanistan Passport Office"),
        INFO_COMMENT_PROP, property("Verified by team")
    ));

    assertEquals("Herat", form.getPlaceOfBirth());
    assertEquals("P123456", form.getTravelDocNumber());
    assertEquals("Afghanistan Passport Office", form.getTravelDocIssuedBy());
    assertEquals("Verified by team", form.getTravelInfoComment());
  }

  @Test
  void propertyBackedStringSettersDelegateToPropertyService() {
    form.setPlaceOfBirth("Herat");
    form.setTravelDocNumber("P123456");
    form.setTravelDocIssuedBy("Afghanistan Passport Office");
    form.setTravelInfoComment("Verified by team");

    verify(propertyService).createOrUpdateProperty(
        candidate, PLACE_OF_BIRTH_PROP, "Herat", null);
    verify(propertyService).createOrUpdateProperty(
        candidate, DOC_NUMBER_PROP, "P123456", null);
    verify(propertyService).createOrUpdateProperty(
        candidate, DOC_ISSUED_BY_PROP, "Afghanistan Passport Office", null);
    verify(propertyService).createOrUpdateProperty(
        candidate, INFO_COMMENT_PROP, "Verified by team", null);
  }

  @Test
  void travelDocTypeGetterConvertsStoredEnumNameAndReturnsNullWhenMissing() {
    candidate.setCandidateProperties(Map.of(
        DOC_TYPE_PROP, property(TravelDocType.PASSPORT.name())
    ));

    assertEquals(TravelDocType.PASSPORT, form.getTravelDocType());

    candidate.setCandidateProperties(Map.of());

    assertNull(form.getTravelDocType());
  }

  @Test
  void travelDocTypeSetterStoresEnumNameAndNull() {
    form.setTravelDocType(TravelDocType.NATIONAL_ID);
    form.setTravelDocType(null);

    verify(propertyService).createOrUpdateProperty(
        candidate, DOC_TYPE_PROP, TravelDocType.NATIONAL_ID.name(), null);
    verify(propertyService).createOrUpdateProperty(
        candidate, DOC_TYPE_PROP, null, null);
  }

  @Test
  void travelDocDateGettersParseStoredDatesAndReturnNullWhenMissing() {
    candidate.setCandidateProperties(Map.of(
        DOC_ISSUE_DATE_PROP, property("2026-01-15"),
        DOC_EXPIRY_DATE_PROP, property("2031-01-15")
    ));

    assertEquals(LocalDate.of(2026, 1, 15), form.getTravelDocIssueDate());
    assertEquals(LocalDate.of(2031, 1, 15), form.getTravelDocExpiryDate());

    candidate.setCandidateProperties(Map.of());

    assertNull(form.getTravelDocIssueDate());
    assertNull(form.getTravelDocExpiryDate());
  }

  @Test
  void travelDocDateSettersStoreIsoDateStringsAndNull() {
    LocalDate issueDate = LocalDate.of(2026, 1, 15);
    LocalDate expiryDate = LocalDate.of(2031, 1, 15);

    form.setTravelDocIssueDate(issueDate);
    form.setTravelDocIssueDate(null);
    form.setTravelDocExpiryDate(expiryDate);
    form.setTravelDocExpiryDate(null);

    verify(propertyService).createOrUpdateProperty(
        candidate, DOC_ISSUE_DATE_PROP, "2026-01-15", null);
    verify(propertyService).createOrUpdateProperty(
        candidate, DOC_ISSUE_DATE_PROP, null, null);
    verify(propertyService).createOrUpdateProperty(
        candidate, DOC_EXPIRY_DATE_PROP, "2031-01-15", null);
    verify(propertyService).createOrUpdateProperty(
        candidate, DOC_EXPIRY_DATE_PROP, null, null);
  }

  private CandidateProperty property(String value) {
    CandidateProperty property = new CandidateProperty();
    property.setValue(value);
    return property;
  }
}