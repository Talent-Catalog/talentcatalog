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

package org.tctalent.server.service.db.impl;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.SupersetService;
import org.springframework.stereotype.Service;
import org.tctalent.server.service.db.UserService;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupersetServiceImpl implements SupersetService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final SavedSearchService savedSearchService;
    private final AuthService authService;

    public String runStats(long savedSearchId) {
      User user = authService.getLoggedInUser()
          .orElseThrow(() -> new InvalidSessionException("Not logged in"));

      Set<Long> candidateIds = savedSearchService.searchCandidates(savedSearchId);

      String tableName = "superset_table_" + user.getId();

      String createSql =
          "CREATE TABLE IF NOT EXISTS " + tableName + " (id BIGINT)";
      jdbcTemplate.execute(createSql);

      // Clear the table
      String truncateSql = "TRUNCATE TABLE " + tableName;
      jdbcTemplate.execute(truncateSql);

      // Insert new values
      String insertSql = "INSERT INTO " + tableName + " VALUES (?)";
      jdbcTemplate.batchUpdate(insertSql, candidateIds, candidateIds.size(),
          (ps, candidateId) -> ps.setLong(1, candidateId));

      return tableName;
    }

}
