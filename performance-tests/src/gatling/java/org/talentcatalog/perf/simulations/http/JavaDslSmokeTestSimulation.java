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

package org.talentcatalog.perf.simulations.http;

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
