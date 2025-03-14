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
 * Represents the response from a Preset API guest token request.
 *
 * <p>The Preset API returns a JSON response containing a guest token which is required for
 * dashboard embedding.
 *
 * <p>Example JSON response:
 * <pre>
 * {
 *   "data": {
 *     "payload": {
 *       "token": "your_guest_token_here"
 *     }
 *   }
 * }
 * </pre>
 *
 * @see <a href="https://api-docs.preset.io/">Preset API Documentation</a>
 */
@Getter
public class PresetGuestTokenResponse {

  /**
   * The payload that contains the guest token.
   */
  @JsonProperty("payload")
  private Payload payload;

  /**
   * Represents the payload that contains the guest token.
   */
  @Getter
  public static class Payload {
    /**
     * Represents the guest token.
     */
    @JsonProperty("token")
    private String guestToken;
  }

}
