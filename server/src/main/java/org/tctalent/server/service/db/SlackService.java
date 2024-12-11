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

import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.request.job.JobInfoForSlackPost;
import org.tctalent.server.request.opportunity.PostJobToSlackRequest;

/**
 * Interface to TBB's Slack workspace
 *
 * @author John Cameron
 */
public interface SlackService {

  /**
   * Sends a post to TBB's Slack workspace containing information about a job.
   * @param request Contains the information to be posted
   * @return Url of Slack channel posted to
   */
  String postJob(PostJobToSlackRequest request);

  /**
   * Sends a post to TBB's Slack workspace containing information about a job.
   * @param jobInfo Job details which will be posted to Slack
   * @return Url of Slack channel posted to
   * @throws NoSuchObjectException if there is no job with that id
   */
  String postJob(JobInfoForSlackPost jobInfo) throws NoSuchObjectException;
}
