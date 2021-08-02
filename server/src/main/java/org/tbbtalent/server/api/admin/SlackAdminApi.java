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

package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.request.opportunity.PostJobToSlackRequest;
import org.tbbtalent.server.service.db.SlackService;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@RestController()
@RequestMapping("/api/admin/slack")
public class SlackAdminApi {
  private final SlackService slackService;

  @Autowired
  public SlackAdminApi(SlackService slackService) {
    this.slackService = slackService;
  }

  @PostMapping("post-job")
  public void postJob(@RequestBody PostJobToSlackRequest request) {
    slackService.postJob(request);
  }
  
}
