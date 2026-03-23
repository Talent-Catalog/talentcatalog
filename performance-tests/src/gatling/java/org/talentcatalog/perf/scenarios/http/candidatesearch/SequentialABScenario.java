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
 * Scenario that executes A then B sequentially per iteration for the same virtual user.
 *
 * <p>Each iteration performs:
 * <ol>
 *   <li>Candidate search against the <b>new</b> endpoint</li>
 *   <li>Candidate search against the <b>old-fetch</b> endpoint</li>
 * </ol>
 *
 * <p>This is useful when you want per-user, per-iteration comparisons (A then B) under the same
 * session/token and similar timing conditions.</p>
 *
 * <p>Authentication is performed once per user via {@link AuthChains#ensureLoggedIn()}.</p>
 */
public final class SequentialABScenario {

  /**
   * Feeder providing login/session variables from {@code users.csv}.
   *
   * <p>Uses {@code circular()} so the simulation can run indefinitely even with a small CSV.</p>
   */
  private static final Batchable<String> USER_FEEDER = csv("users.csv").circular();

  /**
   * Utility class: prevent instantiation.
   */
  private SequentialABScenario() {
  }

  /**
   * Builds the sequential A/B scenario.
   *
   * @param payloadPath path to the JSON payload resource (e.g.,
   *                    {@code "payloads/candidate_search_light.json"})
   * @param repeats     number of iterations per user (must be {@code > 0})
   * @return a configured {@link ScenarioBuilder}
   * @throws IllegalArgumentException if {@code repeats <= 0}
   */
  public static ScenarioBuilder build(String payloadPath, int repeats) {
    if (repeats <= 0) {
      throw new IllegalArgumentException("repeats must be > 0");
    }

    var body = RawFileBody(payloadPath);

    return scenario("Candidate Search - Sequential A/B")
        .feed(USER_FEEDER)
        .exec(AuthChains.ensureLoggedIn())
        .repeat(repeats).on(
            exec(
                group("candidate-search-new").on(
                    exec(CandidateSearchRequests.searchNew(body, "[NEW]"))
                )
            ).exec(
                group("candidate-search-old-fetch").on(
                    exec(CandidateSearchRequests.searchOldFetch(body, "[OLD]"))
                )
            )
        );
  }
}
