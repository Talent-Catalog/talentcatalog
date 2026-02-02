package org.talentcatalog.perf.scenarios.http.candidatesearch;

/**
 * Centralized classpath resource paths for Candidate Search simulation payloads.
 *
 * <p>These constants point to JSON files packaged on the Gatling classpath
 * (typically under {@code src/gatling/resources}). Simulations can use them to load request bodies
 * consistently across scenarios and to make report comparisons clearer.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * String baselineJson = Files.readString(Paths.get(ClassLoader.getSystemResource(CandidateSearchPayloads.BASELINE).toURI()));
 * }</pre>
 *
 * <p>(Exact loading approach depends on your existing helpers; some projects use Gatling
 * {@code RawFileBody}
 * or a custom file loader.)</p>
 */
public final class CandidateSearchPayloads {

  /**
   * Baseline/light candidate search payload.
   *
   * <p>Intended for "typical" request sizes to represent common usage patterns.</p>
   */
  public static final String BASELINE = "payloads/candidate_search_light.json";

  /**
   * Heavy candidate search payload.
   *
   * <p>Intended for stress testing larger queries and exercising more expensive search paths.</p>
   */
  public static final String HEAVY = "payloads/candidate_search_heavy.json";

  /**
   * Utility class: prevent instantiation.
   */
  private CandidateSearchPayloads() {
  }
}
