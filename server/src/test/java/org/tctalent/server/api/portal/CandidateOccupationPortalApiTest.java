package org.tctalent.server.api.portal;/*
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.request.candidate.occupation.CreateCandidateOccupationRequest;
import org.tctalent.server.request.candidate.occupation.UpdateCandidateOccupationsRequest;
import org.tctalent.server.service.db.CandidateOccupationService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

class CandidateOccupationPortalApiTest {

  @Mock
  private CandidateOccupationService candidateOccupationService;

  @Mock
  private OccupationService occupationService;

  @InjectMocks
  private CandidateOccupationPortalApi candidateOccupationPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(occupationService.selectBuilder()).thenReturn(new DtoBuilder()
        .add("id")
        .add("name"));
  }

  @Test
  void testListMyOccupations_Success() {
    List<CandidateOccupation> candidateOccupations = List.of(createSampleCandidateOccupation());
    when(candidateOccupationService.listMyOccupations()).thenReturn(candidateOccupations);

    List<Map<String, Object>> result = candidateOccupationPortalApi.listMyOccupations();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> occupationDto = result.get(0);
    assertEquals(1L, occupationDto.get("id"));
    assertEquals(5L, occupationDto.get("yearsExperience"));
    Map<?, ?> occupation = (Map<?, ?>) occupationDto.get("occupation");
    assertEquals(101L, occupation.get("id"));
    assertEquals("Software Developer", occupation.get("name"));
    verify(candidateOccupationService).listMyOccupations();
  }

  @Test
  void testCreateCandidateOccupation_Success() {
    CreateCandidateOccupationRequest request = new CreateCandidateOccupationRequest();
    CandidateOccupation candidateOccupation = createSampleCandidateOccupation();
    when(candidateOccupationService.createCandidateOccupation(
        any(CreateCandidateOccupationRequest.class)))
        .thenReturn(candidateOccupation);

    Map<String, Object> result = candidateOccupationPortalApi.createCandidateOccupation(request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals(5L, result.get("yearsExperience"));
    Map<?, ?> occupation = (Map<?, ?>) result.get("occupation");
    assertEquals(101L, occupation.get("id"));
    assertEquals("Software Developer", occupation.get("name"));
    verify(candidateOccupationService).createCandidateOccupation(request);
  }

  @Test
  void testCreateUpdateCandidateOccupation_Success() {
    UpdateCandidateOccupationsRequest request = new UpdateCandidateOccupationsRequest();
    List<CandidateOccupation> candidateOccupations = List.of(createSampleCandidateOccupation());
    when(candidateOccupationService.updateCandidateOccupations(
        any(UpdateCandidateOccupationsRequest.class)))
        .thenReturn(candidateOccupations);

    List<Map<String, Object>> result = candidateOccupationPortalApi.createUpdateCandidateOccupation(
        request);

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> occupationDto = result.get(0);
    assertEquals(1L, occupationDto.get("id"));
    assertEquals(5L, occupationDto.get("yearsExperience"));
    Map<?, ?> occupation = (Map<?, ?>) occupationDto.get("occupation");
    assertEquals(101L, occupation.get("id"));
    assertEquals("Software Developer", occupation.get("name"));
    verify(candidateOccupationService).updateCandidateOccupations(request);
  }

  @Test
  void testDeleteCandidateOccupation_Success() {
    Long id = 1L;
    doNothing().when(candidateOccupationService).deleteCandidateOccupation(id);

    ResponseEntity result = candidateOccupationPortalApi.deleteCandidateOccupation(id);

    assertNotNull(result);
    assertEquals(200, result.getStatusCodeValue());
    verify(candidateOccupationService).deleteCandidateOccupation(id);
  }

  @Test
  void testDeleteCandidateOccupation_NoSuchObjectException() {
    Long id = 1L;
    doThrow(new NoSuchObjectException("No such candidate occupation")).when(
        candidateOccupationService).deleteCandidateOccupation(id);

    NoSuchObjectException exception = assertThrows(
        NoSuchObjectException.class,
        () -> candidateOccupationPortalApi.deleteCandidateOccupation(id)
    );

    assertEquals("No such candidate occupation", exception.getMessage());
    verify(candidateOccupationService).deleteCandidateOccupation(id);
  }

  private CandidateOccupation createSampleCandidateOccupation() {
    CandidateOccupation candidateOccupation = new CandidateOccupation();
    candidateOccupation.setId(1L);
    Occupation occupation = new Occupation();
    occupation.setId(101L);
    occupation.setName("Software Developer");
    candidateOccupation.setOccupation(occupation);
    candidateOccupation.setYearsExperience(5L);
    return candidateOccupation;
  }
}