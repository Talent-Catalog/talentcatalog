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

package org.talentcatalog.perf.simulations.http.savedlist;

import static io.gatling.javaapi.core.CoreDsl.global;

import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.util.ArrayList;
import org.talentcatalog.perf.config.HttpProtocolFactory;
import org.talentcatalog.perf.config.PerfConfig;
import org.talentcatalog.perf.payloads.SavedListSearchPagedPayloads;

/**
 * Base class for Saved List candidate search-paged Gatling simulations.
 *
 * <p>This base class centralizes shared setup so individual simulations can focus on scenarios and
 * injection profiles.</p>
 *
 * <h2>Shared responsibilities</h2>
 * <ul>
 *   <li>Resolve the saved-list id from {@code -DlistId=...}</li>
 *   <li>Resolve the request payload mode from {@code -Dpayload=...}</li>
 *   <li>Load performance settings via {@link PerfConfig#settings()}</li>
 *   <li>Build a shared {@link HttpProtocolBuilder} via {@link HttpProtocolFactory}</li>
 *   <li>Provide standard/global assertions (correctness + optional latency)</li>
 * </ul>
 *
 * <h2>Saved-list selection</h2>
 * <p>The {@link #PROP_LIST_ID} system property provides the saved list id used by the endpoints:</p>
 * <ul>
 *   <li>{@code -DlistId=1283} → uses list id {@code 1283}</li>
 * </ul>
 *
 * <p>This property is required; the constructor throws if the id is missing or {@code <= 0}.</p>
 *
 * <h2>Payload selection</h2>
 * <p>The {@link #PROP_PAYLOAD} system property selects which request payload file to use:</p>
 * <ul>
 *   <li>{@code -Dpayload=light} → {@link SavedListSearchPagedPayloads#LIGHT} (default)</li>
 * </ul>
 *
 * <p>If the property is missing, blank, or an unknown value is provided, this class falls back to
 * {@code light}.</p>
 */
public abstract class SavedListSearchPagedBaseSimulation extends Simulation {

  /**
   * System property key used to provide the saved list id.
   *
   * <p>Example: {@code -DlistId=1283}</p>
   */
  protected static final String PROP_LIST_ID = "listId";

  /**
   * System property key used to choose the saved-list search-paged payload mode.
   *
   * <p>Allowed values: {@code light}. Default: {@code light}.</p>
   */
  protected static final String PROP_PAYLOAD = "payload";

  /**
   * Required saved-list id used by saved-list search-paged endpoints.
   */
  protected final long listId;

  /**
   * Resolved classpath resource path for the selected payload JSON.
   */
  protected final String payloadPath;

  /**
   * HTTP protocol configuration shared by all simulations (baseUrl, headers, etc.).
   */
  protected final HttpProtocolBuilder httpProtocol;

  /**
   * System property key for the maximum allowed global p95 response time in milliseconds.
   *
   * <p>Set to {@code 0} to disable the p95 assertion.</p>
   *
   * <p>Example: {@code -Dperf.maxLatencyMs=15000} or {@code -Dperf.maxLatencyMs=0}</p>
   */
  protected static final String PROP_MAX_LATENCY_MS = "perf.maxLatencyMs";

  /**
   * Default p95 threshold (ms) when {@link #PROP_MAX_LATENCY_MS} is not provided.
   */
  protected static final int DEFAULT_MAX_LATENCY_MS = 2000;

  /**
   * System property key for the maximum allowed global failed request percentage.
   *
   * <p>This is the correctness gate. If the percentage of failed requests (KO) exceeds this value,
   * the simulation fails.</p>
   *
   * <p>Example: {@code -Dperf.maxFailedPct=1.0}</p>
   */
  protected static final String PROP_MAX_FAILED_PCT = "perf.maxFailedPct";

  /**
   * Default maximum failed request percentage when {@link #PROP_MAX_FAILED_PCT} is not provided.
   */
  protected static final double DEFAULT_MAX_FAILED_PCT = 1.0;

