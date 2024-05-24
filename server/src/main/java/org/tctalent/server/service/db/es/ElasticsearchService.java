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

package org.tctalent.server.service.db.es;

import java.util.Set;
import org.springframework.lang.NonNull;


public interface ElasticsearchService {

  /**
   * Retrieves a set of candidate IDs by elastic searching for a specified name.
   *
   * @param name the full name to search for in the Elasticsearch index. Must not be null.
   * @return a {@link Set} of {@link Long} candidate IDs that match the search criteria.
   *         The set will be empty if no candidates are found.
   * @throws IllegalArgumentException if the provided name is null.
   */
  Set<Long> findByName(@NonNull String name);

  /**
   * Retrieves a set of candidate IDs by elastic searching for a specified input string
   * that matches either the phone number or email in the Elasticsearch index.
   *
   * @param input the input string to search for in the Elasticsearch index. Must not be null.
   * @return a {@link Set} of {@link Long} candidate IDs that match the search criteria.
   *         The set will be empty if no candidates are found.
   * @throws IllegalArgumentException if the provided input string is null.
   */
  Set<Long> findByPhoneOrEmail(@NonNull String input);

  /**
   * Retrieves a set of candidate IDs by elastic searching for a specified external ID
   *
   * @param externalId the external ID string to search for in the Elasticsearch index. Must not be
   *                  null.
   * @return a {@link Set} of {@link Long} candidate IDs that match the search criteria.
   *         The set will be empty if no candidates are found.
   * @throws IllegalArgumentException if the provided input string is null.
   */
  Set<Long> findByExternalId(@NonNull String externalId);

}
