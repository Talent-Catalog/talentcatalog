/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db;

import org.tbbtalent.server.request.opportunity.PostJobToSlackRequest;

/**
 * Interface to TBB's Slack workspace
 *
 * @author John Cameron
 */
public interface SlackService {

  /**
   * Sends a post TBB's Slack workspace containing information about a job.
   * @param request Contains the information to be posted 
   * @return Url of Slack channel posted to
   */
  String postJob(PostJobToSlackRequest request);
}
