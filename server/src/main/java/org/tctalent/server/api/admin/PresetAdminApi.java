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

package org.tctalent.server.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.response.preset.PresetGuestTokenResponse;
import org.tctalent.server.service.db.PresetApiService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/preset")
public class PresetAdminApi {

  private final PresetApiService presetApiService;

  /**
   * Generates and returns a guest token for embedding a Preset dashboard.
   *
   * @param dashboardId The unique identifier of the dashboard for which guest token is requested.
   * @return A {@link PresetGuestTokenResponse} containing the generated guest token.
   */
  @PostMapping(value = "{dashboardId}/guest-token")
  public PresetGuestTokenResponse fetchGuestToken(@PathVariable("dashboardId") String dashboardId) {
    return presetApiService.fetchGuestToken(dashboardId);
  }

}
