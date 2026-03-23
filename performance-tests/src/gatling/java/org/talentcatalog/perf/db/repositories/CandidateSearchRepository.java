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

package org.talentcatalog.perf.db.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.talentcatalog.perf.db.Db;
import org.talentcatalog.perf.db.sql.Sql;

/**
 * Candidate Search database operations.
 *
 * <p>Repository owns:
 * <ul>
 *   <li>Which SQL file to run</li>
 *   <li>Parameter binding</li>
 *   <li>Execution details (fetch size, timeouts, autocommit)</li>
 * </ul>
 */
public final class CandidateSearchRepository {

  private static final String SEARCH_PAGED_SQL = Sql.load("candidate_search_paged.sql");

  public void searchPaged(int limit, int offset) throws Exception {
    try (Connection con = Db.getConnection();
        PreparedStatement ps = con.prepareStatement(SEARCH_PAGED_SQL)) {

      ps.setInt(1, limit);
      ps.setInt(2, offset);

      con.setAutoCommit(false);
      ps.setFetchSize(Math.min(limit, 1000));
      ps.setQueryTimeout(30);

      int rows = 0;
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          rows++;
        }
      }
    }
  }
}