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

import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;

import java.time.Duration;
import org.talentcatalog.perf.scenarios.http.candidatesearch.NewEndpointLoopScenario;
import org.talentcatalog.perf.scenarios.http.candidatesearch.OldEndpointLoopScenario;

/**
 * Runs a parallel A/B comparison of candidate search endpoints using Gatling's <b>closed workload
 * model</b>.
 *
 * <h2>Goal</h2>
 * Keep a constant number of concurrent users active across both endpoints for the full test
 * duration, split approximately 50/50 between:
 * <ul>
 *   <li><b>A (NEW)</b>: {@link NewEndpointLoopScenario}</li>
 *   <li><b>B (OLD-FETCH)</b>: {@link OldEndpointLoopScenario}</li>
 * </ul>
 *
 * <p>Each virtual user logs in once (via the scenario chain) and then loops {@code searchRepeats}
 * search calls with random think time between iterations.</p>
 *
 * <h2>Duration</h2>
 * Total runtime is {@code warmupSeconds + measureSeconds}. This simulation also sets
 * maxDuration(Duration) with a small buffer to ensure Gatling shuts down cleanly and writes reports.</p>
 *
 * <h2>System properties</h2>
 * <ul>
 *   <li>{@code -Dpayload=baseline|heavy} (default: heavy) – resolved by {@link CandidateSearchBaseSimulation}</li>
 *   <li>{@code -DtotalConcurrentUsers} (required, &gt; 0) – total concurrent users across A and B</li>
 *   <li>{@code -DwarmupSeconds} (default: 120)</li>
 *   <li>{@code -DmeasureSeconds} (default: 600)</li>
 *   <li>{@code -DsearchRepeats} (default: 10)</li>
 *   <li>{@code -DsearchMinPauseSeconds} (default: 0)</li>
 *   <li>{@code -DsearchMaxPauseSeconds} (default: 2)</li>
 * </ul>
 *
 * <h2>How to run (Gradle)</h2>
 * <pre>{@code
 * ./gradlew :performance-tests:gatlingTest \
 *   -PsimClass=org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchParallelClosedSimulation \
 *   -Dperf.baseUrl=https://YOUR_ENV_HOST \
 *   -Dpayload=heavy \
 *   -DtotalConcurrentUsers=100 \
 *   -DwarmupSeconds=120 -DmeasureSeconds=600 \
 *   -DsearchRepeats=10 \
 *   -DsearchMinPauseSeconds=0 \
 *   -DsearchMaxPauseSeconds=2
 * }</pre>
 */
public class CandidateSearchParallelClosedSimulation extends CandidateSearchBaseSimulation {

  /**
   * Total concurrent users across both scenarios (A + B). Required: {@code > 0}.
   */
  private static final int TOTAL_CONCURRENT_USERS = Integer.getInteger("totalConcurrentUsers", 0);

  /**
   * Warmup duration in seconds. Default: 120.
   */
  private static final int WARMUP_SECONDS = Integer.getInteger("warmupSeconds", 120);

  /**
   * Measurement duration in seconds. Default: 600.
   */
  private static final int MEASURE_SECONDS = Integer.getInteger("measureSeconds", 600);

  /**
   * Number of search iterations per virtual user. Default: 10.
   */
  private static final int SEARCH_REPEATS = Integer.getInteger("searchRepeats", 10);

  /**
   * Minimum think time between search iterations in seconds. Default: 0.
   */
  private static final int SEARCH_MIN_PAUSE_SECONDS = Integer.getInteger("searchMinPauseSeconds",
      0);

  /**
   * Maximum think time between search iterations in seconds. Default: 2.
   */
  private static final int SEARCH_MAX_PAUSE_SECONDS = Integer.getInteger("searchMaxPauseSeconds",
      2);

  /**
   * Constructs and configures the simulation.
   *
   * @throws IllegalArgumentException if required system properties are invalid
   */
  public CandidateSearchParallelClosedSimulation() {
    if (TOTAL_CONCURRENT_USERS <= 0) {
      throw new IllegalArgumentException("totalConcurrentUsers must be > 0");
    }
    if (WARMUP_SECONDS < 0) {
      throw new IllegalArgumentException("warmupSeconds must be >= 0");
    }
    if (MEASURE_SECONDS <= 0) {
      throw new IllegalArgumentException("measureSeconds must be > 0");
    }
    if (SEARCH_REPEATS <= 0) {
      throw new IllegalArgumentException("searchRepeats must be > 0");
    }
    if (SEARCH_MIN_PAUSE_SECONDS < 0) {
      throw new IllegalArgumentException("searchMinPauseSeconds must be >= 0");
    }
    if (SEARCH_MAX_PAUSE_SECONDS < SEARCH_MIN_PAUSE_SECONDS) {
      throw new IllegalArgumentException("searchMaxPauseSeconds must be >= searchMinPauseSeconds");
    }

    // Split total concurrency approximately 50/50 across A and B (ensure both get at least 1).
    int concA = Math.max(1, TOTAL_CONCURRENT_USERS / 2);
    int concB = Math.max(1, TOTAL_CONCURRENT_USERS - concA);

    var scnA = NewEndpointLoopScenario.build(
        payloadPath, "[NEW]", SEARCH_REPEATS, SEARCH_MIN_PAUSE_SECONDS, SEARCH_MAX_PAUSE_SECONDS
    );

    var scnB = OldEndpointLoopScenario.build(
        payloadPath, "[OLD]", SEARCH_REPEATS, SEARCH_MIN_PAUSE_SECONDS, SEARCH_MAX_PAUSE_SECONDS
    );

    int totalSeconds = WARMUP_SECONDS + MEASURE_SECONDS;

    setUp(
        scnA.injectClosed(constantConcurrentUsers(concA).during(totalSeconds)),
        scnB.injectClosed(constantConcurrentUsers(concB).during(totalSeconds))
    )
        .protocols(httpProtocol)
        .maxDuration(Duration.ofSeconds(totalSeconds + 30L))
        .assertions(defaultAssertions());
  }
}
