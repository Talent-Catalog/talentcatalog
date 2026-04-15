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
import static io.gatling.javaapi.core.CoreDsl.pause;
import static io.gatling.javaapi.core.CoreDsl.randomSwitch;
import static io.gatling.javaapi.core.CoreDsl.scenario;

import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.FeederBuilder.Batchable;
import io.gatling.javaapi.core.ScenarioBuilder;
import org.talentcatalog.perf.chains.AuthChains;
import org.talentcatalog.perf.requests.http.candidatesearch.CandidateSearchRequests;

/**
 * Scenario that models finite "active user" behavior with:
 * <ul>
 *   <li>Random think time between requests</li>
 *   <li>Random A/B choice between the new and old-fetch candidate search endpoints</li>
 * </ul>
 *
 * <p>Each virtual user runs exactly {@code repeats} iterations. For each iteration:
 * <ol>
 *   <li>Pause randomly between {@code minPauseSeconds} and {@code maxPauseSeconds}</li>
 *   <li>Choose NEW vs OLD based on {@code pctNew} (0..100)</li>
 *   <li>Execute the chosen request inside a {@code group(...)} for report breakdown</li>
 * </ol>
 *
 * <p>Authentication is performed once per user via {@link AuthChains#ensureLoggedIn()}.</p>
 */
public final class RandomABFiniteScenario {

  /**
   * Feeder providing login/session variables from {@code users.csv}.
   *
   * <p>Uses {@code circular()} so the simulation can run indefinitely even with a small CSV.</p>
   */
  private static final Batchable<String> USER_FEEDER = csv("users.csv").circular();

  /**
   * Utility class: prevent instantiation.
   */
  private RandomABFiniteScenario() {
  }

  /**
   * Builds the random A/B finite scenario.
   *
   * @param payloadPath     path to the JSON payload resource (e.g.,
   *                        {@code "payloads/candidate_search_light.json"})
   * @param repeats         number of iterations per user (must be {@code > 0})
   * @param minPauseSeconds minimum think-time pause per iteration in seconds (must be
   *                        {@code >= 0})
   * @param maxPauseSeconds maximum think-time pause per iteration in seconds (must be
   *                        {@code >= minPauseSeconds})
   * @param pctNew          percentage of traffic routed to the NEW endpoint (0..100). Old endpoint
   *                        receives {@code 100 - pctNew}.
   * @param labelNew        label appended to the NEW request name for reporting (e.g.,
   *                        {@code "[baseline]"})
   * @param labelOld        label appended to the OLD request name for reporting (e.g.,
   *                        {@code "[baseline]"})
   * @return a configured {@link ScenarioBuilder}
   * @throws IllegalArgumentException if any constraints are invalid
   */
  public static ScenarioBuilder build(
      String payloadPath,
      int repeats,
      int minPauseSeconds,
      int maxPauseSeconds,
      int pctNew,
      String labelNew,
      String labelOld
  ) {
    if (repeats <= 0) {
      throw new IllegalArgumentException("repeats must be > 0");
    }
    if (minPauseSeconds < 0) {
      throw new IllegalArgumentException("minPauseSeconds must be >= 0");
    }
    if (maxPauseSeconds < minPauseSeconds) {
      throw new IllegalArgumentException("maxPauseSeconds must be >= minPauseSeconds");
    }
    if (pctNew < 0 || pctNew > 100) {
      throw new IllegalArgumentException("pctNew must be between 0 and 100");
    }

    var body = RawFileBody(payloadPath);

    double newWeight = (double) pctNew;
    double oldWeight = 100.0 - newWeight;

    return scenario("Candidate Search - Random A/B (Finite)")
        .feed(USER_FEEDER)
        .exec(AuthChains.ensureLoggedIn())
        .repeat(repeats).on(
            pause(minPauseSeconds, maxPauseSeconds)
                .exec(
                    randomSwitch().on(
                        new Choice.WithWeight(
                            newWeight,
                            group("candidate-search-new").on(
                                exec(CandidateSearchRequests.searchNew(body, labelNew))
                            )
                        ),
                        new Choice.WithWeight(
                            oldWeight,
                            group("candidate-search-old-fetch").on(
                                exec(CandidateSearchRequests.searchOldFetch(body, labelOld))
                            )
                        )
                    )
                )
        );
  }
}