  /**
   * Initializes shared simulation configuration:
   * <ul>
   *   <li>Loads {@link org.talentcatalog.perf.config.PerfSettings} via {@link PerfConfig#settings()}</li>
   *   <li>Builds {@link #httpProtocol}</li>
   *   <li>Resolves {@link #listId} from {@code -DlistId}</li>
   *   <li>Resolves {@link #payloadPath} from {@code -Dpayload}</li>
   * </ul>
   *
   * @throws IllegalArgumentException if {@code -DlistId} is missing or {@code <= 0}
   */
  protected SavedListSearchPagedBaseSimulation() {
    var settings = PerfConfig.settings();
    this.httpProtocol = HttpProtocolFactory.build(settings);

    this.listId = Long.parseLong(System.getProperty(PROP_LIST_ID, "0"));
    if (this.listId <= 0) {
      throw new IllegalArgumentException("listId must be > 0 (use -DlistId=1283)");
    }

    this.payloadPath = resolvePayloadPath();
  }

  /**
   * Resolves the payload JSON file path from {@code -Dpayload}.
   *
   * <p>Trims and lowercases the property value. Defaults to {@code light} when missing, blank,
   * or unrecognized.</p>
   *
   * @return the classpath resource path for the selected payload
   */
  private static String resolvePayloadPath() {
    String mode = System.getProperty(PROP_PAYLOAD, "light").trim().toLowerCase();
    return switch (mode) {
      case "light" -> SavedListSearchPagedPayloads.LIGHT;
      default -> SavedListSearchPagedPayloads.LIGHT;
    };
  }

  /**
   * Reads an integer system property safely.
   *
   * @param key        property name
   * @param defaultVal fallback value
   * @return parsed integer or fallback
   */
  protected static int getIntProp(String key, int defaultVal) {
    String v = System.getProperty(key);
    if (v == null || v.trim().isEmpty()) {
      return defaultVal;
    }
    try {
      return Integer.parseInt(v.trim());
    } catch (Exception e) {
      return defaultVal;
    }
  }

  /**
   * Reads a double system property safely.
   *
   * @param key        property name
   * @param defaultVal fallback value
   * @return parsed double or fallback
   */
  protected static double getDoubleProp(String key, double defaultVal) {
    String v = System.getProperty(key);
    if (v == null || v.trim().isEmpty()) {
      return defaultVal;
    }
    try {
      return Double.parseDouble(v.trim());
    } catch (Exception e) {
      return defaultVal;
    }
  }

  /**
   * Builds the default global assertion set for saved-list simulations.
   *
   * <h2>Correctness</h2>
   * <p>Always enforced via {@code global().failedRequests().percent().lt(maxFailedPct)} where
   * {@code maxFailedPct} is controlled by {@code -Dperf.maxFailedPct} (default {@code 1.0}).</p>
   *
   * <h2>Latency</h2>
   * <p>Optionally enforced via p95:
   * {@code global().responseTime().percentile3().lt(maxLatencyMs)} where {@code maxLatencyMs} is controlled
   * by {@code -Dperf.maxLatencyMs} (default {@code 2000}). Set {@code -Dperf.maxLatencyMs=0} to
   * disable.</p>
   *
   * @return an array of Gatling assertions to apply in {@code setUp(...).assertions(...)}
   */
  protected static Assertion[] defaultAssertions() {
    var assertions = new ArrayList<Assertion>();

    double maxFailedPct = getDoubleProp(PROP_MAX_FAILED_PCT, DEFAULT_MAX_FAILED_PCT);
    int maxLatencyMs = getIntProp(PROP_MAX_LATENCY_MS, DEFAULT_MAX_LATENCY_MS);

    // Correctness gate (always on)
    assertions.add(global().failedRequests().percent().lt(maxFailedPct));

    // Optional latency gate
    if (maxLatencyMs > 0) {
      assertions.add(global().responseTime().percentile3().lt(maxLatencyMs));
    }

    return assertions.toArray(new Assertion[0]);
  }
}