package org.talentcatalog.perf.http;

import static io.gatling.javaapi.http.HttpDsl.http;
import static org.talentcatalog.perf.chains.AuthChains.SESSION_ACCESS_TOKEN;

import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.talentcatalog.perf.config.PerfSettings;

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
