/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
 * Configuration properties for the Duolingo API integration.
 * <p>
 * This class contains the properties used to connect to the Duolingo API,
 * including the API secret for authentication and the base URL for API requests.
 * These properties are injected from the application's configuration files,
 * `application.yml`, using the prefix "duolingo.api".
 * </p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "duolingo.api")
public class DuolingoProperties {

  /**
   * The API secret used for authentication with the Duolingo API.
   * <p>
   * This secret is required to authenticate API requests to Duolingo services.
   * </p>
   */
  private String apiSecret;

  /**
   * The base URL of the Duolingo API.
   * <p>
   * This URL defines the endpoint to which requests will be made for Duolingo services.
   * It should be updated if the Duolingo API endpoint changes.
   * </p>
   */
  private String baseUrl;
}
