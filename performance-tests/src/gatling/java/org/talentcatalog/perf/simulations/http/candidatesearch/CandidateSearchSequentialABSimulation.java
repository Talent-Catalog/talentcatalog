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

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;

import org.talentcatalog.perf.scenarios.http.candidatesearch.SequentialABScenario;

/**
 * Runs a sequential A/B comparison per virtual user (NEW then OLD-FETCH).
 *
 * <h2>Behavior</h2>
 * <p>Each virtual user logs in once and then performs {@code seqRepeats} iterations. For each
 * iteration:</p>
 * <ol>
 *   <li>Execute candidate search against the <b>new</b> endpoint</li>
 *   <li>Execute candidate search against the <b>old-fetch</b> endpoint</li>
 * </ol>
 *
 * <p>This simulation is useful when you want per-user, per-iteration comparisons under the same
 * session/token and similar conditions (A then B back-to-back).</p>
 *
 * <h2>System properties</h2>
 * <ul>
 *   <li>{@code -Dperf.baseUrl} (required) – target base URL</li>
 *   <li>{@code -Dpayload=baseline|heavy} (default: heavy) – resolved by {@link CandidateSearchBaseSimulation}</li>
 *   <li>{@code -DseqUsers} (default: 1) – number of users started immediately via {@code atOnceUsers}</li>
 *   <li>{@code -DseqRepeats} (default: 1) – number of sequential A/B iterations per user</li>
 * </ul>
 *
 * <h2>How to run (Gradle)</h2>
 * <pre>{@code
 * ./gradlew :performance-tests:gatlingTest \
 *   -PsimClass=org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchSequentialABSimulation \
 *   -Dperf.baseUrl=https://YOUR_ENV_HOST \
 *   -Dpayload=baseline \
 *   -DseqUsers=5 \
 *   -DseqRepeats=50
 * }</pre>
 */
public class CandidateSearchSequentialABSimulation extends CandidateSearchBaseSimulation {

  /**
   * Number of users started immediately. Default: 1. Required: {@code > 0}.
   */
  private static final int SEQ_USERS = Integer.getInteger("seqUsers", 1);

  /**
   * Number of sequential A/B iterations per user. Default: 1. Required: {@code > 0}.
   */
  private static final int SEQ_REPEATS = Integer.getInteger("seqRepeats", 1);

  /**
   * Constructs and configures the simulation.
   *
   * @throws IllegalArgumentException if required system properties are invalid
   */
  public CandidateSearchSequentialABSimulation() {
    if (SEQ_USERS <= 0) {
      throw new IllegalArgumentException("seqUsers must be > 0");
    }
    if (SEQ_REPEATS <= 0) {
      throw new IllegalArgumentException("seqRepeats must be > 0");
    }

    var scn = SequentialABScenario.build(payloadPath, SEQ_REPEATS);

    setUp(scn.injectOpen(atOnceUsers(SEQ_USERS)))
        .protocols(httpProtocol)
        .assertions(defaultAssertions());
  }
}
