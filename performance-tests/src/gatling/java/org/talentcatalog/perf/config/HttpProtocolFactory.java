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

package org.talentcatalog.perf.config;

import static io.gatling.javaapi.http.HttpDsl.http;
import static org.talentcatalog.perf.chains.AuthChains.SESSION_ACCESS_TOKEN;

import io.gatling.javaapi.http.HttpProtocolBuilder;

/**
 * Factory for the shared Gatling {@link HttpProtocolBuilder}.
 *
 * <p>Centralizes common HTTP settings used across simulations:
 * <ul>
 *   <li>Base URL ({@link PerfSettings#baseUrl()})</li>
 *   <li>JSON headers: {@code Accept} and {@code Content-Type}</li>
 *   <li>User-Agent ({@link PerfSettings#userAgent()})</li>
 *   <li>Authorization header derived from the session access token, when present</li>
 * </ul>
 *
 * <p>The access token is expected to be stored in the Gatling session under
 * {@link org.talentcatalog.perf.chains.AuthChains#SESSION_ACCESS_TOKEN}.</p>
 */
public final class HttpProtocolFactory {

  /**
   * Utility class: prevent instantiation.
   */
  private HttpProtocolFactory() {
  }

  /**
   * Builds an {@link HttpProtocolBuilder} configured for the current performance test run.
   *
   * @param s validated performance settings (base URL and user agent)
   * @return a configured {@link HttpProtocolBuilder} to be reused by simulations
   */
  public static HttpProtocolBuilder build(PerfSettings s) {
    return http.baseUrl(s.baseUrl())
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .header(
            "Authorization",
            session -> session.contains(SESSION_ACCESS_TOKEN)
                ? "Bearer " + session.getString(SESSION_ACCESS_TOKEN)
                : ""
        )
        .userAgentHeader(s.userAgent());
  }
}
