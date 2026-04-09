
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

/**
 * Gatling Java DSL {@link io.gatling.javaapi.core.ActionBuilder} that wraps an arbitrary
 * {@link java.util.function.Function} as a Gatling {@link io.gatling.core.action.Action} and logs
 * its execution as a request in Gatling reports.
 *
 * <p>This is useful for representing JDBC (or any non-HTTP) work in a Gatling scenario while still
 * getting timing and OK/KO statistics in the Gatling HTML reports.</p>
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>Records a start timestamp using Gatling's clock.</li>
 *   <li>Executes the provided function {@code fn} with the current {@link io.gatling.core.session.Session}.</li>
 *   <li>On success:
 *     <ul>
 *       <li>Records an end timestamp.</li>
 *       <li>Logs an {@code OK} response via {@link io.gatling.core.stats.StatsEngine#logResponse}.</li>
 *       <li>Passes the returned {@link io.gatling.core.session.Session} to the next action.</li>
 *     </ul>
 *   </li>
 *   <li>On failure (any {@link Exception}):
 *     <ul>
 *       <li>Records an end timestamp.</li>
 *       <li>Logs a {@code KO} response with the exception message.</li>
 *       <li>Marks the session as failed and continues to the next action.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Logging interop note</h2>
 * <p>When implementing Scala traits from Java (e.g., Gatling's {@link io.gatling.core.action.Action}),
 * Scala's {@code StrictLogging} expects a synthetic setter for the logger. This class provides the
 * required method {@code com$typesafe$scalalogging$StrictLogging$_setter_$logger_$eq} so the logger
 * can be initialized correctly.</p>
 */

package org.talentcatalog.perf.db;

import com.typesafe.scalalogging.Logger;
import io.gatling.commons.stats.KO$;
import io.gatling.commons.stats.OK$;
import io.gatling.core.action.Action;
import io.gatling.core.action.builder.ActionBuilder;
import io.gatling.core.session.Session;
import io.gatling.core.stats.StatsEngine;
import io.gatling.core.structure.ScenarioContext;
import java.util.function.Function;
import scala.Option;

public final class JdbcRequest implements io.gatling.javaapi.core.ActionBuilder {

  private final ActionBuilder scalaBuilder;

  /**
   * Creates a wrapper around a Scala {@link io.gatling.core.action.builder.ActionBuilder}.
   *
   * @param scalaBuilder the underlying Scala builder used by Gatling internally
   */
  private JdbcRequest(ActionBuilder scalaBuilder) {
    this.scalaBuilder = scalaBuilder;
  }

  /**
   * Returns the underlying Scala {@link io.gatling.core.action.builder.ActionBuilder}.
   *
   * @return the Scala action builder
   */
  @Override
  public ActionBuilder asScala() {
    return scalaBuilder;
  }

  /**
   * Builds a Gatling action that executes the provided function and logs it as a named request.
   *
   * <p>The function should perform the desired work (commonly a JDBC operation) and return the
   * {@link io.gatling.core.session.Session} to be forwarded to subsequent steps.</p>
   *
   * @param requestName the name that will appear in Gatling metrics/reports
   * @param fn the function to execute; it receives the current session and must return a session
   * @return an {@link JdbcRequest} usable in the Gatling Java DSL chain
   */
  public static JdbcRequest jdbc(String requestName, Function<Session, Session> fn) {
    return new JdbcRequest(new ActionBuilder() {

      @Override
      public Action build(ScenarioContext ctx, Action next) {

        final StatsEngine stats = ctx.coreComponents().statsEngine();
        final var clock = ctx.coreComponents().clock();

        return new Action() {

          private Logger _logger = Logger.apply(getClass());

          @Override
          public Logger logger() {
            return _logger;
          }

          public void com$typesafe$scalalogging$StrictLogging$_setter_$logger_$eq(Logger l) {
            this._logger = l;
          }

          @Override
          public String name() {
            return "JdbcRequest(" + requestName + ")";
          }

          @Override
          public void execute(Session session) {
            long start = clock.nowMillis();

            try {
              Session out = fn.apply(session);
              long end = clock.nowMillis();

              stats.logResponse(
                  session.scenario(),
                  session.groups(),
                  requestName,
                  start,
                  end,
                  OK$.MODULE$,
                  Option.empty(),
                  Option.empty()
              );

              next.execute(out);

            } catch (Exception e) {
              long end = clock.nowMillis();

              stats.logResponse(
                  session.scenario(),
                  session.groups(),
                  requestName,
                  start,
                  end,
                  KO$.MODULE$,
                  Option.apply(String.valueOf(e.getMessage())),
                  Option.empty()
              );

              next.execute(session.markAsFailed());
            }
          }

          @Override
          public void $bang(Session session) {
            execute(session);
          }
        };
      }
    });
  }
}