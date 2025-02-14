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
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PresetGuestTokenRequest {

  private User user;
  private List<Resource> resources;
  private List<Object> rls;

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class User {
    private String username;
    private String firstName;
    private String lastName;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Resource {
    private String type;
    private String id;
  }

}
