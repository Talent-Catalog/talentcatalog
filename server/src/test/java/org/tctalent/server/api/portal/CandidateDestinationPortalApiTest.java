package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateDestination;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.YesNoUnsure;
import org.tctalent.server.request.candidate.destination.CreateCandidateDestinationRequest;
import org.tctalent.server.request.candidate.destination.UpdateCandidateDestinationRequest;
import org.tctalent.server.service.db.CandidateDestinationService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.util.dto.DtoBuilder;

class CandidateDestinationPortalApiTest {

  @Mock
  private CandidateDestinationService candidateDestinationService;

  @Mock
  private CountryService countryService;

  @InjectMocks
  private CandidateDestinationPortalApi candidateDestinationPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(countryService.selectBuilder()).thenReturn(new DtoBuilder()
        .add("id")
        .add("name"));
  }

  @Test
  void testCreate_Success() {
    long candidateId = 1L;
    CreateCandidateDestinationRequest request = new CreateCandidateDestinationRequest();
    CandidateDestination destination = createSampleDestination();
    when(candidateDestinationService.createDestination(eq(candidateId),
        any(CreateCandidateDestinationRequest.class)))
        .thenReturn(destination);

    Map<String, Object> result = candidateDestinationPortalApi.create(candidateId, request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals(YesNoUnsure.Yes, result.get("interest"));
    assertEquals("Test notes", result.get("notes"));
    @SuppressWarnings("unchecked")
    Map<String, Object> country = (Map<String, Object>) result.get("country");
    assertEquals(1L, country.get("id"));
    assertEquals("Test Country", country.get("name"));
    verify(candidateDestinationService).createDestination(candidateId, request);
  }

  @Test
  void testCreate_NoSuchObjectException() {
    long candidateId = 1L;
    CreateCandidateDestinationRequest request = new CreateCandidateDestinationRequest();
    when(candidateDestinationService.createDestination(eq(candidateId),
        any(CreateCandidateDestinationRequest.class)))
        .thenThrow(new NoSuchObjectException("No such candidate or country"));

    NoSuchObjectException exception = assertThrows(
        NoSuchObjectException.class,
        () -> candidateDestinationPortalApi.create(candidateId, request)
    );

    assertEquals("No such candidate or country", exception.getMessage());
    verify(candidateDestinationService).createDestination(candidateId, request);
  }

  @Test
  void testUpdate_Success() {
    long id = 1L;
    UpdateCandidateDestinationRequest request = new UpdateCandidateDestinationRequest();
    CandidateDestination destination = createSampleDestination();
    when(candidateDestinationService.updateDestination(eq(id),
        any(UpdateCandidateDestinationRequest.class)))
        .thenReturn(destination);

    Map<String, Object> result = candidateDestinationPortalApi.update(id, request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals(YesNoUnsure.Yes, result.get("interest"));
    assertEquals("Test notes", result.get("notes"));
    @SuppressWarnings("unchecked")
    Map<String, Object> country = (Map<String, Object>) result.get("country");
    assertEquals(1L, country.get("id"));
    assertEquals("Test Country", country.get("name"));
    verify(candidateDestinationService).updateDestination(id, request);
  }

  @Test
  void testUpdate_EntityExistsException() {
    long id = 1L;
    UpdateCandidateDestinationRequest request = new UpdateCandidateDestinationRequest();
    when(candidateDestinationService.updateDestination(eq(id),
        any(UpdateCandidateDestinationRequest.class)))
        .thenThrow(new EntityExistsException("Destination already exists"));

    EntityExistsException exception = assertThrows(
        EntityExistsException.class,
        () -> candidateDestinationPortalApi.update(id, request)
    );

    assertTrue(exception.getMessage().contains("Destination already exists"));
    verify(candidateDestinationService).updateDestination(id, request);
  }

  @Test
  void testUpdate_InvalidRequestException() {
    long id = 1L;
    UpdateCandidateDestinationRequest request = new UpdateCandidateDestinationRequest();
    when(candidateDestinationService.updateDestination(eq(id),
        any(UpdateCandidateDestinationRequest.class)))
        .thenThrow(new InvalidRequestException("Invalid request data"));

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> candidateDestinationPortalApi.update(id, request)
    );

    assertEquals("Invalid request data", exception.getMessage());
    verify(candidateDestinationService).updateDestination(id, request);
  }

  @Test
  void testUpdate_NoSuchObjectException() {
    long id = 1L;
    UpdateCandidateDestinationRequest request = new UpdateCandidateDestinationRequest();
    when(candidateDestinationService.updateDestination(eq(id),
        any(UpdateCandidateDestinationRequest.class)))
        .thenThrow(new NoSuchObjectException("No such destination"));

    NoSuchObjectException exception = assertThrows(
        NoSuchObjectException.class,
        () -> candidateDestinationPortalApi.update(id, request)
    );

    assertEquals("No such destination", exception.getMessage());
    verify(candidateDestinationService).updateDestination(id, request);
  }

  private CandidateDestination createSampleDestination() {
    CandidateDestination destination = new CandidateDestination();
    destination.setId(1L);
    Country country = new Country();
    country.setId(1L);
    country.setName("Test Country");
    destination.setCountry(country);
    destination.setInterest(YesNoUnsure.Yes);
    destination.setNotes("Test notes");
    return destination;
  }
}