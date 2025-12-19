/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.talentcatalog

import io.gatling.core.Predef._
import org.talentcatalog.queries.EsLoadQuery.{esLoadCountQueryName, esLoadPagedQueryName}
import org.talentcatalog.queries.NewSearchScreenQuery.newSearchScreenQueryName
import org.talentcatalog.scenarios.EsLoadScenario.esLoadScenario
import org.talentcatalog.scenarios.NewSearchScreenScenario.newSearchScreenScenario
import ru.tinkoff.load.jdbc.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps

class PostgresLoadTest extends JdbcBaseSimulation {

  setUp(
    newSearchScreenScenario.inject(
      nothingFor(1 second),
      atOnceUsers(1),
      nothingFor(1 minutes),
      rampUsers(20) during (5 minutes)
    ).andThen(
      esLoadScenario.inject(
        nothingFor(1 second),
        atOnceUsers(1)
      )
    )
  ).protocols(dataBase)
    .maxDuration(35 minutes) // Set a maximum duration
    .assertions(
      global.failedRequests.percent.is(0.0),
      global.successfulRequests.percent.is(100),

      details(newSearchScreenQueryName).responseTime.percentile1.lt(500),
      details(newSearchScreenQueryName).responseTime.percentile2.lt(750),
      details(newSearchScreenQueryName).responseTime.percentile3.lt(1000),
      details(newSearchScreenQueryName).responseTime.percentile4.lt(2000),
      details(newSearchScreenQueryName).responseTime.max.lt(3000),

      details(esLoadPagedQueryName).responseTime.percentile1.lt(150),
      details(esLoadPagedQueryName).responseTime.percentile2.lt(200),
      details(esLoadPagedQueryName).responseTime.percentile3.lt(300),
      details(esLoadPagedQueryName).responseTime.percentile4.lt(500),
      details(esLoadPagedQueryName).responseTime.max.lt(800),

      details(esLoadCountQueryName).responseTime.percentile1.lt(150),
      details(esLoadCountQueryName).responseTime.percentile2.lt(200),
      details(esLoadCountQueryName).responseTime.percentile3.lt(300),
      details(esLoadCountQueryName).responseTime.percentile4.lt(500),
      details(esLoadCountQueryName).responseTime.max.lt(800)
    )
}
