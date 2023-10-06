/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import com.typesafe.config._
import io.gatling.core.Predef._
import ru.tinkoff.load.jdbc.Predef._
import ru.tinkoff.load.jdbc.protocol.JdbcProtocolBuilder

import scala.concurrent.duration._
import scala.language.postfixOps

class JdbcBaseSimulation extends Simulation {

  val config: Config = ConfigFactory.load()

  val connectionTimeout: FiniteDuration =
    config.getDuration("db.connectionTimeout").toSeconds.seconds

  val dataBase: JdbcProtocolBuilder = DB
    .url(config.getString("db.url"))
    .username(config.getString("db.username"))
    .password(config.getString("db.password"))
    .maximumPoolSize(config.getInt("db.maximumPoolSize"))
    .connectionTimeout(connectionTimeout)

}
