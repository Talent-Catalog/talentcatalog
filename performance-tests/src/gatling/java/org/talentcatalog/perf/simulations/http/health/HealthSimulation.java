package org.talentcatalog.perf.simulations.http.health;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.nothingFor;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class HealthSimulation extends Simulation {

  private static String sys(String key, String def) {
    String v = System.getProperty(key);
    return (v == null || v.isBlank()) ? def : v;
  }

  private static int sysInt(String key, int def) {
    try {
      return Integer.parseInt(System.getProperty(key, String.valueOf(def)));
    } catch (Exception e) {
      return def;
    }
  }

  public HealthSimulation() {
    // Optional: -Dperf.baseUrl=https://... (defaults to http://localhost:8080 for local runs)
    String baseUrl = sys("perf.baseUrl", "http://localhost:8080");

    // Optional: -Dperf.healthPath=/api/health
    String healthPath = sys("perf.healthPath", "/actuator/health");

    // Optional tuning (keep tiny for smoke)
    int warmupSeconds = sysInt("warmupSeconds", 2);
    int measureSeconds = sysInt("measureSeconds", 8);
    int users = sysInt("totalConcurrentUsers", 1);

    HttpProtocolBuilder httpProtocol =
        http.baseUrl(baseUrl)
            .acceptHeader("application/json");

    var scn =
        scenario("Health Check")
            .exec(
                http("GET health")
                    .get(healthPath)
                    .check(status().in(200, 204))
            )
            .pause(1);

    setUp(
        scn.injectOpen(
            nothingFor(warmupSeconds),
            constantUsersPerSec(users).during(measureSeconds)
        )
    ).protocols(httpProtocol);
  }
}
