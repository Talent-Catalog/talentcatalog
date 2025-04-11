/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.tctalent.server.configuration.properties.DuolingoProperties;
import reactor.core.publisher.Mono;

/**
 * Configuration class for setting up a WebClient to interact with the Duolingo API.
 * <p>
 * This class provides the necessary configuration for connecting to the Duolingo API by using the
 * {@link WebClient} to send HTTP requests. It retrieves the required API properties, such as the
 * base URL and API secret, from the application properties (specifically, the "duolingo.api" prefix).
 * </p>
 */
@Getter
@Setter
@Configuration
public class DuolingoConfig {

  // The API secret used for authentication with the Duolingo API
  private final DuolingoProperties properties;
  private final WebClient.Builder builder;
  /**
   * Constructor that accepts a {@link DuolingoProperties} instance containing configuration values.
   *
   * @param properties the Duolingo properties containing the API secret and base URL
   */
  public DuolingoConfig(DuolingoProperties properties, WebClient.Builder builder) {
    this.properties = properties;
    this.builder = builder;
  }
  /**
   * Creates and configures a {@link WebClient} to interact with the Duolingo API.
   * <p>
   * The WebClient is configured with the base URL defined in {@link DuolingoProperties} and a custom
   * authentication filter that adds the API secret to the request headers using basic authentication.
   * </p>
   *
   * @return the configured WebClient instance
   */
  @Bean
  public WebClient duolingoWebClient(WebClient.Builder builder) {
    return builder
        .baseUrl(this.properties.getBaseUrl())
        .filter(authenticationFilter())
        .build();
  }

  /**
   * Creates a filter function for adding authentication headers to each API request.
   * <p>
   * This filter adds the API secret as a basic authentication header to every request made by the WebClient.
   * </p>
   *
   * @return the {@link ExchangeFilterFunction} used for authentication
   */
  private ExchangeFilterFunction authenticationFilter() {
    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
      return Mono.just(
          ClientRequest.from(clientRequest)
              .headers(headers -> headers.setBasicAuth(this.properties.getApiSecret(), ""))
              .build()
      );
    });
  }

}
