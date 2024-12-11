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

package org.tctalent.server.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configures access to TBB slack workspace (http://refugeejobsmarket.slack.com) through
 * the TalentCatalog Slack App - https://api.slack.com/apps/A01UW0XCBBN
 *
 * @author John Cameron
 */
@Getter
@Setter
@ConfigurationProperties("slack")
public class SlackConfig {

  /**
   * ID of Slack Channel where messages are sent.
   * Go to TBB Slack workspace in web browser https://refugeejobsmarket.slack.com and select
   * channel. The Channel ID is the last part of the url after the last "/" character.
   * <p/>
   * eg https://app.slack.com/client/T3U28AZQC/C02532JHVNZ, channel is C02532JHVNZ
   */
  private String channelId;

  /**
   * Bot User OAuth token
   * https://api.slack.com/apps/A01UW0XCBBN/oauth?
   */
  private String token;

  /**
   * Url of TBB's Slack workspace - eg https://refugeejobsmarket.slack.com/
   */
  private String workspace;

  public String getChannelUrl() {
    return workspace + "/archives/" + channelId;
  }

}
