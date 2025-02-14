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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tctalent.server.configuration.properties.PresetProperties;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.request.preset.PresetGuestTokenRequest;
import org.tctalent.server.request.preset.PresetGuestTokenRequest.User;
import org.tctalent.server.request.preset.PresetJwtTokenRequest;
import org.tctalent.server.response.PresetAuthResponse;
import org.tctalent.server.service.db.PresetApiService;

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

  public String fetchGuestToken() throws WebClientResponseException {
    if (jwtToken == null) {
      initialiseJwtToken();
    }

    PresetGuestTokenRequest request = createPresetGuestTokenRequest();

    // TODO: needs to handle expired/invalid token (retry a few times, fail gracefully)
    try {
      return authClient.post()
          .uri(getGuestTokenUri())
          .bodyValue(request)
          .retrieve()
          .bodyToMono(String.class)
          .block();
    } catch (WebClientResponseException e) {
      LogBuilder.builder(log)
          .action("Fetch Preset Guest Token")
          .message("Failed to fetch Preset Guest Token")
          .logError(e);
      throw e;
    }
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
    // TODO: needs to handle expired/invalid token (retry a few times, fail gracefully)

    PresetJwtTokenRequest request = new PresetJwtTokenRequest(
        this.properties.getApiToken(),
        this.properties.getApiSecret()
    );

    try {
      return authClient.post()
          .uri("auth/")
          .bodyValue(request)
          .retrieve()
          .bodyToMono(PresetAuthResponse.class)
          .map(response -> response.getPayload().getAccessToken())
          .block(); // Blocking call to store token at startup
    } catch (WebClientResponseException e) {
      LogBuilder.builder(log)
          .action("Fetch Preset JWT Token")
          .message("Failed to fetch Preset JWT Token")
          .logError(e);
      throw(e);
    }
  }

  private PresetGuestTokenRequest createPresetGuestTokenRequest() {
    PresetGuestTokenRequest request = new PresetGuestTokenRequest();
    // TODO add required properties to yml and properties file and populate request

    return request;
  }

}
