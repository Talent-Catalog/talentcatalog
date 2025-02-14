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

package org.tctalent.server.service.db.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.service.db.PresetApiService;

@Service
@Slf4j
public class PresetApiServiceImpl implements PresetApiService {

  private final WebClient presetWebClient;

  /**
   * Since there are more than one of the same type of bean,
   * <a href="https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired-qualifiers.html">
   *   Spring doc
   *   </a>
   * prescribes use of @Qualifier to ensure that the correct WebClient is injected.
   * @param presetWebClient the {@link WebClient} for making calls to the Preset API
   */
  @Autowired
  public PresetApiServiceImpl(@Qualifier("presetWebClient") WebClient presetWebClient) {
    this.presetWebClient = presetWebClient;
  }

  // TODO delete once no longer needed (or clearly identify as a model for making standard Preset API calls
  public String getDashboards() {
    try {
      return presetWebClient.get()
          .uri("dashboards/")
          .retrieve()
          .bodyToMono(String.class)
          .block();
    } catch (WebClientResponseException e) {
      LogBuilder.builder(log)
          .logError(e);
      throw e;
    }
  }

}
