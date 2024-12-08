package org.tctalent.server.service.db;/*
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


import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.response.DuolingoDashboardResponse;
import org.tctalent.server.response.DuolingoVerifyScoreResponse;
import reactor.core.publisher.Mono;

/**
 * Interface for interacting with the Duolingo API.
 * Provides methods to retrieve dashboard results and verify scores.
 */
public interface DuolingoApiService {

  /**
   * Retrieves a list of Duolingo test results (Dashboard) for the specified date range.
   *
   * @param minDate the minimum date of the results to be fetched (inclusive)
   * @param maxDate the maximum date of the results to be fetched (inclusive)
   * @return a {@link List} containing a list of {@link DuolingoDashboardResponse} representing the test results
   * @throws WebClientException if there is an error while making the API call or processing the response.
   */
  List<DuolingoDashboardResponse> getDashboardResults(LocalDateTime minDate, LocalDateTime maxDate) throws WebClientException;

  /**
   * Verifies a Duolingo test score using the provided certificate ID and birthdate.
   *
   * @param certificateId the certificate ID of the Duolingo test
   * @param birthdate the birthdate of the individual taking the test
   * @return a {@link DuolingoVerifyScoreResponse} containing the verification results and score details.
   * @throws WebClientException if there is an error while making the API call or processing the response.
   */
  DuolingoVerifyScoreResponse verifyScore(String certificateId, String birthdate) throws WebClientException;
}
