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

package org.tctalent.server.service.db;

import java.util.List;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.IdContext;

/**
 * Service for background processing of candidate opportunities
 */
public interface CandidateOppBackgroundProcessingService {

  /**
   * Update all open Cases from their corresponding records on Salesforce.
   */
  void updateOpenCasesFromSf();

  /**
   * TODO
   * @param sfIds
   */
  void initiateBackgroundCaseUpdate(List<String> sfIds);

  /**
   * TODO
   */
  BackProcessor<IdContext> createCaseUpdateBackProcessor(List<Opportunity> sfOpps);

}