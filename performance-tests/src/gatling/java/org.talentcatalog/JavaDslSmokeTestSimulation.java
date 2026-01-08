package org.talentcatalog;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class JavaDslSmokeTestSimulation extends Simulation {

  // Minimal HTTP protocol so we can send a request and generate reports
  private final HttpProtocolBuilder httpProtocol = http
      .baseUrl("https://httpbin.org")
      .acceptHeader("application/json");

  {
    var scn = scenario("Java DSL Smoke Test Scenario")
        .exec(session -> {
          return session;
        })
        // One real request so Gatling produces stats + HTML reports
        .exec(
            http("GET /get")
                .get("/get")
                .check(status().is(200))
        )
        .pause(1);

    setUp(
        scn.injectOpen(atOnceUsers(1))
    ).protocols(httpProtocol);
  }
}
