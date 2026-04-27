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

package org.tctalent.server.service.db;

import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tctalent.server.response.preset.PresetGuestTokenResponse;

/**
 * Interface for interacting with the Preset API
 */
public interface PresetApiService {

  /**
   * Fetches a guest token from the Preset API that can be used to embed the specified dashboard.
   *
   * <p>Begins by obtaining a JWT token to authenticate the subsequent guest token request. Both
   * requests are attempted up to 3 times in the event of a regular exception. A JWT token is
   * time-limited, so it is re-initialised if the guest token request returns a 401 error, and in
   * this case the attempt to fetch the guest token is performed once more.
   * @param dashboardId The unique identifier of the dashboard that is to be embedded.
   * @return The {@link PresetGuestTokenResponse} object containing the guest token.
   *  @throws WebClientResponseException If the request to fetch the token fails due to network
   *  issues or an error response from the Preset API.
   *  @throws RuntimeException If any unexpected error occurs during the process.
   */
  PresetGuestTokenResponse fetchGuestToken(String dashboardId) throws WebClientResponseException;

}
