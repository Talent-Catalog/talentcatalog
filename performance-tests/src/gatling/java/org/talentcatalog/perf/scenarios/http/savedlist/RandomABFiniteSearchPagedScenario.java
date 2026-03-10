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

package org.talentcatalog.perf.scenarios.http.savedlist;

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
import org.talentcatalog.perf.requests.http.savedlist.SavedListCandidateSearchPagedRequests;

/**
 * Builds a finite (bounded by {@code repeats}) A/B scenario that:
 * <ol>
 *   <li>Feeds user credentials from {@code users.csv}</li>
 *   <li>Logs in once (idempotent via {@link AuthChains#ensureLoggedIn()})</li>
 *   <li>Repeats {@code repeats} iterations, pausing between {@code minPauseSeconds} and {@code maxPauseSeconds}</li>
 *   <li>On each iteration, routes to <b>NEW</b> vs <b>OLD-FETCH</b> paged saved-list search using a weighted random split</li>
 * </ol>
 *
 * <p>The split is controlled by {@code pctNew} (0..100). The remaining traffic is sent to the legacy
 * old-fetch behavior. This makes the scenario useful for A/B testing and gradual rollout comparisons
 * under identical virtual-user and pause characteristics.</p>
 *
 * <p>The request body is loaded from a classpath resource via {@code RawFileBody(payloadPath)}
 * (typically under {@code src/gatling/resources}).</p>
 */
public final class RandomABFiniteSearchPagedScenario {

  /**
   * Feeder providing login/session variables from {@code users.csv}.
   *
   * <p>Uses {@code circular()} so the simulation can run indefinitely even with a small CSV.</p>
   */
  private static final Batchable<String> USER_FEEDER = csv("users.csv").circular();

  /**
   * Utility class: prevent instantiation.
   */
  private RandomABFiniteSearchPagedScenario() {
  }

  /**
   * Creates the finite random A/B saved-list candidate search (paged) scenario.
   *
   * @param listId          saved-list id to search within
   * @param payloadPath     path to the JSON payload resource (e.g.,
   *                        {@code "payloads/saved_list_search_light.json"})
   * @param repeats         number of iterations (requests) per virtual user (must be {@code > 0})
   * @param minPauseSeconds minimum pause between iterations in seconds (must be {@code >= 0})
   * @param maxPauseSeconds maximum pause between iterations in seconds (must be
   *                        {@code >= minPauseSeconds})
   * @param pctNew          percentage of traffic to route to the new paged search (0..100)
   * @param labelNew        label appended to the NEW request name for reporting (e.g.,
   *                        {@code "[new-50]"})
   * @param labelOld        label appended to the OLD request name for reporting (e.g.,
   *                        {@code "[old-50]"})
   * @return a configured {@link ScenarioBuilder}
   * @throws IllegalArgumentException if the repeat/pause/split constraints are invalid
   */
  public static ScenarioBuilder build(
      long listId,
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
      throw new IllegalArgumentException("pctNew must be 0..100");
    }

    var body = RawFileBody(payloadPath);

    double newWeight = (double) pctNew;
    double oldWeight = 100.0 - newWeight;

    return scenario("Saved List Search Paged - Random A/B (Finite)")
        .feed(USER_FEEDER)
        .exec(AuthChains.ensureLoggedIn())
        .repeat(repeats).on(
            pause(minPauseSeconds, maxPauseSeconds)
                .exec(
                    randomSwitch().on(
                        new Choice.WithWeight(
                            newWeight,
                            group("saved-list-search-paged-new").on(
                                exec(SavedListCandidateSearchPagedRequests.searchPagedNew(listId,
                                    body, labelNew))
                            )
                        ),
                        new Choice.WithWeight(
                            oldWeight,
                            group("saved-list-search-paged-old-fetch").on(
                                exec(SavedListCandidateSearchPagedRequests.searchPagedOldFetch(
                                    listId, body, labelOld))
                            )
                        )
                    )
                )
        );
  }
}
