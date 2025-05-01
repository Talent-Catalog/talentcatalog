/*
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

package org.tctalent.server.service.api;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.tctalent.server.configuration.properties.TcApiServiceProperties;

/**
 * @author sadatmalik
 */
@Service
public class TcApiServiceImpl implements TcApiService {

  private final TcApiServiceProperties properties;
  private final RestClient restClient;

  public TcApiServiceImpl(RestClient.Builder restClientBuilder,
      TcApiServiceProperties properties) {
    this.properties = properties;
    this.restClient = restClientBuilder.baseUrl(properties.getApiUrl()).build();
  }

  @Override
  public String runApiMigration() throws RestClientException {
    try {
      return restClient.post()
          .uri("/batch/jobs/run")
          .contentType(APPLICATION_JSON)
          .header("x-api-key", properties.getApiKey())
          .retrieve()
          .body(String.class);

    } catch (HttpClientErrorException | HttpServerErrorException e) {
      return e.getResponseBodyAsString();
    }
  }

  @Override
  public String listApiMigrations() {
    try {
      return restClient.get()
          .uri("/batch/jobs")
          .header("x-api-key", properties.getApiKey())
          .retrieve()
          .body(String.class);

    } catch (HttpClientErrorException | HttpServerErrorException e) {
      return e.getResponseBodyAsString();
    }
  }

  @Override
  public String stopApiMigration(long id) {
    try {
      return restClient.post()
          .uri("/batch/jobs/" + id + "/stop")
          .contentType(APPLICATION_JSON)
          .header("x-api-key", properties.getApiKey())
          .retrieve()
          .body(String.class);

    } catch (HttpClientErrorException | HttpServerErrorException e) {
      return e.getResponseBodyAsString();
    }
  }

}
