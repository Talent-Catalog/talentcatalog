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

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.talentcatalog.perf.config.HttpProtocolFactory;
import org.talentcatalog.perf.config.PerfConfig;
import org.talentcatalog.perf.payloads.CandidateSearchPayloads;

/**
 * Base class for Candidate Search Gatling simulations.
 *
 * <p>This base class centralizes shared setup so individual simulations can focus on
 * scenarios and injection profiles.</p>
 *
 * <h2>Shared responsibilities</h2>
 * <ul>
 *   <li>Resolve the candidate search payload mode from {@code -Dpayload=...}</li>
 *   <li>Load performance settings via {@link PerfConfig#settings()}</li>
 *   <li>Build a shared {@link HttpProtocolBuilder} via {@link HttpProtocolFactory}</li>
 * </ul>
 *
 * <h2>Payload selection</h2>
 * <p>The {@link #PROP_PAYLOAD} system property selects which request payload file to use:</p>
 * <ul>
 *   <li>{@code -Dpayload=baseline} → {@link CandidateSearchPayloads#BASELINE}</li>
 *   <li>{@code -Dpayload=heavy} → {@link CandidateSearchPayloads#HEAVY} (default)</li>
 * </ul>
 *
 * <p>If an unknown value is provided, this class falls back to {@code heavy}.</p>
 */
public abstract class CandidateSearchBaseSimulation extends Simulation {

  /**
   * System property key used to choose the candidate-search payload mode.
   * <p>Allowed values: {@code baseline}, {@code heavy}. Default: {@code heavy}.</p>
   */
  protected static final String PROP_PAYLOAD = "payload";

  /**
   * Resolved classpath resource path for the selected payload JSON.
   */
  protected final String payloadPath;

  /**
   * HTTP protocol configuration shared by all simulations (baseUrl, headers, etc.).
   */
  protected final HttpProtocolBuilder httpProtocol;

  /**
   * Initializes shared simulation configuration:
   * <ul>
   *   <li>Loads {@link org.talentcatalog.perf.config.PerfSettings} via {@link PerfConfig#settings()}</li>
   *   <li>Builds {@link #httpProtocol}</li>
   *   <li>Resolves {@link #payloadPath}</li>
   * </ul>
   */
  protected CandidateSearchBaseSimulation() {
    var settings = PerfConfig.settings();
    this.httpProtocol = HttpProtocolFactory.build(settings);
    this.payloadPath = resolvePayloadPath();
  }

  /**
   * Resolves the payload JSON file path from {@code -Dpayload}.
   *
   * <p>Trims and lowercases the property value. Defaults to {@code heavy} when missing or
   * blank.</p>
   *
   * @return the classpath resource path for the selected payload
   */
  private static String resolvePayloadPath() {
    String mode = System.getProperty(PROP_PAYLOAD, "heavy").trim().toLowerCase();
    return switch (mode) {
      case "baseline" -> CandidateSearchPayloads.BASELINE;
      case "heavy" -> CandidateSearchPayloads.HEAVY;
      default -> CandidateSearchPayloads.HEAVY; // fallback for unknown values
    };
  }
}
