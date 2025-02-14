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

package org.tctalent.server.request.preset;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the request payload for authenticating with the Preset API.
 * <p>
 * This class is used to send authentication credentials (name and secret)
 * when requesting a JWT token from the Preset API. Both can be obtained via the
 * <a href="https://manage.app.preset.io/app/user">Preset Manager user edit UI</a> - the user must
 * be a workspace admin.
 * </p>
 *
 * <p>Example JSON representation:</p>
 * <pre>
 * {
 *   "name": "API_token",
 *   "secret": "API_secret"
 * }
 * </pre>
 *
 * @see <a href="https://api-docs.preset.io/">Preset API Documentation</a>
 */
@Getter
@Setter
@AllArgsConstructor
public class PresetJwtTokenRequest {

  /**
   * The API token name used for authentication.
   */
  private String name;

  /**
   * The API secret key used for authentication.
   */
  private String secret;

}
