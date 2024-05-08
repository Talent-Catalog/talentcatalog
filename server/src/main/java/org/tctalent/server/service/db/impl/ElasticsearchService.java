/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import org.springframework.lang.NonNull;


public interface ElasticsearchService {

  /**
   * Retrieves a set of candidate IDs by searching for a specified name.
   * This method uses Elasticsearch to perform a search based on the full name of candidates.
   *
   * @param name the full name to search for in the Elasticsearch index. Must not be null.
   * @return a {@link Set} of {@link Long} candidate IDs that match the search criteria.
   *         The set will be empty if no candidates are found.
   * @throws IllegalArgumentException if the provided name is null.
   */
  Set<Long> findByName(@NonNull String name);

}
