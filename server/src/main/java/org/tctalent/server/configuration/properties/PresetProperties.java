/*
 * Copyright (c) 2025 Talent Beyond Boundaries.
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

package org.tctalent.server.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Preset API integration.
 *
 * <p>This class contains the properties used to connect to the Preset API,
 * including the API secret for authentication and the base URL for API requests.
 * These properties are injected from the application's configuration files,
 * `application.yml`, using the prefix "preset.api".
 */
// TODO align properties with final version of application.yml - some may not be needed
@Getter
@Setter
@ConfigurationProperties(prefix = "preset.api")
public class PresetProperties {

  /**
   * The API secret used for authentication with the Preset API.
   *
   * <p>This secret is required to authenticate API requests to Preset services.
   */
  private String apiSecret;

  /**
   * The API token (referred to in Preset doc as 'name') used for authentication with the Preset API.
   *
   * <p>This secret is required to authenticate API requests to Preset services.
   */
  private String apiToken;

  /**
   * The base URL for obtaining JWT and guest tokens from the Preset API.
   */
  private String authBaseUrl;

  /**
   * The ID of the TC Preset team, required for Preset API interactions
   */
  private String teamId;

  /**
   * The ID of the TC Preset workspace, required for Preset API interactions
   */
  private String workspaceId;

  /**
   * The ID of the TC Preset dashboard, required for Preset API interactions
   */
  private String intelligenceDashId;

}
