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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
class CandidateFormInstanceHelperTest {

  private static final String FORM_NAME = "TestForm";
  private static final String PROPERTY_NAME = "testProperty";
  private static final String PROPERTY_VALUE = "test value";

  @Mock
  private AuthService authService;

  @Mock
  private CandidateService candidateService;

  @Mock
  private CandidatePropertyService propertyService;

  private Candidate candidate;
  private CandidateFormInstanceHelper helper;

  @BeforeEach
  void setUp() {
    candidate = new Candidate();

    helper = new CandidateFormInstanceHelper(
        FORM_NAME,
        authService,
        candidateService,
        propertyService
    );
  }

  @Test
  void constructorStoresDependenciesAndFormName() {
    assertEquals(FORM_NAME, helper.getFormName());
    assertSame(authService, helper.getAuthService());
    assertSame(candidateService, helper.getCandidateService());
    assertSame(propertyService, helper.getPropertyService());
  }

  @Test
  void getCandidateReturnsExistingCandidateWithoutCallingServices() {
    helper.setCandidate(candidate);

    Candidate result = helper.getCandidate();

    assertSame(candidate, result);
    verifyNoInteractions(authService, candidateService);
  }

  @Test
  void getCandidateLoadsLoggedInCandidateWhenCandidateIsNotAlreadySet() {
    when(authService.getLoggedInCandidateId()).thenReturn(123L);
    when(candidateService.getCandidate(123L)).thenReturn(candidate);

    Candidate firstResult = helper.getCandidate();
    Candidate secondResult = helper.getCandidate();

    assertSame(candidate, firstResult);
    assertSame(candidate, secondResult);

    verify(authService).getLoggedInCandidateId();
    verify(candidateService).getCandidate(123L);
  }

  @Test
  void getCandidateThrowsWhenNoCandidateIsSetAndUserIsNotLoggedIn() {
    when(authService.getLoggedInCandidateId()).thenReturn(null);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> helper.getCandidate()
    );

    assertEquals("Not logged in", exception.getMessage());
    verify(candidateService, never()).getCandidate(0);
  }

  @Test
  void getPropertyReturnsValueWhenCandidatePropertyExists() {
    CandidateProperty property = property(PROPERTY_VALUE);
    candidate.setCandidateProperties(Map.of(PROPERTY_NAME, property));
    helper.setCandidate(candidate);

    assertEquals(PROPERTY_VALUE, helper.getProperty(PROPERTY_NAME));
  }

  @Test
  void getPropertyReturnsNullWhenCandidatePropertiesMapIsNull() {
    candidate.setCandidateProperties(null);
    helper.setCandidate(candidate);

    assertNull(helper.getProperty(PROPERTY_NAME));
  }

  @Test
  void getPropertyReturnsNullWhenPropertyDoesNotExist() {
    candidate.setCandidateProperties(Map.of("otherProperty", property("other value")));
    helper.setCandidate(candidate);

    assertNull(helper.getProperty(PROPERTY_NAME));
  }

  @Test
  void setPropertyDelegatesToPropertyService() {
    helper.setCandidate(candidate);

    helper.setProperty(PROPERTY_NAME, PROPERTY_VALUE);

    verify(propertyService).createOrUpdateProperty(
        candidate,
        PROPERTY_NAME,
        PROPERTY_VALUE,
        null
    );
  }

  @Test
  void saveDelegatesToCandidateService() {
    helper.setCandidate(candidate);

    helper.save();

    verify(candidateService).save(candidate, false);
  }

  private CandidateProperty property(String value) {
    CandidateProperty property = new CandidateProperty();
    property.setValue(value);
    return property;
  }
}