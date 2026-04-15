/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.talentcatalog.perf.config;


/**
 * Immutable configuration snapshot for Gatling performance tests.
 *
 * <p>Instances are typically created by {@link PerfConfig#settings()} after applying
 * the module's resolution rules (system properties override {@code application.conf})
 * and validating required values.
 *
 * @param baseUrl  Base URL of the target system under test (e.g., {@code https://tctalent-test.org/}).
 *                 Must be non-blank.
 * @param userAgent Value for the HTTP {@code User-Agent} header used by Gatling requests.
 *                 If blank/missing in configuration, a default is applied by {@link PerfConfig}.
 */
public record PerfSettings(
    String baseUrl,
    String userAgent
) {}