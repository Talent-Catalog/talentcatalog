package org.talentcatalog

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import org.talentcatalog.NewSearchScreenQuery.longQuery
import ru.tinkoff.load.jdbc.Predef._
import ru.tinkoff.load.jdbc.actions.actions.RawSqlActionBuilder
import ru.tinkoff.load.jdbc.protocol.JdbcProtocolBuilder

import scala.concurrent.duration._
import scala.language.postfixOps

class PostgresLoadTest extends Simulation {

  val dataBase: JdbcProtocolBuilder = DB
    .url("jdbc:postgresql://localhost:5432/tbbtalent")
    .username("")
    .password("")
    .maximumPoolSize(23)
    .connectionTimeout(2.minute)

  def selectCandidates(): RawSqlActionBuilder =
    jdbc("Select Candidates")
      .rawSql("select * from Candidate c where c.status in ('active', 'unreachable', 'incomplete', 'pending')")

  def newSearchScreenQuery(): RawSqlActionBuilder =
    jdbc("New Search Screen Query")
      .rawSql(longQuery)

  // stats

  val scn: ScenarioBuilder = scenario("PostgresLoadTest")
    .repeat(50) (
      exec(selectCandidates())
        .exec(newSearchScreenQuery())
    )

  setUp(
    scn.inject(rampUsers(20) during (10 seconds))
  ).protocols(dataBase)
    .maxDuration(120)
    .assertions(
      global.failedRequests.percent.is(0.0),
      global.successfulRequests.percent.is(100),
      details("Select Candidates").responseTime.percentile1.lt(150), // 50th percentile < 150ms
      details("Select Candidates").responseTime.percentile2.lt(200), // 75th percentile < 200ms
      details("Select Candidates").responseTime.percentile3.lt(300), // 95th percentile < 300ms
      details("Select Candidates").responseTime.percentile4.lt(500), // 99th percentile < 500ms
      details("Select Candidates").responseTime.max.lt(800) // max < 800ms
  )

}
