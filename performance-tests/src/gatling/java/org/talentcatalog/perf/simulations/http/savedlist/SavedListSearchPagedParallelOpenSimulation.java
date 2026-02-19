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

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;

import java.time.Duration;
import org.talentcatalog.perf.scenarios.http.savedlist.NewSearchPagedLoopScenario;
import org.talentcatalog.perf.scenarios.http.savedlist.OldSearchPagedLoopScenario;

/**
 * Runs a parallel A/B comparison of saved-list paged search endpoints using Gatling's
 * <b>open workload model</b>.
 *
 * <h2>Goal</h2>
 * Generates a constant arrival rate (requests started per second) split approximately 50/50
 * between:
 * <ul>
 *   <li><b>A (NEW)</b>: {@link NewSearchPagedLoopScenario}</li>
 *   <li><b>B (OLD)</b>: {@link OldSearchPagedLoopScenario}</li>
 * </ul>
 *
 * <p>Each virtual user logs in once (inside the scenario) and then loops {@code searchRepeats} paged-search calls
 * with random think time, so the test isolates paged search endpoint performance rather than login throughput
 * or rate limits.</p>
 *
 * <h2>Phases</h2>
 * <ol>
 *   <li><b>Ramp</b>: {@code 0 -> (totalUsersPerSec/2)} over {@code rampSeconds}</li>
 *   <li><b>Warmup</b>: constant {@code totalUsersPerSec/2} over {@code warmupSeconds}</li>
 *   <li><b>Measure</b>: constant {@code totalUsersPerSec/2} over {@code measureSeconds}</li>
 * </ol>
 *
 * <h2>System properties</h2>
 * <ul>
 *   <li>{@code -Dperf.baseUrl} (required) – target base URL</li>
 *   <li>{@code -Dpayload=baseline|heavy} (default: heavy) – resolved by {@link SavedListSearchPagedBaseSimulation}</li>
 *   <li>{@code -DlistId} (required) – resolved by {@link SavedListSearchPagedBaseSimulation}</li>
 *   <li>{@code -DtotalUsersPerSec} (required, &gt; 0) – total arrival rate across A and B</li>
 *   <li>{@code -DrampSeconds} (default: 60)</li>
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
 *   -PsimClass=org.talentcatalog.perf.simulations.http.savedlist.SavedListSearchPagedParallelOpenSimulation \
 *   -Dperf.baseUrl=https://YOUR_ENV_HOST \
 *   -Dpayload=heavy \
 *   -DlistId=12345 \
 *   -DtotalUsersPerSec=20 \
 *   -DrampSeconds=60 -DwarmupSeconds=120 -DmeasureSeconds=600 \
 *   -DsearchRepeats=10 \
 *   -DsearchMinPauseSeconds=0 \
 *   -DsearchMaxPauseSeconds=2
 * }</pre>
 */
public class SavedListSearchPagedParallelOpenSimulation extends SavedListSearchPagedBaseSimulation {

  /**
   * Total arrival rate across both scenarios (A + B), in users/second. Required: {@code > 0}.
   */
  private static final double TOTAL_USERS_PER_SEC =
      Double.parseDouble(System.getProperty("totalUsersPerSec", "0"));

  /**
   * Ramp-up duration in seconds. Default: 60.
   */
  private static final int RAMP_SECONDS = Integer.getInteger("rampSeconds", 60);

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
  private static final int SEARCH_MIN_PAUSE_SECONDS =
      Integer.getInteger("searchMinPauseSeconds", 0);

  /**
   * Maximum think time between search iterations in seconds. Default: 2.
   */
  private static final int SEARCH_MAX_PAUSE_SECONDS =
      Integer.getInteger("searchMaxPauseSeconds", 2);

  /** Total planned test duration (ramp + warmup + measure) in seconds, excluding shutdown buffer. */
  int plannedSeconds = RAMP_SECONDS + WARMUP_SECONDS + MEASURE_SECONDS;
  /**
   * Constructs and configures the simulation.
   *
   * @throws IllegalArgumentException if required system properties are invalid
   */
  public SavedListSearchPagedParallelOpenSimulation() {
    if (TOTAL_USERS_PER_SEC <= 0.0) {
      throw new IllegalArgumentException("totalUsersPerSec must be > 0");
    }
    if (RAMP_SECONDS < 0) {
      throw new IllegalArgumentException("rampSeconds must be >= 0");
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

    // Split total arrival rate approximately 50/50 across A and B.
    double perScenarioRps = TOTAL_USERS_PER_SEC / 2.0;

    var scnA = NewSearchPagedLoopScenario.build(
        listId,
        payloadPath,
        "[NEW]",
        SEARCH_REPEATS,
        SEARCH_MIN_PAUSE_SECONDS,
        SEARCH_MAX_PAUSE_SECONDS
    );

    var scnB = OldSearchPagedLoopScenario.build(
        listId,
        payloadPath,
        "[OLD]",
        SEARCH_REPEATS,
        SEARCH_MIN_PAUSE_SECONDS,
        SEARCH_MAX_PAUSE_SECONDS
    );

    setUp(
        scnA.injectOpen(
            rampUsersPerSec(0).to(perScenarioRps).during(RAMP_SECONDS),
            constantUsersPerSec(perScenarioRps).during(WARMUP_SECONDS),
            constantUsersPerSec(perScenarioRps).during(MEASURE_SECONDS)
        ),
        scnB.injectOpen(
            rampUsersPerSec(0).to(perScenarioRps).during(RAMP_SECONDS),
            constantUsersPerSec(perScenarioRps).during(WARMUP_SECONDS),
            constantUsersPerSec(perScenarioRps).during(MEASURE_SECONDS)
        )
    )
        .protocols(httpProtocol)
        .maxDuration(Duration.ofSeconds(plannedSeconds + 60L))
        .assertions(defaultAssertions());
  }
}
