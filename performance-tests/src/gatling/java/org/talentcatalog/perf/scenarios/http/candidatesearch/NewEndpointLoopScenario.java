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
package org.talentcatalog.perf.scenarios.http.candidatesearch;

import static io.gatling.javaapi.core.CoreDsl.RawFileBody;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.scenario;

import io.gatling.javaapi.core.FeederBuilder.Batchable;
import io.gatling.javaapi.core.ScenarioBuilder;
import org.talentcatalog.perf.chains.AuthChains;
import org.talentcatalog.perf.requests.http.candidatesearch.CandidateSearchRequests;

/**
 * Builds a scenario that:
 * <ol>
 *   <li>Feeds user credentials from {@code users.csv}</li>
 *   <li>Logs in once (idempotent via {@link AuthChains#ensureLoggedIn()})</li>
 *   <li>Repeats {@code repeats} candidate-search requests against the <b>new</b> endpoint</li>
 *   <li>Pauses a random duration between {@code minPauseSeconds} and {@code maxPauseSeconds} between iterations</li>
 * </ol>
 *
 * <p>This scenario is useful for isolating endpoint performance by amortizing authentication overhead
 * (login happens once per virtual user, then searches loop).</p>
 *
 * <p>The request body is loaded from a classpath resource via {@code RawFileBody(payloadPath)}
 * (typically under {@code src/gatling/resources}).</p>
 */
public final class NewEndpointLoopScenario {

  /**
   * Feeder providing login/session variables from {@code users.csv}.
   *
   * <p>Uses {@code circular()} so the simulation can run indefinitely even with a small CSV.</p>
   */
  private static final Batchable<String> USER_FEEDER = csv("users.csv").circular();

  /** Utility class: prevent instantiation. */
  private NewEndpointLoopScenario() {}

  /**
   * Creates the "new endpoint" candidate search loop scenario.
   *
   * @param payloadPath path to the JSON payload resource (e.g., {@code "payloads/candidate_search_light.json"})
   * @param label label appended to the request name for reporting (e.g., {@code "[baseline]"} or {@code "[heavy]"})
   * @param repeats number of searches to execute per virtual user (must be {@code > 0})
   * @param minPauseSeconds minimum pause between searches in seconds (must be {@code >= 0})
   * @param maxPauseSeconds maximum pause between searches in seconds (must be {@code >= minPauseSeconds})
   * @return a configured {@link ScenarioBuilder}
   * @throws IllegalArgumentException if the repeat/pause constraints are invalid
   */
  public static ScenarioBuilder build(
      String payloadPath,
      String label,
      int repeats,
      int minPauseSeconds,
      int maxPauseSeconds
  ) {
    if (repeats <= 0) throw new IllegalArgumentException("repeats must be > 0");
    if (minPauseSeconds < 0) throw new IllegalArgumentException("minPauseSeconds must be >= 0");
    if (maxPauseSeconds < minPauseSeconds) {
      throw new IllegalArgumentException("maxPauseSeconds must be >= minPauseSeconds");
    }

    var body = RawFileBody(payloadPath);

    return scenario("Candidate Search - NEW (Loop)")
        .feed(USER_FEEDER)
        .exec(AuthChains.ensureLoggedIn())
        .exec(session -> session.set("searchBodyPath", payloadPath))
        .repeat(repeats).on(
            group("candidate-search-new").on(
                    exec(CandidateSearchRequests.searchNew(body, label))
                )
                .pause(minPauseSeconds, maxPauseSeconds)
        );
  }
}
