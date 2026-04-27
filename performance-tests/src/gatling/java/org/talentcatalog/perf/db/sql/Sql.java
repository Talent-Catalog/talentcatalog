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

package org.talentcatalog.perf.db.sql;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads SQL files from classpath: src/gatling/resources/sql/*.sql
 * and caches them in-memory.
 */
public final class Sql {

  private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

  private Sql() {}

  /**
   * Load a SQL file by name (e.g. "candidate_search_paged.sql") from resources/sql.
   */
  public static String load(String fileName) {
    return CACHE.computeIfAbsent(fileName, Sql::readFromClasspath);
  }

  private static String readFromClasspath(String fileName) {
    String path = "sql/" + fileName;
    try (InputStream in = Sql.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalArgumentException("SQL not found on classpath: " + path);
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load SQL: " + path, e);
    }
  }
}