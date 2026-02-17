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

import static io.gatling.javaapi.core.CoreDsl.global;

import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.util.ArrayList;
import org.talentcatalog.perf.config.HttpProtocolFactory;
import org.talentcatalog.perf.config.PerfConfig;
import org.talentcatalog.perf.payloads.SavedListSearchPagedPayloads;

/**
 * Base class for Saved List candidate search-paged Gatling simulations.
 *
 * <p>This base class centralizes shared setup so individual simulations can focus on
 * scenarios and injection profiles.</p>
 *
 * <h2>Shared responsibilities</h2>
 * <ul>
 *   <li>Resolve the saved-list id from {@code -DlistId=...}</li>
 *   <li>Load performance settings via {@link PerfConfig#settings()}</li>
 *   <li>Build a shared {@link HttpProtocolBuilder} via {@link HttpProtocolFactory}</li>
 *   <li>Resolve the request payload path (currently fixed to a preview payload)</li>
 * </ul>
 *
 * <h2>Saved-list selection</h2>
 * <p>The {@link #PROP_LIST_ID} system property provides the saved list id used by the endpoints:</p>
 * <ul>
 *   <li>{@code -DlistId=1283} → uses list id {@code 1283}</li>
 * </ul>
 *
 * <p>This property is required; the constructor throws if the id is missing or {@code <= 0}.</p>
 *
 * <h2>Payload selection</h2>
 * <p>At present this base class uses a single payload:</p>
 * <ul>
 *   <li>{@link SavedListSearchPagedPayloads#PREVIEW} (default and only option)</li>
 * </ul>
 *
 * <p>This can be extended later to support payload modes (e.g., baseline/heavy) similar to
 * {@code CandidateSearchBaseSimulation}.</p>
 */
public abstract class SavedListSearchPagedBaseSimulation extends Simulation {

  /**
   * System property key used to provide the saved list id.
   *
   * <p>Example: {@code -DlistId=1283}</p>
   */
  protected static final String PROP_LIST_ID = "listId";

  /**
   * Required saved-list id used by saved-list search-paged endpoints.
   */
  protected final long listId;

  /**
   * Resolved classpath resource path for the selected payload JSON.
   */
  protected final String payloadPath;

  /**
   * HTTP protocol configuration shared by all simulations (baseUrl, headers, etc.).
   */
  protected final HttpProtocolBuilder httpProtocol;
  /**
   * Maximum allowed global p95 response time (ms).
   *
   * <p>Controlled via {@code -Dperf.maxP95Ms}.
   *
   * <ul>
   *   <li>Default: {@code 2000} (strict)</li>
   *   <li>Set to {@code 0} to disable latency assertions (useful for CI nightlies)</li>
   * </ul>
   */
  protected static final int MAX_P95_MS =
      (int) getDoubleProp("perf.maxP95Ms", 2000);

  /**
   * Initializes shared simulation configuration:
   * <ul>
   *   <li>Loads {@link org.talentcatalog.perf.config.PerfSettings} via {@link PerfConfig#settings()}</li>
   *   <li>Builds {@link #httpProtocol}</li>
   *   <li>Resolves {@link #listId} from {@code -DlistId}</li>
   *   <li>Resolves {@link #payloadPath} (currently {@link SavedListSearchPagedPayloads#PREVIEW})</li>
   * </ul>
   *
   * @throws IllegalArgumentException if {@code -DlistId} is missing or {@code <= 0}
   */
  protected SavedListSearchPagedBaseSimulation() {
    var settings = PerfConfig.settings();
    this.httpProtocol = HttpProtocolFactory.build(settings);

    this.listId = Long.parseLong(System.getProperty(PROP_LIST_ID, "0"));
    if (this.listId <= 0) {
      throw new IllegalArgumentException("listId must be > 0 (use -DlistId=1283)");
    }
    this.payloadPath = SavedListSearchPagedPayloads.PREVIEW;
  }

  /**
   * Reads a numeric system property safely.
   *
   * @param key property name
   * @param def fallback value
   * @return parsed value or fallback
   */
  protected static double getDoubleProp(String key, double def) {
    String v = System.getProperty(key);
    if (v == null || v.isBlank()) return def;
    try {
      return Double.parseDouble(v.trim());
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Builds the default global assertion set for saved-list simulations.
   *
   * <p>Always enforces correctness (failed request percentage).
   *
   * <p>Latency (p95) enforcement is optional and can be disabled with:
   *
   * <pre>
   *   -Dperf.maxP95Ms=0
   * </pre>
   *
   * @return assertion array for Gatling {@code setUp(...)}
   */
  protected static Assertion[] defaultAssertions() {
    var assertions = new ArrayList<Assertion>();

    // Always enforce correctness
    assertions.add(global().failedRequests().percent().lt(1.0));

    // Optional latency gate
    if (MAX_P95_MS > 0) {
      assertions.add(global().responseTime().percentile3().lt(MAX_P95_MS));
    }

    return assertions.toArray(new Assertion[0]);
  }

}
