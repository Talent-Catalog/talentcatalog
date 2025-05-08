/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tctalent.server.api.admin.AdminApiTestUtil;
import org.tctalent.server.configuration.properties.PresetProperties;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.preset.PresetGuestTokenRequest;
import org.tctalent.server.request.preset.PresetGuestTokenRequest.PresetResource;
import org.tctalent.server.response.preset.PresetGuestTokenResponse;
import org.tctalent.server.response.preset.PresetJwtTokenResponse;
import org.tctalent.server.security.AuthService;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PresetApiServiceImplTest {

  private static final String DASHBOARD_ID = "dashboard123";
  private static final String JWT_TOKEN = "test-jwt-token";
  private static final String API_TOKEN = "api-token";
  private static final String API_SECRET = "api-secret";

  @Mock private PresetProperties properties;
  @Mock private AuthService authService;
  @Mock private WebClient webClient;
  @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;
  @Mock private WebClient.RequestBodySpec requestBodySpec;
  @Mock private WebClient.ResponseSpec responseSpec;
  @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;

  @InjectMocks
  @Spy
  private PresetApiServiceImpl presetApiService;

  @Captor
  private ArgumentCaptor<PresetGuestTokenRequest> guestTokenRequestCaptor;

  private User mockUser;
  private PresetGuestTokenResponse guestTokenResponse;
  private PresetJwtTokenResponse jwtTokenResponse;

  @BeforeEach
  void setUp() {
    mockUser = AdminApiTestUtil.getFullUser();

    PresetGuestTokenResponse.Payload guestTokenPayload = new PresetGuestTokenResponse.Payload();
    guestTokenPayload.setGuestToken("guest-token-123");
    guestTokenResponse = new PresetGuestTokenResponse();
    guestTokenResponse.setPayload(guestTokenPayload);

    PresetJwtTokenResponse.Payload jwtPayload = new PresetJwtTokenResponse.Payload();
    jwtPayload.setJwtToken(JWT_TOKEN);
    jwtTokenResponse = new PresetJwtTokenResponse();
    jwtTokenResponse.setPayload(jwtPayload);

    given(properties.getApiToken()).willReturn(API_TOKEN);
    given(properties.getApiSecret()).willReturn(API_SECRET);
//    given(properties.getWorkspaceId()).willReturn(WORKSPACE_ID);

    given(webClient.post()).willReturn(requestBodyUriSpec);
    given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
    given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
  }

  @Test
  @DisplayName("should return valid response when token is valid")
  void fetchGuestToken_ShouldReturnValidResponse_WhenTokenIsValid() {
    // Given
    given(responseSpec.bodyToMono(PresetJwtTokenResponse.class))
        .willReturn(Mono.just(jwtTokenResponse));
    given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));
    given(responseSpec.bodyToMono(PresetGuestTokenResponse.class))
        .willReturn(Mono.just(guestTokenResponse));
    given(presetApiService.getAuthClient()).willReturn(webClient);
    given(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).willReturn(requestBodySpec);

    // When
    PresetGuestTokenResponse response = presetApiService.fetchGuestToken(DASHBOARD_ID);

    // Then
    assertNotNull(response);
    assertEquals(guestTokenResponse.getPayload().getGuestToken(), response.getPayload().getGuestToken());
    then(authService).should().getLoggedInUser();
  }

  @Test
  void fetchGuestToken_ShouldReinitializeToken_WhenUnauthorized() {

    // First call throws Unauthorized
    WebClientResponseException unauthorized =
        WebClientResponseException.create(401, "Unauthorized", null, null, null);

    given(responseSpec.bodyToMono(PresetJwtTokenResponse.class))
        .willReturn(Mono.just(jwtTokenResponse));
    given(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).willReturn(requestBodySpec);

    // First unauthorized, then success for guest token
    given(responseSpec.bodyToMono(PresetGuestTokenResponse.class))
        .willReturn(Mono.error(unauthorized))
        .willReturn(Mono.just(guestTokenResponse));

    given(presetApiService.getAuthClient()).willReturn(webClient);
    given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));

    // When
    PresetGuestTokenResponse response = presetApiService.fetchGuestToken(DASHBOARD_ID);

    // Then
    assertNotNull(response);
    assertEquals(guestTokenResponse.getPayload().getGuestToken(), response.getPayload().getGuestToken());

    // Verify token was initialized twice
    verify(responseSpec, times(2)).bodyToMono(eq(PresetGuestTokenResponse.class));
  }

  @Test
  void fetchGuestToken_ShouldThrowException_WhenUserNotLoggedIn() {
    // Given
    given(presetApiService.getAuthClient()).willReturn(webClient);
    given(responseSpec.bodyToMono(PresetJwtTokenResponse.class))
        .willReturn(Mono.just(jwtTokenResponse));
    given(authService.getLoggedInUser()).willReturn(Optional.empty());

    // When / Then
    assertThrows(InvalidSessionException.class, () -> presetApiService.fetchGuestToken(DASHBOARD_ID));
  }

  @Test
  void fetchGuestToken_ShouldCreateCorrectRequest() {
    // Given
    given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));

    // Explicit class type for each bodyToMono call
    given(responseSpec.bodyToMono(PresetJwtTokenResponse.class))
        .willReturn(Mono.just(jwtTokenResponse));

    given(responseSpec.bodyToMono(PresetGuestTokenResponse.class))
        .willReturn(Mono.just(guestTokenResponse));

    given(presetApiService.getAuthClient()).willReturn(webClient);

    given(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).willReturn(requestBodySpec);

    // When
    presetApiService.fetchGuestToken(DASHBOARD_ID);

    // Then
    // Capture and verify the request object
    then(requestBodySpec).should().bodyValue(guestTokenRequestCaptor.capture());

    PresetGuestTokenRequest capturedRequest = guestTokenRequestCaptor.getValue();
    assertNotNull(capturedRequest);

    // Verify user details
    assertEquals(mockUser.getUsername(), capturedRequest.getUser().getUsername());

    // Verify resources
    assertEquals(1, capturedRequest.getResources().size());
    PresetResource resource = capturedRequest.getResources().get(0);
    assertEquals("dashboard", resource.getType());
    assertEquals(DASHBOARD_ID, resource.getId());
  }

}
