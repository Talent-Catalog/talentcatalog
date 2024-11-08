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

package org.tctalent.server.service.db;

import java.util.List;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.CandidateStatus;

/**
 * Provides separation for background processing methods when desired. Particularly useful when
 * processing requires Spring's @Transactional annotation, which doesn't work when annotated method
 * is called by another in its class.
 */
public interface BackgroundProcessingService {

  /**
   * Processes a single page for the TC-SF candidate sync.
   * @param startPage page to process (zero-based index)
   * @param statuses types of {@link CandidateStatus} to filter for in search
   * @throws WebClientException if there is a problem connecting to Salesforce
   * @throws SalesforceException if Salesforce had a problem with the data
   */
  void processSfCandidateSyncPage(
      long startPage, List<CandidateStatus> statuses
  ) throws SalesforceException, WebClientException;

}
