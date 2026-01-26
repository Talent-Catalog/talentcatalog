package org.talentcatalog.perf;

import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;
import java.util.Optional;
import scala.collection.mutable.Map;

/**
 * Entry-point runner for executing Gatling simulations from IntelliJ (green arrow) or any JVM launcher.
 *
 * <p>This runner starts Gatling programmatically using {@link Gatling#fromMap(Map)} (java.util.Map)} and
 * {@link GatlingPropertiesBuilder}, so we can run either Java DSL or Scala simulations without
 * invoking Gradle tasks.</p>
 *
 * <h2>Simulation selection priority</h2>
 * <ol>
 *   <li><b>Program argument 0</b>: a fully-qualified simulation class name</li>
 *   <li><b>System property</b>: {@code -DsimClass=...}</li>
 *   <li><b>Default</b>: {@link #DEFAULT_SIMULATION_CLASS}</li>
 * </ol>
 *
 * <h2>Reports / results folder</h2>
 * <p>By default results are written to {@code build/reports/gatling}. We can override this with
 * {@code -Dgatling.resultsFolder=...}.</p>
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // 1) IntelliJ Program arguments (recommended):
 * org.talentcatalog.perf.simulations.http.JavaDslSmokeTestSimulation
 *
 * // 2) IntelliJ VM options:
 * -DsimClass=org.talentcatalog.perf.simulations.http.JavaDslSmokeTestSimulation
 * -Dgatling.resultsFolder=build/reports/gatling
 *
 * // 3) Command line:
 * java -cp <classpath> org.talentcatalog.perf.GatlingRunner \
 *   org.talentcatalog.perf.simulations.http.JavaDslSmokeTestSimulation
 * }</pre>
 */
public class GatlingRunner {

  /**
   * Default simulation to run when neither a program argument nor {@code -DsimClass} is provided.
   */
  public static final String DEFAULT_SIMULATION_CLASS =
      "org.talentcatalog.perf.simulations.http.JavaDslSmokeTestSimulation";

  /**
   * System property key used to specify the simulation class name.
   * <p>Example: {@code -DsimClass=org.talentcatalog.perf.simulations.http.JavaDslSmokeTestSimulation}</p>
   */
  public static final String SIM_CLASS_PROPERTY = "simClass";

  /**
   * System property key used to override the Gatling results directory.
   * <p>Example: {@code -Dgatling.resultsFolder=build/reports/gatling}</p>
   */
  public static final String RESULTS_FOLDER_PROPERTY = "gatling.resultsFolder";

  /**
   * Default results directory used when {@link #RESULTS_FOLDER_PROPERTY} is not provided.
   */
  public static final String DEFAULT_RESULTS_FOLDER = "build/reports/gatling";

  /**
   * Launches Gatling with the selected simulation class and results directory.
   *
   * @param args Optional program arguments. If {@code args[0]} is present and non-blank,
   *             it is treated as the fully-qualified simulation class name.
   */
  public static void main(String[] args) {

    // Priority:
    // 1) Program argument (fully qualified class name)
    // 2) -DsimClass=...
    // 3) Default
    String simClass =
        (args.length > 0 && args[0] != null && !args[0].isBlank())
            ? args[0]
            : Optional.ofNullable(System.getProperty(SIM_CLASS_PROPERTY))
                .orElse(DEFAULT_SIMULATION_CLASS);

    // Results folder:
    // -Dgatling.resultsFolder=...
    String resultsFolder =
        Optional.ofNullable(System.getProperty(RESULTS_FOLDER_PROPERTY))
            .orElse(DEFAULT_RESULTS_FOLDER);

    GatlingPropertiesBuilder props = new GatlingPropertiesBuilder()
        .simulationClass(simClass)
        .resultsDirectory(resultsFolder);

    Gatling.fromMap(props.build());
  }
}
