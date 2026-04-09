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

package org.talentcatalog.perf.requests.http.auth;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static org.talentcatalog.perf.chains.AuthChains.SESSION_ACCESS_TOKEN;
import static org.talentcatalog.perf.chains.AuthChains.SESSION_LOGIN_BODY;

import io.gatling.javaapi.http.HttpRequestActionBuilder;

/**
 * HTTP request builders related to authentication.
 *
 * <p>These builders are intended to be composed into Gatling chains/scenarios.
 * A successful login extracts {@code $.accessToken} from the response JSON and stores it in the
 * Gatling session under {@link org.talentcatalog.perf.chains.AuthChains#SESSION_ACCESS_TOKEN},
 * enabling subsequent authenticated requests.</p>
 *
 * <p>This request expects the following Gatling session attributes (typically provided via
 * feeders):
 * {@code username}, {@code password}, {@code reCaptchaV3Token}, {@code totpToken}.</p>
 */
public final class AuthRequests {

  /**
   * Utility class: prevent instantiation.
   */
  private AuthRequests() {
  }

  /**
   * Performs a login POST request and stores useful values in the Gatling session.
   *
   * <p>On success (HTTP 200), this method saves:
   * <ul>
   *   <li>the raw response body to {@link org.talentcatalog.perf.chains.AuthChains#SESSION_LOGIN_BODY} (useful for debugging failures)</li>
   *   <li>the extracted {@code $.accessToken} to {@link org.talentcatalog.perf.chains.AuthChains#SESSION_ACCESS_TOKEN}</li>
   * </ul>
   *
   * <p>The request body is JSON and uses Gatling Expression Language (EL) placeholders,
   * e.g. {@code "#{username}"}.</p>
   *
   * @param name request name shown in Gatling reports (e.g. {@code "POST /api/admin/auth/login"})
   * @param path request path relative to the configured base URL (e.g.
   *             {@code "/api/admin/auth/login"})
   * @return an {@link HttpRequestActionBuilder} for the login request
   */
  public static HttpRequestActionBuilder login(String name, String path) {
    return http(name)
        .post(path)
        .asJson()
        .body(StringBody("""
            {
              "username": "#{username}",
              "password": "#{password}",
              "reCaptchaV3Token": "#{reCaptchaV3Token}",
              "totpToken": "#{totpToken}"
            }
            """))
        .check(bodyString().saveAs(SESSION_LOGIN_BODY))
        .check(status().is(200))
        .check(jsonPath("$.accessToken").exists())
        .check(jsonPath("$.accessToken").saveAs(SESSION_ACCESS_TOKEN));
  }
}
