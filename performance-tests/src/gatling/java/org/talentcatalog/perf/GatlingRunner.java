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

package org.talentcatalog.perf;

import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;
import java.util.Optional;
import scala.collection.mutable.Map;

/**
 * Programmatic entry point to run Gatling simulations from an IDE (IntelliJ "Run"/green arrow)
 * or any JVM launcher (java command, Gradle/Maven exec, etc.).
 *
 * <p>This runner starts Gatling via {@link Gatling#fromMap(Map)}} using
 * {@link GatlingPropertiesBuilder}, so you can run simulations without relying on Gradle/Maven
 * Gatling tasks.</p>
 *
 * <h2>Simulation selection priority</h2>
 * <ol>
 *   <li><b>Program argument 0</b>: fully-qualified simulation class name</li>
 *   <li><b>System property</b>: {@code -DsimClass=...}</li>
 *   <li><b>Default</b>: {@link #DEFAULT_SIMULATION_CLASS}</li>
 * </ol>
 *
 * <h2>Results folder</h2>
 * <p>By default results are written to {@link #DEFAULT_RESULTS_FOLDER}. Override with:</p>
 * <pre>{@code -Dgatling.resultsFolder=build/reports/gatling}</pre>
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // 1) IntelliJ Program arguments (recommended):
 * org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchSequentialABSimulation
 *
 * // 2) IntelliJ VM options:
 * -DsimClass=org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchSequentialABSimulation
 * -Dgatling.resultsFolder=build/reports/gatling
 *
 * // 3) Command line:
 * java -cp <classpath> org.talentcatalog.perf.GatlingRunner \
 *   org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchSequentialABSimulation
 * }</pre>
 */
public class GatlingRunner {

  /**
   * Default simulation to run when neither a program argument nor {@code -DsimClass} is provided.
   */
  public static final String DEFAULT_SIMULATION_CLASS =
      "org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchSequentialABSimulation";

  /**
   * System property key for selecting the simulation class.
   * <p>Example: {@code -DsimClass=org.talentcatalog.perf.simulations.http.SomeSimulation}</p>
   */
  public static final String SIM_CLASS_PROPERTY = "simClass";

  /**
   * System property key for overriding Gatling's results directory.
   * <p>Example: {@code -Dgatling.resultsFolder=build/reports/gatling}</p>
   */
  public static final String RESULTS_FOLDER_PROPERTY = "gatling.resultsFolder";

  /** Default results directory used when {@link #RESULTS_FOLDER_PROPERTY} is not provided. */
  public static final String DEFAULT_RESULTS_FOLDER = "build/reports/gatling";

  /**
   * Launches Gatling with the selected simulation and results directory.
   *
   * @param args optional program args; if {@code args[0]} is present and non-blank,
   *             it is treated as the fully-qualified simulation class name.
   */
  public static void main(String[] args) {

    // Priority:
    // 1) Program arg
    // 2) -DsimClass=...
    // 3) Default
    String simClass =
        (args.length > 0 && args[0] != null && !args[0].isBlank())
            ? args[0].trim()
            : Optional.ofNullable(System.getProperty(SIM_CLASS_PROPERTY))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .orElse(DEFAULT_SIMULATION_CLASS);

    // Results folder:
    // -Dgatling.resultsFolder=...
    String resultsFolder =
        Optional.ofNullable(System.getProperty(RESULTS_FOLDER_PROPERTY))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .orElse(DEFAULT_RESULTS_FOLDER);

    GatlingPropertiesBuilder props =
        new GatlingPropertiesBuilder()
            .simulationClass(simClass)
            .resultsDirectory(resultsFolder);

    Gatling.fromMap(props.build());
  }
}
