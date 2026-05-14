/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy.RepositoryDetectionStrategies;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.tctalent.server.repository.db.CandidatePropertyDefinitionRepository;

/**
 * Set up our use of
 * <a href="https://docs.spring.io/spring-data/rest/docs/current/reference/html/#_repository_rest_resources">
 *     Spring Data Rest</a> repository-driven APIs.
 * <p>
 * The API is exposed at /api/hal.
 * <p>
 * Exposed repositories are determined by the presence of the @RepositoryRestResource annotation.
 * <p>
 * See for example {@link CandidatePropertyDefinitionRepository}.
 *
 * @author John Cameron
 */
@Configuration
public class RestRepositoryConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config,
        CorsRegistry cors) {

        config.setBasePath("api/hal");

        //We only want to expose automated API's from repositories that are explicitly annotated.
        config.setRepositoryDetectionStrategy(RepositoryDetectionStrategies.ANNOTATED);
    }
}
