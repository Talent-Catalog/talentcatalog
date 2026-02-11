package org.talentcatalog.perf.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Centralized configuration access for Gatling performance tests.
 *
 * <h2>Resolution order</h2>
 * <ol>
 *   <li><b>JVM system properties</b> (highest precedence), e.g. {@code -Dperf.baseUrl=https://...}</li>
 *   <li><b>Typesafe Config</b> loaded from {@code application.conf} under {@code src/gatling/resources}</li>
 * </ol>
 *
 * <p>All keys for this module are prefixed with {@code perf.} (for example, {@code perf.baseUrl}).
 *
 * <p>This class is intentionally stateless and non-instantiable. Use {@link #settings()} to obtain a
 * validated {@link PerfSettings} snapshot for the current JVM/config state.
 */
public final class PerfConfig {

  /**
   * Loaded Typesafe configuration. By default, {@link ConfigFactory#load()} searches the classpath
   * for {@code application.conf}, {@code reference.conf}, and other supported config files.
   */
  private static final Config CONF = ConfigFactory.load();

  /** Namespace prefix for all performance-test settings. */
  private static final String PREFIX = "perf.";

  /** Utility class: prevent instantiation. */
  private PerfConfig() {}

  /**
   * Reads the performance-test configuration and returns a validated settings snapshot.
   *
   * <p>Required:
   * <ul>
   *   <li>{@code perf.baseUrl} - Base URL of the target system under test.</li>
   * </ul>
   *
   * <p>Optional:
   * <ul>
   *   <li>{@code perf.userAgent} - User-Agent header value. Defaults to {@code "gatling"} when blank/missing.</li>
   * </ul>
   *
   * <p><b>Note:</b> Values are trimmed. A blank value is treated as "not set".
   *
   * @return an immutable {@link PerfSettings} object with validated values
   * @throws IllegalStateException if a required setting (currently {@code perf.baseUrl}) is missing/blank
   */
  public static PerfSettings settings() {
    // Base URL is required for all scenarios; fail fast with a clear error.
    String baseUrl = required();

    // Default user agent helps identify Gatling traffic in logs/APM.
    String userAgent = optional("userAgent");
    if (userAgent.isBlank()) userAgent = "gatling";

    return new PerfSettings(baseUrl, userAgent);
  }

  /**
   * Reads a required configuration value.
   *
   * <p>This delegates to {@link #optional(String)} and then enforces non-blank content.
   *
   * @return the trimmed, non-blank value
   * @throws IllegalStateException if the value is missing or blank
   */
  private static String required() {
    String v = optional("baseUrl");
    if (v.isBlank()) {
      // Include full key name to make failures easy to diagnose in CI/local runs.
      throw new IllegalStateException("Missing required config: " + PREFIX + "baseUrl");
    }
    return v;
  }

  /**
   * Reads an optional configuration value, applying the module's resolution order.
   *
   * <h3>Resolution</h3>
   * <ol>
   *   <li>System property {@code perf.<key>} via {@link System#getProperty(String)}</li>
   *   <li>Typesafe Config path {@code perf.<key>} via {@link Config#getString(String)}</li>
   * </ol>
   *
   * <p>Returns {@code ""} when the key is not present or resolves to blank content. Callers may
   * apply defaults as needed.
   *
   * @param key the un-prefixed key name (e.g., {@code "bearerToken"} for {@code perf.bearerToken})
   * @return the trimmed value, or {@code ""} if missing/blank
   */
  private static String optional(String key) {
    // Prefer JVM system properties so CI/CD or local runs can override without editing files.
    String sysKey = PREFIX + key;
    String sysVal = System.getProperty(sysKey);
    if (sysVal != null && !sysVal.isBlank()) return sysVal.trim();

    // Fall back to application.conf (or other classpath config) if present.
    String confKey = PREFIX + key;
    if (CONF.hasPath(confKey)) {
      String confVal = CONF.getString(confKey);
      return confVal == null ? "" : confVal.trim();
    }

    // Not found anywhere: treat as unset.
    return "";
  }
}
