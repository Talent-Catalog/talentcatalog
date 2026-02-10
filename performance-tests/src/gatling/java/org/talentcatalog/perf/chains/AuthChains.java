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
package org.talentcatalog.perf.chains;

import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;

import io.gatling.javaapi.core.ChainBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talentcatalog.perf.requests.http.auth.AuthRequests;

/**
 * Reusable Gatling chain(s) for authentication.
 *
 * <p>This class centralizes:
 * <ul>
 *   <li>Session keys used to store auth-related values (token, raw login response)</li>
 *   <li>Default login endpoint metadata (request name + path)</li>
 *   <li>An idempotent login chain that logs helpful diagnostics on failures</li>
 * </ul>
 *
 * <p>Typical usage in a scenario:
 * <pre>{@code
 * scenario("My Scenario")
 *   .exec(AuthChains.ensureLoggedIn())
 *   .exec(... authenticated requests ...);
 * }</pre>
 */
public final class AuthChains {

  private static final Logger log = LoggerFactory.getLogger(AuthChains.class);

  /**
   * Gatling session key holding the access token extracted from the login response.
   * <p>Used by authenticated requests as {@code Authorization: Bearer <token>}.</p>
   */
  public static final String SESSION_ACCESS_TOKEN = "accessToken";

  /**
   * Gatling session key holding the raw login response body (debugging aid).
   * <p>Populated by {@link AuthRequests#login(String, String)} via
   * {@code bodyString().saveAs(...)}.</p>
   */
  public static final String SESSION_LOGIN_BODY = "loginBody";

  /**
   * Default request name used for the login request in Gatling reports.
   * <p>Example: {@code "POST /api/admin/auth/login"}.</p>
   */
  public static final String LOGIN_ENDPOINT_NAME = "POST /api/admin/auth/login";

  /**
   * Default login endpoint path relative to the configured base URL.
   * <p>Example: {@code "/api/admin/auth/login"}.</p>
   */
  public static final String LOGIN_ENDPOINT_PATH = "/api/admin/auth/login";

  /**
   * Utility class: prevent instantiation.
   */
  private AuthChains() {
  }

  /**
   * Ensures the virtual user is logged in (idempotent).
   *
   * <p>If {@link #SESSION_ACCESS_TOKEN} is already present in the session, this chain does
   * nothing. Otherwise it performs a login request and:
   * <ul>
   *   <li>marks the session as failed if the token is still missing</li>
   *   <li>logs the raw login response body (when available) to help diagnose failures</li>
   * </ul>
   *
   * <p>On failure, the chain exits early via {@code exitHereIfFailed()}.</p>
   *
   * @return a {@link ChainBuilder} that guarantees an access token is present for subsequent steps
   */
  public static ChainBuilder ensureLoggedIn() {
    return doIf(session -> !session.contains(SESSION_ACCESS_TOKEN)).then(
        exec(AuthRequests.login(LOGIN_ENDPOINT_NAME, LOGIN_ENDPOINT_PATH))
            .exec(session -> {
              if (!session.contains(SESSION_ACCESS_TOKEN)) {
                String body = session.contains(SESSION_LOGIN_BODY)
                    ? session.getString(SESSION_LOGIN_BODY)
                    : "<no loginBody>";
                return session.markAsFailed();
              }
              return session;
            })
            .exitHereIfFailed()
    );
  }
}
