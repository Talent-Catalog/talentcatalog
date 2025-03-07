/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.response.preset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Represents the response from the Preset API JWT token request.
 *
 * <p>The Preset API returns a JSON response containing a JWT token which is required for subsequent
 * API requests.
 *
 * <p>Example JSON response:
 * <pre>
 * {
 *   "payload": {
 *     "access_token": "your_jwt_token_here"
 *   }
 * }
 * </pre>
 *
 * @see <a href="https://api-docs.preset.io/">Preset API Documentation</a>
 */
@Getter
public class PresetJwtTokenResponse {

  /**
   * The payload containing authentication details.
   */
  @JsonProperty("payload")
  private Payload payload;

  /**
   * Represents the payload that contains the JWT token.
   */
  @Getter
  public static class Payload {
    @JsonProperty("access_token")
    private String jwtToken;
  }
}
