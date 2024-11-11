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
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.PageContext;

/**
 * Service for creating background processors
 */
public interface BackgroundProcessingService {

  /**
   * Creates a back processor to handle TC -> SF sync of active candidates. Page processing is
   * delegated to {@link CandidateService#processSfCandidateSyncPage(long, List)}, which enables
   * creation of a user session with Spring's @Transactional annotation, which doesn't work if
   * annotated method is called by another method in the same class.
   */
  BackProcessor<PageContext> createSfSyncBackProcessor(
      List<CandidateStatus> statuses, long totalNoOfPages
  );

}
