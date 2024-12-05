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
import org.tctalent.server.api.admin.SystemAdminApi;
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

  /**
   * Creates a back processor to handle processing of potential duplicate candidates.
   */
  BackProcessor<PageContext> createPotentialDuplicatesBackProcessor(List<Long> candidateIds);

  /**
   * Daily check for candidates who may have more than one profile based on identical first name AND
   * last name AND DOB - sets potentialDuplicate to true. Can also be triggered manually from
   * {@link SystemAdminApi}.
   * <p>
   *   Calls {@link CandidateService#cleanUpResolvedDuplicates(List)} which sets same property to
   *   false if previously flagged candidates no longer meet the criteria.
   * </p>
   */
  void processPotentialDuplicateCandidates();

  /**
   * Creates a background processor and sets parameters for its scheduled operations.
   * @param potentialDupeIds IDs of candidates identified as potential duplicates by caller.
   */
  void initiateDuplicateProcessing(List<Long> potentialDupeIds);

}
