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

package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.TravelDocType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.form.TravelInfoFormData;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.util.dto.DtoBuilder;

@ExtendWith(MockitoExtension.class)
class TravelInfoFormPortalApiTest {

  private static final long CANDIDATE_ID = 25L;

  @Mock
  private AuthService authService;

  @Mock
  private CandidateService candidateService;

  @Mock
  private CountryService countryService;

  @Mock
  private CandidatePropertyService candidatePropertyService;

  private TravelInfoFormPortalApi api;
  private Candidate candidate;
  private User user;
  private Country birthCountry;

  @BeforeEach
  void setUp() {
    api = new TravelInfoFormPortalApi(authService, candidateService, countryService,
        candidatePropertyService);

    user = new User();

    birthCountry = new Country();
    birthCountry.setId(1L);
    birthCountry.setName("Afghanistan");

    candidate = new Candidate();
    candidate.setId(CANDIDATE_ID);
    candidate.setUser(user);
    candidate.setCandidateProperties(new HashMap<>());

    when(authService.getLoggedInCandidateId()).thenReturn(CANDIDATE_ID);
    when(candidateService.getCandidate(CANDIDATE_ID)).thenReturn(candidate);
    when(countryService.selectBuilder()).thenReturn(new DtoBuilder().add("id").add("name"));
  }

  @Test
  void createOrUpdateMapsEveryRequestFieldAndReturnsDto() {
    LocalDate dateOfBirth = LocalDate.of(1995, 5, 10);
    LocalDate issueDate = LocalDate.of(2024, 1, 15);
    LocalDate expiryDate = LocalDate.of(2034, 1, 15);

    // DtoBuilder reads candidate properties after the setters execute.
    // Pre-populate them with the values represented by the request.
    addProperty("placeOfBirth", "Herat");
    addProperty("TTH_IT$TRAVEL_DOC_TYPE", "PASSPORT");
    addProperty("TTH_IT$TRAVEL_DOC_NUMBER", "P1234567");
    addProperty("TTH_IT$TRAVEL_DOC_ISSUED_BY", "Afghanistan");
    addProperty("TTH_IT$TRAVEL_DOC_ISSUE_DATE", "2024-01-15");
    addProperty("TTH_IT$TRAVEL_DOC_EXPIRY_DATE", "2034-01-15");
    addProperty("TTH_IT$TRAVEL_INFO_COMMENT", "Valid passport");

    TravelInfoFormData request = new TravelInfoFormData();
    request.setFirstName("Ehsan");
    request.setLastName("Ehrari");
    request.setBirthCountry(birthCountry);
    request.setGender(Gender.male);
    request.setDateOfBirth(dateOfBirth);
    request.setPlaceOfBirth("Herat");
    request.setTravelDocType(TravelDocType.PASSPORT);
    request.setTravelDocNumber("P1234567");
    request.setTravelDocIssuedBy("Afghanistan");
    request.setTravelDocIssueDate(issueDate);
    request.setTravelDocExpiryDate(expiryDate);
    request.setTravelInfoComment("Valid passport");

    Map<String, Object> result = api.createOrUpdate(request);

    assertAll(() -> assertEquals("Ehsan", candidate.getUser().getFirstName()),
        () -> assertEquals("Ehrari", candidate.getUser().getLastName()),
        () -> assertEquals(birthCountry, candidate.getBirthCountry()),
        () -> assertEquals(Gender.male, candidate.getGender()),
        () -> assertEquals(dateOfBirth, candidate.getDob()),
        () -> assertEquals("Ehsan", result.get("firstName")),
        () -> assertEquals("Ehrari", result.get("lastName")),
        () -> assertEquals(dateOfBirth, result.get("dateOfBirth")),
        () -> assertEquals(Gender.male, result.get("gender")),
        () -> assertEquals("Herat", result.get("placeOfBirth")),
        () -> assertEquals(TravelDocType.PASSPORT, result.get("travelDocType")),
        () -> assertEquals("P1234567", result.get("travelDocNumber")),
        () -> assertEquals("Afghanistan", result.get("travelDocIssuedBy")),
        () -> assertEquals(issueDate, result.get("travelDocIssueDate")),
        () -> assertEquals(expiryDate, result.get("travelDocExpiryDate")),
        () -> assertEquals("Valid passport", result.get("travelInfoComment")));

    verify(candidatePropertyService).createOrUpdateProperty(candidate, "placeOfBirth", "Herat",
        null);
    verify(candidatePropertyService).createOrUpdateProperty(candidate, "TTH_IT$TRAVEL_DOC_TYPE",
        "PASSPORT", null);
    verify(candidatePropertyService).createOrUpdateProperty(candidate, "TTH_IT$TRAVEL_DOC_NUMBER",
        "P1234567", null);
    verify(candidatePropertyService).createOrUpdateProperty(candidate,
        "TTH_IT$TRAVEL_DOC_ISSUED_BY", "Afghanistan", null);
    verify(candidatePropertyService).createOrUpdateProperty(candidate,
        "TTH_IT$TRAVEL_DOC_ISSUE_DATE", "2024-01-15", null);
    verify(candidatePropertyService).createOrUpdateProperty(candidate,
        "TTH_IT$TRAVEL_DOC_EXPIRY_DATE", "2034-01-15", null);
    verify(candidatePropertyService).createOrUpdateProperty(candidate, "TTH_IT$TRAVEL_INFO_COMMENT",
        "Valid passport", null);
  }

  @Test
  void getReturnsCurrentTravelInformation() {
    user.setFirstName("Ehsan");
    user.setLastName("Ehrari");

    candidate.setDob(LocalDate.of(1995, 5, 10));
    candidate.setGender(Gender.male);
    candidate.setBirthCountry(birthCountry);

    addProperty("placeOfBirth", "Herat");
    addProperty("TTH_IT$TRAVEL_DOC_TYPE", "NATIONAL_ID");
    addProperty("TTH_IT$TRAVEL_DOC_NUMBER", "ID-12345");
    addProperty("TTH_IT$TRAVEL_DOC_ISSUED_BY", "Afghanistan");
    addProperty("TTH_IT$TRAVEL_DOC_ISSUE_DATE", "2023-02-01");
    addProperty("TTH_IT$TRAVEL_DOC_EXPIRY_DATE", "2033-02-01");
    addProperty("TTH_IT$TRAVEL_INFO_COMMENT", "National identity document");

    Map<String, Object> result = api.get();

    assertAll(() -> assertEquals("Ehsan", result.get("firstName")),
        () -> assertEquals("Ehrari", result.get("lastName")),
        () -> assertEquals(LocalDate.of(1995, 5, 10), result.get("dateOfBirth")),
        () -> assertEquals(Gender.male, result.get("gender")),
        () -> assertEquals("Herat", result.get("placeOfBirth")),
        () -> assertEquals(TravelDocType.NATIONAL_ID, result.get("travelDocType")),
        () -> assertEquals("ID-12345", result.get("travelDocNumber")),
        () -> assertEquals("Afghanistan", result.get("travelDocIssuedBy")),
        () -> assertEquals(LocalDate.of(2023, 2, 1), result.get("travelDocIssueDate")),
        () -> assertEquals(LocalDate.of(2033, 2, 1), result.get("travelDocExpiryDate")),
        () -> assertEquals("National identity document", result.get("travelInfoComment")),
        () -> assertTrue(result.containsKey("birthCountry")));
  }

  private void addProperty(String name, String value) {
    CandidateProperty property = new CandidateProperty();
    property.setCandidate(candidate);
    property.setName(name);
    property.setValue(value);

    candidate.getCandidateProperties().put(name, property);
  }
}