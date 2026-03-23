/*
 * Copyright (c) 2026 Talent Catalog.
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
package org.talentcatalog.perf.requests.http.candidatesearch;

import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.Body;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

/**
 * HTTP request builders for the candidate search endpoints used in performance tests.
 *
 * <p>These methods return Gatling {@link HttpRequestActionBuilder}s that can be composed into
 * chains/scenarios. Each request accepts a prepared {@link Body} (typically JSON) and a human-readable
 * label to help differentiate requests in Gatling reports (e.g., different payload sizes or query types).</p>
 */
public final class CandidateSearchRequests {

  /** Utility class: prevent instantiation. */
  private CandidateSearchRequests() {}

  /**
   * Executes the "new" candidate search endpoint.
   *
   * <p>Request:
   * <ul>
   *   <li>Method: {@code POST}</li>
   *   <li>Path: {@code /api/admin/candidate/search}</li>
   * </ul>
   *
   * <p>Response expectation: HTTP 200.</p>
   *
   * @param body  request body (usually JSON); typically built with Gatling {@code StringBody(...)}
   * @param label label appended to the request name for reporting/debugging (e.g., {@code "[small-payload]"})
   * @return an {@link HttpRequestActionBuilder} for the search request
   */
  public static HttpRequestActionBuilder searchNew(Body body, String label) {
    return http("POST candidate search (new) " + label)
        .post("/api/admin/candidate/search")
        .body(body)
        .asJson()
        .check(status().is(200));
  }

  /**
   * Executes the legacy "old-fetch" candidate search endpoint.
   *
   * <p>Request:
   * <ul>
   *   <li>Method: {@code POST}</li>
   *   <li>Path: {@code /api/admin/candidate/search-old-fetch}</li>
   * </ul>
   *
   * <p>Response expectation: HTTP 200.</p>
   *
   * @param body  request body (usually JSON); typically built with Gatling {@code StringBody(...)} or similar
   * @param label label appended to the request name for reporting/debugging (e.g., {@code "[old-fetch]"})
   * @return an {@link HttpRequestActionBuilder} for the old-fetch search request
   */
  public static HttpRequestActionBuilder searchOldFetch(Body body, String label) {
    return http("POST candidate search (old-fetch) " + label)
        .post("/api/admin/candidate/search-old-fetch")
        .body(body)
        .asJson()
        .check(status().is(200));
  }
}
