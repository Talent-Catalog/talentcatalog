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

package org.tctalent.server.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.tctalent.server.configuration.properties.PresetProperties;
import org.tctalent.server.request.preset.PresetAuthRequest;
import org.tctalent.server.response.PresetAuthResponse;
import reactor.core.publisher.Mono;

/**
 * Configuration class for setting up a WebClient to interact with the Preset API.
 * <p>
 * This class provides the necessary configuration for connecting to the Preset API by using the
 * {@link WebClient} to send HTTP requests. It retrieves the required API properties, such as the
 * base URL and API secret, from the application properties (specifically, the "preset.api" prefix).
 * </p>
 */
@Getter
@Setter
@Configuration
public class PresetConfig {

  // The properties required to interact with the Preset API
  private final PresetProperties properties;
  private final WebClient.Builder builder;
  private String jwtToken; // Store the JWT token after retrieval

  /**
   * Constructor that accepts a {@link PresetProperties} instance containing configuration values.
   *
   * @param properties the Preset properties containing the API secret and base URL
   */
  public PresetConfig(PresetProperties properties, WebClient.Builder builder) {
    this.properties = properties;
    this.builder = builder;
  }

  /**
   * Creates and configures a {@link WebClient} to interact with the Preset API.
   *
   * <p>This WebClient automatically adds the JWT token required for authentication.
   *
   * @return the configured WebClient instance
   */
  @Bean
  public WebClient presetWebClient(WebClient.Builder builder) {
    return builder
        .baseUrl(this.properties.getApiBaseUrl())
        .filter(authenticationFilter())
        .build();
  }

  /**
   * Retrieves a JWT token from the Preset API using the API secret.
   *
   * <p>@PostConstruct annotation means token will be fetched after the dependency injection is
   * complete, but before the bean is used.
   */
  @PostConstruct
  public void initializeToken() {
    WebClient authClient = builder.baseUrl(this.properties.getAuthBaseUrl()).build();
    this.jwtToken = fetchJwtToken(authClient);
  }

  /**
   * Makes an API call to retrieve the JWT token from Preset.
   */
  private String fetchJwtToken(WebClient authClient) {
    PresetAuthRequest request = new PresetAuthRequest(
        this.properties.getApiToken(),
        this.properties.getApiSecret()
    );

    return authClient.post()
        .bodyValue(request)
        .retrieve()
        .bodyToMono(PresetAuthResponse.class)
        .map(response -> response.getPayload().getAccessToken())
        .block(); // Blocking call to store token at startup
  }

  /**
   * Adds the JWT token to each request for authentication.
   */
  private ExchangeFilterFunction authenticationFilter() {
    return ExchangeFilterFunction.ofRequestProcessor(request ->
        Mono.just(ClientRequest.from(request)
            .headers(headers -> headers.setBearerAuth(jwtToken))
            .build()
        )
    );
  }

}
