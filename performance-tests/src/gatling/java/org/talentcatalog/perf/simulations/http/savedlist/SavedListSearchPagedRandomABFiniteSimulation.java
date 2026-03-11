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

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;

import org.talentcatalog.perf.scenarios.http.savedlist.RandomABFiniteSearchPagedScenario;

/**
 * Runs a finite "active user" workload that randomly routes each iteration to the NEW or OLD-FETCH
 * saved-list candidate search (paged) implementation.
 *
 * <h2>Behavior</h2>
 * <p>Each virtual user performs exactly {@code randRepeats} iterations. For each iteration:</p>
 * <ol>
 *   <li>Pause randomly between {@code randMinPauseSeconds} and {@code randMaxPauseSeconds}</li>
 *   <li>Choose NEW vs OLD-FETCH based on {@code randPctNew} (0..100)</li>
 *   <li>Execute the chosen saved-list search-paged request</li>
 * </ol>
 *
 * <p>This simulation is useful for modeling a finite burst of users with mixed traffic, while keeping
 * per-user behavior bounded (exactly {@code randRepeats} loops).</p>
 *
 * <h2>System properties</h2>
 * <ul>
 *   <li>{@code -Dperf.baseUrl} (required) – target base URL</li>
 *   <li>{@code -DlistId} (required, &gt; 0) – resolved by {@link SavedListSearchPagedBaseSimulation}</li>
 *   <li>{@code -DrandUsers} (required, &gt; 0) – number of users started immediately via {@code atOnceUsers}</li>
 *   <li>{@code -DrandRepeats} (default: 100) – iterations per user</li>
 *   <li>{@code -DrandMinPauseSeconds} (default: 1)</li>
 *   <li>{@code -DrandMaxPauseSeconds} (default: 10)</li>
 *   <li>{@code -DrandPctNew} (default: 50) – percent of iterations routed to NEW (OLD gets {@code 100 - randPctNew})</li>
 * </ul>
 *
 * <h2>How to run (Gradle)</h2>
 * <pre>{@code
 * ./gradlew :performance-tests:gatlingTest \
 *   -PsimClass=org.talentcatalog.perf.simulations.http.savedlist.SavedListSearchPagedRandomABFiniteSimulation \
 *   -Dperf.baseUrl=https://YOUR_ENV_HOST \
 *   -DlistId=1283 \
 *   -DrandUsers=100 \
 *   -DrandRepeats=100 \
 *   -DrandMinPauseSeconds=1 \
 *   -DrandMaxPauseSeconds=10 \
 *   -DrandPctNew=50
 * }</pre>
 */
public class SavedListSearchPagedRandomABFiniteSimulation extends
    SavedListSearchPagedBaseSimulation {

  /**
   * Number of users started immediately. Required: {@code > 0}.
   */
  private static final int RAND_USERS = Integer.getInteger("randUsers", 0);

  /**
   * Number of iterations per user. Default: 100.
   */
  private static final int RAND_REPEATS = Integer.getInteger("randRepeats", 100);

  /**
   * Minimum think time per iteration in seconds. Default: 1.
   */
  private static final int RAND_MIN_PAUSE_SECONDS = Integer.getInteger("randMinPauseSeconds", 1);

  /**
   * Maximum think time per iteration in seconds. Default: 10.
   */
  private static final int RAND_MAX_PAUSE_SECONDS = Integer.getInteger("randMaxPauseSeconds", 10);

  /**
   * Percent of iterations routed to the NEW implementation (0..100). Default: 50.
   */
  private static final int RAND_PCT_NEW = Integer.getInteger("randPctNew", 50);

  /**
   * Constructs and configures the simulation.
   *
   * @throws IllegalArgumentException if required system properties are invalid
   */
  public SavedListSearchPagedRandomABFiniteSimulation() {
    if (RAND_USERS <= 0) {
      throw new IllegalArgumentException("randUsers must be > 0");
    }
    if (RAND_REPEATS <= 0) {
      throw new IllegalArgumentException("randRepeats must be > 0");
    }
    if (RAND_MIN_PAUSE_SECONDS < 0) {
      throw new IllegalArgumentException("randMinPauseSeconds must be >= 0");
    }
    if (RAND_MAX_PAUSE_SECONDS < RAND_MIN_PAUSE_SECONDS) {
      throw new IllegalArgumentException("randMaxPauseSeconds must be >= randMinPauseSeconds");
    }
    if (RAND_PCT_NEW < 0 || RAND_PCT_NEW > 100) {
      throw new IllegalArgumentException("randPctNew must be 0..100");
    }

    var scn = RandomABFiniteSearchPagedScenario.build(
        listId,
        payloadPath,
        RAND_REPEATS,
        RAND_MIN_PAUSE_SECONDS,
        RAND_MAX_PAUSE_SECONDS,
        RAND_PCT_NEW,
        "[RAND_NEW]",
        "[RAND_OLD]"
    );

    setUp(scn.injectOpen(atOnceUsers(RAND_USERS)))
        .protocols(httpProtocol)
        .assertions(defaultAssertions());
  }
}
