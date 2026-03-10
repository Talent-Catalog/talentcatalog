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
package org.talentcatalog.perf.simulations.http.candidatesearch;

import static io.gatling.javaapi.core.CoreDsl.global;

import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.util.ArrayList;
import org.talentcatalog.perf.config.HttpProtocolFactory;
import org.talentcatalog.perf.config.PerfConfig;
import org.talentcatalog.perf.payloads.CandidateSearchPayloads;

/**
 * Base class for Candidate Search Gatling simulations.
 *
 * <p>This base class centralizes shared setup so individual simulations can focus on scenarios and
 * injection profiles.</p>
 *
 * <h2>Shared responsibilities</h2>
 * <ul>
 *   <li>Resolve the candidate search payload mode from {@code -Dpayload=...}</li>
 *   <li>Load performance settings via {@link PerfConfig#settings()}</li>
 *   <li>Build a shared {@link HttpProtocolBuilder} via {@link HttpProtocolFactory}</li>
 *   <li>Provide standard/global assertions (correctness + optional latency)</li>
 * </ul>
 *
 * <h2>Payload selection</h2>
 * <p>The {@link #PROP_PAYLOAD} system property selects which request payload file to use:</p>
 * <ul>
 *   <li>{@code -Dpayload=baseline} → {@link CandidateSearchPayloads#BASELINE}</li>
 *   <li>{@code -Dpayload=heavy} → {@link CandidateSearchPayloads#HEAVY} (default)</li>
 * </ul>
 *
 * <p>If an unknown value is provided, this class falls back to {@code heavy}.</p>
 */
public abstract class CandidateSearchBaseSimulation extends Simulation {

  /**
   * System property key used to choose the candidate-search payload mode.
   *
   * <p>Allowed values: {@code baseline}, {@code heavy}. Default: {@code heavy}.</p>
   */
  protected static final String PROP_PAYLOAD = "payload";

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
   * <p>Set to {@code 0} to disable the p95 assertion (useful for nightlies where you want the run
   * to complete and enforce latency budgets later via aggregated checks).</p>
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
   *   <li>Resolves {@link #payloadPath}</li>
   * </ul>
   */
  protected CandidateSearchBaseSimulation() {
    var settings = PerfConfig.settings();
    this.httpProtocol = HttpProtocolFactory.build(settings);
    this.payloadPath = resolvePayloadPath();
  }

  /**
   * Resolves the payload JSON file path from {@code -Dpayload}.
   *
   * <p>Trims and lowercases the property value. Defaults to {@code heavy} when missing or
   * blank.</p>
   *
   * @return the classpath resource path for the selected payload
   */
  private static String resolvePayloadPath() {
    String mode = System.getProperty(PROP_PAYLOAD, "heavy").trim().toLowerCase();
    return switch (mode) {
      case "baseline" -> CandidateSearchPayloads.BASELINE;
      case "heavy" -> CandidateSearchPayloads.HEAVY;
      default -> CandidateSearchPayloads.HEAVY; // fallback for unknown values
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
   * Builds the default global assertion set for candidate-search simulations.
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
