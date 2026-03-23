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
import static io.gatling.javaapi.core.CoreDsl.scenario;

import io.gatling.javaapi.core.FeederBuilder.Batchable;
import io.gatling.javaapi.core.ScenarioBuilder;
import org.talentcatalog.perf.chains.AuthChains;
import org.talentcatalog.perf.requests.http.savedlist.SavedListCandidateSearchPagedRequests;

/**
 * Builds a sequential A/B scenario that:
 * <ol>
 *   <li>Feeds user credentials from {@code users.csv}</li>
 *   <li>Logs in once (idempotent via {@link AuthChains#ensureLoggedIn()})</li>
 *   <li>Repeats {@code repeats} iterations</li>
 *   <li>On each iteration, executes the <b>NEW</b> paged saved-list search followed immediately by the <b>OLD-FETCH</b> search</li>
 * </ol>
 *
 * <p>This scenario is useful for back-to-back comparisons where you want both implementations to run under the
 * same virtual-user session and request cadence. Because both requests run sequentially inside the same repeat
 * block, it can help reduce noise from user/session variability when comparing behavior and performance.</p>
 *
 * <p>The request body is loaded from a classpath resource via {@code RawFileBody(payloadPath)}
 * (typically under {@code src/gatling/resources}).</p>
 *
 * <p>Labels are fixed as {@code "[NEW]"} and {@code "[OLD]"} to make the two request types easy to distinguish
 * in Gatling reports.</p>
 */
public final class SequentialABSearchPagedScenario {

  /**
   * Feeder providing login/session variables from {@code users.csv}.
   *
   * <p>Uses {@code circular()} so the simulation can run indefinitely even with a small CSV.</p>
   */
  private static final Batchable<String> USER_FEEDER = csv("users.csv").circular();

  /**
   * Utility class: prevent instantiation.
   */
  private SequentialABSearchPagedScenario() {
  }

  /**
   * Creates the sequential A/B saved-list candidate search (paged) scenario.
   *
   * @param listId      saved-list id to search within
   * @param payloadPath path to the JSON payload resource (e.g.,
   *                    {@code "payloads/saved_list_search_light.json"})
   * @param repeats     number of iterations per virtual user (must be {@code > 0})
   * @return a configured {@link ScenarioBuilder}
   * @throws IllegalArgumentException if {@code repeats <= 0}
   */
  public static ScenarioBuilder build(long listId, String payloadPath, int repeats) {
    if (repeats <= 0) {
      throw new IllegalArgumentException("repeats must be > 0");
    }

    var body = RawFileBody(payloadPath);

    return scenario("Saved List Search Paged - Sequential A/B")
        .feed(USER_FEEDER)
        .exec(AuthChains.ensureLoggedIn())
        .repeat(repeats).on(
            exec(
                group("saved-list-search-paged-new").on(
                    exec(
                        SavedListCandidateSearchPagedRequests.searchPagedNew(listId, body, "[NEW]"))
                )
            ).exec(
                group("saved-list-search-paged-old-fetch").on(
                    exec(SavedListCandidateSearchPagedRequests.searchPagedOldFetch(listId, body,
                        "[OLD]"))
                )
            )
        );
  }
}
