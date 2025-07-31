package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tctalent.server.response.DuolingoDashboardResponse;
import org.tctalent.server.response.DuolingoDashboardWrapper;
import org.tctalent.server.response.DuolingoVerifyScoreResponse;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class DuolingoApiServiceImplTest {

  @Mock
  private WebClient webClient;

  @Mock
  private WebClient.RequestHeadersUriSpec<WebClient.RequestBodySpec> requestHeadersUriSpec;

  @Mock
  private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

  @Mock
  private WebClient.ResponseSpec responseSpec;

  private DuolingoApiServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new DuolingoApiServiceImpl(webClient);
  }

  @Test
  void testGetDashboardResultsNoDates() {
    DuolingoDashboardWrapper wrapper = new DuolingoDashboardWrapper();
    List<DuolingoDashboardResponse> expectedExams = List.of(new DuolingoDashboardResponse());
    wrapper.setExams(expectedExams);

    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri("/get_dashboard_results");
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    doReturn(Mono.just(wrapper)).when(responseSpec).bodyToMono(DuolingoDashboardWrapper.class);

    List<DuolingoDashboardResponse> result = service.getDashboardResults(null, null);

    assertNotNull(result);
    assertEquals(expectedExams, result);
  }

  @Test
  void testGetDashboardResultsWithMinDateOnly() {
    LocalDateTime minDate = LocalDateTime.of(2023, 1, 1, 0, 0);
    DuolingoDashboardWrapper wrapper = new DuolingoDashboardWrapper();
    List<DuolingoDashboardResponse> expectedExams = List.of(new DuolingoDashboardResponse());
    wrapper.setExams(expectedExams);

    ArgumentCaptor<Function> uriCaptor = ArgumentCaptor.forClass(Function.class);
    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(uriCaptor.capture());
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    doReturn(Mono.just(wrapper)).when(responseSpec).bodyToMono(DuolingoDashboardWrapper.class);

    List<DuolingoDashboardResponse> result = service.getDashboardResults(minDate, null);

    assertNotNull(result);
    assertEquals(expectedExams, result);
    verify(requestHeadersUriSpec).uri(any(Function.class));
  }

  @Test
  void testGetDashboardResultsWithBothDates() {
    LocalDateTime minDate = LocalDateTime.of(2023, 1, 1, 0, 0);
    LocalDateTime maxDate = LocalDateTime.of(2023, 12, 31, 23, 59);
    DuolingoDashboardWrapper wrapper = new DuolingoDashboardWrapper();
    List<DuolingoDashboardResponse> expectedExams = List.of(new DuolingoDashboardResponse());
    wrapper.setExams(expectedExams);

    ArgumentCaptor<Function> uriCaptor = ArgumentCaptor.forClass(Function.class);
    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(uriCaptor.capture());
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    doReturn(Mono.just(wrapper)).when(responseSpec).bodyToMono(DuolingoDashboardWrapper.class);

    List<DuolingoDashboardResponse> result = service.getDashboardResults(minDate, maxDate);

    assertNotNull(result);
    assertEquals(expectedExams, result);
    verify(requestHeadersUriSpec).uri(any(Function.class));
  }

  @Test
  void testGetDashboardResultsThrowsWebClientException() {
    LocalDateTime minDate = LocalDateTime.of(2023, 1, 1, 0, 0);
    LocalDateTime maxDate = LocalDateTime.of(2023, 12, 31, 23, 59);
    WebClientResponseException exception = mock(WebClientResponseException.class);

    ArgumentCaptor<Function> uriCaptor = ArgumentCaptor.forClass(Function.class);
    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(uriCaptor.capture());
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    doReturn(Mono.error(exception)).when(responseSpec).bodyToMono(DuolingoDashboardWrapper.class);

    // Act & Assert
    assertThrows(WebClientResponseException.class, () -> service.getDashboardResults(minDate, maxDate));
    verify(requestHeadersUriSpec).uri(any(Function.class));
  }

  @Test
  void testVerifyScoreSuccess() {
    // Arrange
    String certificateId = "CERT123";
    String birthdate = "1990-01-01";
    DuolingoVerifyScoreResponse expectedResponse = new DuolingoVerifyScoreResponse();

    ArgumentCaptor<Function> uriCaptor = ArgumentCaptor.forClass(Function.class);
    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(uriCaptor.capture());
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    doReturn(Mono.just(expectedResponse)).when(responseSpec).bodyToMono(DuolingoVerifyScoreResponse.class);

    DuolingoVerifyScoreResponse result = service.verifyScore(certificateId, birthdate);

    assertNotNull(result);
    assertEquals(expectedResponse, result);
    verify(requestHeadersUriSpec).uri(any(Function.class));
  }

  @Test
  void testVerifyScoreThrowsWebClientException() {
    String certificateId = "CERT123";
    String birthdate = "1990-01-01";
    WebClientResponseException exception = mock(WebClientResponseException.class);

    ArgumentCaptor<Function> uriCaptor = ArgumentCaptor.forClass(Function.class);
    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(uriCaptor.capture());
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    doReturn(Mono.error(exception)).when(responseSpec).bodyToMono(DuolingoVerifyScoreResponse.class);

    assertThrows(WebClientResponseException.class, () -> service.verifyScore(certificateId, birthdate));
    verify(requestHeadersUriSpec).uri(any(Function.class));
  }
}