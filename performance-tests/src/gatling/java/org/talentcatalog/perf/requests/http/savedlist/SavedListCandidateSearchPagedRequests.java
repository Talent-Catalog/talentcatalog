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

package org.talentcatalog.perf.requests.http.savedlist;

import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.Body;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

/**
 * HTTP request builders for Saved List Candidate search-paged endpoints.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>NEW: POST /api/admin/saved-list-candidate/{listId}/search-paged</li>
 *   <li>OLD: POST /api/admin/saved-list-candidate/{listId}/search-paged-old-fetch</li>
 * </ul>
 */
public final class SavedListCandidateSearchPagedRequests {

  private SavedListCandidateSearchPagedRequests() {}

  public static HttpRequestActionBuilder searchPagedNew(long listId, Body body, String label) {
    return http("POST saved-list search-paged (new) " + label)
        .post("/api/admin/saved-list-candidate/" + listId + "/search-paged")
        .body(body)
        .asJson()
        .check(status().is(200));
  }

  public static HttpRequestActionBuilder searchPagedOldFetch(long listId, Body body, String label) {
    return http("POST saved-list search-paged (old-fetch) " + label)
        .post("/api/admin/saved-list-candidate/" + listId + "/search-paged-old-fetch")
        .body(body)
        .asJson()
        .check(status().is(200));
  }
}
