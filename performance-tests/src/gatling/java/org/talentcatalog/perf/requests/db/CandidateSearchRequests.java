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

package org.talentcatalog.perf.requests.db;

import static io.gatling.javaapi.core.CoreDsl.exec;

import io.gatling.javaapi.core.ChainBuilder;
import org.talentcatalog.perf.db.JdbcRequest;
import org.talentcatalog.perf.db.repositories.CandidateSearchRepository;

/**
 * Gatling request wrappers for Candidate Search DB actions.
 *
 * <p>Rule: 1 Gatling request == 1 repository call, so reports stay clean.
 */
public final class CandidateSearchRequests {

  private static final CandidateSearchRepository repo = new CandidateSearchRepository();

  private CandidateSearchRequests() {}

  public static ChainBuilder searchPaged() {
    return exec(JdbcRequest.jdbc("DB CandidateSearchPaged", session -> {
      int limit = 20;
      int offset = 0;
      try {
        repo.searchPaged(limit, offset);
      } catch (Exception e) {
        throw new RuntimeException("DB CandidateSearchPaged failed", e);
      }
      return session;
    }));
  }
}