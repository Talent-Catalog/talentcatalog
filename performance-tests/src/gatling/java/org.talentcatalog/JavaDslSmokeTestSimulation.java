package org.talentcatalog;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;


// NOTE: This simulation is intentionally minimal and only exists to verify
// that Gatling Java DSL runs correctly and produces reports.
// The target endpoint is a placeholder and will be replaced with a proper
// TalentCatalog endpoint once performance tests are finalized.
public class JavaDslSmokeTestSimulation extends Simulation {

  private final HttpProtocolBuilder httpProtocol = http
      .baseUrl("https://tctalent-test.org")
      .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      .disableCaching()
      .disableWarmUp()
      .disableFollowRedirect();

  {
    var scn = scenario("Admin Portal Smoke Test")
        .exec(
            http("GET /admin-portal")
                .get("/admin-portal")
                // accept this as "reachable"
                .check(status().in(200))
        );

    setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
