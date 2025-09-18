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

package org.tctalent.server.candidateservices.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tctalent.server.candidateservices.core.services.CandidateService;
import org.tctalent.server.candidateservices.application.providers.duolingo.DuolingoService;


@Configuration
public class ProviderRegistryConfig {

  @Bean(name = "DUOLINGO") // TODO -- SM -- use enum ?? not sure if can here ?
  CandidateService duolingoService(DuolingoService impl) {
    return impl;
  }

  // Add more providers similarly

}
