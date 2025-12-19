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

/**
 * Service for background processing of Candidate Opportunities
 */
public interface CandidateOppBackgroundProcessingService {

  /**
   * Initiates background processing of updates to all open TC Candidate Opps from their Salesforce
   * equivalents, provided they have one. Will also update Opps that are closed on the TC but
   * recently reopened on Salesforce.
   *
   * <p>The intent is to keep the TC up to date when users have updated Opps from Salesforce instead
   * of the TC, as is preferred. Updates are only made when the Salesforce record contains new data.
   */
  void initiateBackgroundCaseUpdate();

}