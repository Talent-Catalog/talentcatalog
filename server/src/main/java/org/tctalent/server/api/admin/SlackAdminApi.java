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

package org.tctalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.request.job.JobInfoForSlackPost;
import org.tctalent.server.request.opportunity.PostJobToSlackRequest;
import org.tctalent.server.request.opportunity.PostJobToSlackResponse;
import org.tctalent.server.service.db.JobService;
import org.tctalent.server.service.db.SlackService;

/**
 * API for communicating with TBB's Slack workspace
 *
 * @author John Cameron
 */
@RestController()
@RequestMapping("/api/admin/slack")
public class SlackAdminApi {
  private final JobService jobService;
  private final SlackService slackService;

  @Autowired
  public SlackAdminApi(JobService jobService, SlackService slackService) {
    this.jobService = jobService;
    this.slackService = slackService;
  }

  /**
   * Post job info to Slack
   * @param request Data to be posted
   * @return Url of channel posted to
   */
  @PostMapping("post-job")
  public PostJobToSlackResponse postJob(@RequestBody PostJobToSlackRequest request) {
    String slackChannelUrl = slackService.postJob(request);
    return new PostJobToSlackResponse(slackChannelUrl);
  }

  /**
   * Post job to Slack given a job id and a link to the job on the TC
   */
  @PostMapping("{id}/post-job")
  public PostJobToSlackResponse postJob(@PathVariable("id") long id, @RequestBody String tcJobLink) {
    JobInfoForSlackPost jobInfo = jobService.extractJobInfoForSlack(id, tcJobLink);
    String slackChannelUrl = slackService.postJob(jobInfo);
    return new PostJobToSlackResponse(slackChannelUrl);
  }

}
