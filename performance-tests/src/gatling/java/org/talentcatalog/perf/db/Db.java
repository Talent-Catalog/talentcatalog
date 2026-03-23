package org.talentcatalog.perf.db;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Infrastructure-only DB module.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Build and own the shared {@link HikariDataSource}</li>
 *   <li>Provide {@link #getConnection()}</li>
 *   <li>Clean shutdown via {@link #close()}</li>
 * </ul>
 */
public final class Db {

  private static final HikariDataSource ds = buildDataSource();

  private Db() {}

  public static Connection getConnection() throws SQLException {
    return ds.getConnection();
  }

  public static void close() {
    ds.close();
  }

  private static HikariDataSource buildDataSource() {
    Config c = ConfigFactory.load();

    String url = System.getProperty("db.url", c.getString("db.url"));
    String user = System.getProperty("db.username", c.getString("db.username"));
    String password = System.getProperty("db.password", c.getString("db.password"));

    int maxPool = Integer.parseInt(System.getProperty(
        "db.maximumPoolSize",
        Integer.toString(c.getInt("db.maximumPoolSize"))
    ));

    long connTimeoutMs = Long.parseLong(System.getProperty(
        "db.connectionTimeoutMs",
        Long.toString(c.getLong("db.connectionTimeoutMs"))
    ));

    long stmtTimeoutMs = Long.parseLong(System.getProperty(
        "db.statementTimeoutMs",
        Long.toString(c.getLong("db.statementTimeoutMs"))
    ));

    HikariConfig hc = new HikariConfig();
    hc.setJdbcUrl(url);
    hc.setUsername(user);
    hc.setPassword(password);
    hc.setMaximumPoolSize(maxPool);
    hc.setConnectionTimeout(connTimeoutMs);

    // Postgres statement_timeout (server-side) as a safety net
    hc.addDataSourceProperty("options", "-c statement_timeout=" + stmtTimeoutMs);

    return new HikariDataSource(hc);
  }
}