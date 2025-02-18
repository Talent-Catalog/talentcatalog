/*
 * Copyright (c) 2025 Talent Beyond Boundaries.
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

import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tctalent.server.configuration.properties.PresetProperties;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.request.preset.PresetGuestTokenRequest;
import org.tctalent.server.request.preset.PresetGuestTokenRequest.PresetResource;
import org.tctalent.server.request.preset.PresetGuestTokenRequest.PresetUser;
import org.tctalent.server.request.preset.PresetJwtTokenRequest;
import org.tctalent.server.response.PresetGuestTokenResponse;
import org.tctalent.server.response.PresetJwtTokenResponse;
import org.tctalent.server.service.db.PresetApiService;
import reactor.util.retry.Retry;

@Slf4j
@Service
public class PresetApiServiceImpl implements PresetApiService {

  private final PresetProperties properties;
  private final WebClient authClient;
  private String jwtToken = null;

  @Autowired
  public PresetApiServiceImpl(PresetProperties properties) {
    this.properties = properties;
    this.authClient = WebClient.builder().baseUrl(properties.getAuthBaseUrl()).build();
  }

  public String fetchGuestToken(String dashboardId) throws WebClientResponseException {
    if (jwtToken == null) {
      initialiseJwtToken();
    }

    PresetGuestTokenRequest request = createPresetGuestTokenRequest(dashboardId);

    try {
      return attemptFetchGuestToken(request);
    } catch (WebClientResponseException.Unauthorized e) {
      LogBuilder.builder(log)
          .action("Fetch Preset Guest Token")
          .message("JWT token expired. Reinitializing and retrying...")
          .logWarn();
      initialiseJwtToken();
      return attemptFetchGuestToken(request); // Retry once with a new token
    } catch (Exception e) {
      LogBuilder.builder(log)
          .action("Fetch Preset Guest Token")
          .message("Failed to fetch Preset Guest Token")
          .logError(e);
      throw e;
    }
  }

  private String attemptFetchGuestToken(PresetGuestTokenRequest request) {
    return authClient.post()
        .uri(getGuestTokenUri())
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(PresetGuestTokenResponse.class)
        .map(response -> response.getData().getPayload().getGuestToken())
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)) // Retry x 3 w 2-sec delay
            .filter(ex -> !(ex instanceof WebClientResponseException.Unauthorized))) // except if 401
        .block();
  }


  private String getGuestTokenUri() {
    return "teams/"
        + properties.getTeamId()
        + "/workspaces/"
        + properties.getWorkspaceId()
        + "/guest-token/";
  }

  private void initialiseJwtToken() {
    this.jwtToken = fetchJwtToken();
  }

  private String fetchJwtToken() {
    PresetJwtTokenRequest request = new PresetJwtTokenRequest(
        this.properties.getApiToken(),
        this.properties.getApiSecret()
    );

    try {
      return authClient.post()
          .uri("auth/")
          .bodyValue(request)
          .retrieve()
          .bodyToMono(PresetJwtTokenResponse.class)
          .map(response -> response.getPayload().getJwtToken())
          .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))) // Retry x 3 w 2-sec delay
          .block();
    } catch (Exception e) {
      LogBuilder.builder(log)
          .action("Fetch Preset JWT Token")
          .message("Failed to fetch Preset JWT Token")
          .logError(e);
      throw(e);
    }
  }

  private PresetGuestTokenRequest createPresetGuestTokenRequest(String dashboardId) {
    PresetUser user = new PresetUser("guest_user", "Guest", "User");
    PresetResource resource = new PresetResource("dashboard", dashboardId);

    PresetGuestTokenRequest request =
        new PresetGuestTokenRequest(user, List.of(resource), List.of());

    return request;
  }

}
