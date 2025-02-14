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

package org.tctalent.server.service.db.impl;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.response.DuolingoDashboardResponse;
import org.tctalent.server.response.DuolingoDashboardWrapper;
import org.tctalent.server.response.DuolingoVerifyScoreResponse;
import org.tctalent.server.service.db.DuolingoApiService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Service
@Slf4j
public class DuolingoApiServiceImpl implements DuolingoApiService {

  private final WebClient webClient;

  @Autowired
  public DuolingoApiServiceImpl(@Qualifier("duolingoWebClient") WebClient duolingoWebClient) {
    this.webClient = duolingoWebClient;
  }

  @Override
  public List<DuolingoDashboardResponse> getDashboardResults(LocalDateTime minDate, LocalDateTime maxDate) throws WebClientResponseException{
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    try {
      if (minDate == null && maxDate == null) {
        return webClient.get()
            .uri("/get_dashboard_results")
            .retrieve()
            .bodyToMono(DuolingoDashboardWrapper.class)
            .map(DuolingoDashboardWrapper::getExams)
            .block();
      }

      if (minDate != null && maxDate == null) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/get_dashboard_results")
                .queryParam("min_datetime", minDate.format(formatter))
                .build())
            .retrieve()
            .bodyToMono(DuolingoDashboardWrapper.class)
            .map(DuolingoDashboardWrapper::getExams)
            .block();
      }

      return webClient.get()
          .uri(uriBuilder -> {
            assert minDate != null;
            return uriBuilder
                .path("/get_dashboard_results")
                .queryParam("min_datetime", minDate.format(formatter))
                .queryParam("max_datetime", maxDate.format(formatter))
                .build();
          })
          .retrieve()
          .bodyToMono(DuolingoDashboardWrapper.class)
          .map(DuolingoDashboardWrapper::getExams)
          .block();

    } catch (WebClientResponseException ex) {
      LogBuilder.builder(log)
          .action("getDashboardResults")
          .message(String.format("Error fetching dashboard results for dates: %s - %s. Exception: %s",
              minDate, maxDate, ex.getMessage()))
          .logError(ex);
      throw ex;
    }
  }

  @Override
  public DuolingoVerifyScoreResponse verifyScore(String certificateId, String birthdate) throws WebClientResponseException{
    try {
      return webClient.get()
          .uri(uriBuilder -> uriBuilder
              .path("/verify_score")
              .queryParam("certificate_id", certificateId)
              .queryParam("birthdate", birthdate)
              .build())
          .retrieve()
          .bodyToMono(DuolingoVerifyScoreResponse.class)
          .block();

    } catch (WebClientResponseException ex) {
      LogBuilder.builder(log)
          .action("verifyScore")
          .message(String.format("Error verifying score for certificate ID '%s' and birthdate '%s'. Exception: %s",
              certificateId, birthdate, ex.getMessage()))
          .logError(ex);
      throw ex;
    }
  }
}
