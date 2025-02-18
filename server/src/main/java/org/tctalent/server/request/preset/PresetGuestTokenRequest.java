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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the request payload for requesting a guest token from the Preset API.
 *
 * <p>Example JSON representation:</p>
 * <pre>
 *   "user": {
 *   "username": "example_username",
 *   "first_name": "First",
 *   "last_name": "Last"
 *   },
 *   "resources": [{
 *   "type": "dashboard",
 *   "id": EMBEDDED_DASHBOARD_ID
 *   }],
 *   "rls": []
 * </pre>
 *
 * @see <a href="https://docs.preset.io/v1/docs/step-2-deployment#1-create-guest-tokens-backend">
 *   Preset Doc
 *   </a>
 */
@Getter
@AllArgsConstructor
public class PresetGuestTokenRequest {

  private PresetUser user;
  private List<PresetResource> resources;
  private List<Object> rls = List.of(); // If TC user can see dashboard, they see all of it

  @Getter
  @AllArgsConstructor
  public static class PresetUser {
    private String username;
    private String first_name;
    private String last_name;
  }

  @Getter
  @AllArgsConstructor
  public static class PresetResource {
    private String type;
    private String id;
  }

}
