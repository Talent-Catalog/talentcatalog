/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PartnerDatabaseDefinitionTest {

  private TestDriver testDriver;

  @AfterEach
  void tearDown() throws SQLException {
    if (testDriver != null) {
      DriverManager.deregisterDriver(testDriver);
    }
  }

  @Test
  void constructorAndGetters_storeAndReturnConfiguredValues() {
    List<PartnerTableDefinition> tables = List.of(mock(PartnerTableDefinition.class));

    PartnerDatabaseDefinition definition = new PartnerDatabaseDefinition(
        "Afghanistan",
        "jdbc:test-db",
        "db-user",
        "db-password",
        tables
    );

    assertEquals("Afghanistan", definition.getCountry());
    assertEquals("jdbc:test-db", definition.getDbUrl());
    assertSame(tables, definition.getTables());
  }

  @Test
  void connect_usesConfiguredUrlUserAndPassword() throws SQLException {
    Connection connection = mock(Connection.class);
    testDriver = new TestDriver("jdbc:partner-test:", connection);
    DriverManager.registerDriver(testDriver);

    PartnerDatabaseDefinition definition = new PartnerDatabaseDefinition(
        "Afghanistan",
        "jdbc:partner-test:destination-db",
        "partner-user",
        "partner-password",
        List.of()
    );

    Connection result = definition.connect();

    assertSame(connection, result);
    assertEquals("jdbc:partner-test:destination-db", testDriver.receivedUrl);
    assertEquals("partner-user", testDriver.receivedProperties.getProperty("user"));
    assertEquals("partner-password", testDriver.receivedProperties.getProperty("password"));
  }

  private static class TestDriver implements Driver {

    private final String acceptedUrlPrefix;
    private final Connection connection;

    private String receivedUrl;
    private Properties receivedProperties;

    private TestDriver(String acceptedUrlPrefix, Connection connection) {
      this.acceptedUrlPrefix = acceptedUrlPrefix;
      this.connection = connection;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
      if (!acceptsURL(url)) {
        return null;
      }

      receivedUrl = url;
      receivedProperties = info;
      return connection;
    }

    @Override
    public boolean acceptsURL(String url) {
      return url != null && url.startsWith(acceptedUrlPrefix);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
      return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
      return 1;
    }

    @Override
    public int getMinorVersion() {
      return 0;
    }

    @Override
    public boolean jdbcCompliant() {
      return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      throw new SQLFeatureNotSupportedException();
    }
  }
}