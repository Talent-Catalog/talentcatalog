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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/*
 * DuolingoWebClientConfig:
 * Configures a WebClient to interact with the Duolingo API, retrieving the API secret and base URL
 * from application properties (duolingo.api).
 */
@Getter
@Setter
@ConfigurationProperties("duolingo.api")
public class DuolingoWebClentConfig {

  // The API secret used for authentication with the Duolingo API
  private String apiSecret;
  // The base URL of the Duolingo API
  private String baseUrl;

  /**
   * Creates and returns a WebClient configured with the base URL, a custom filter and API secret for making requests
   * to the Duolingo API. Basic authentication is used with the API secret.
   */
  @Bean
  public WebClient duolingoWebClient() {
    return WebClient.builder()
        .baseUrl(baseUrl)
        .filter(authenticationFilter())
        .build();
  }

  /**
   * A filter for adding authentication headers to every request.
   */
  private ExchangeFilterFunction authenticationFilter() {
    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
      return Mono.just(
          ClientRequest.from(clientRequest)
              .headers(headers -> headers.setBasicAuth(apiSecret, ""))
              .build()
      );
    });
  }

}
