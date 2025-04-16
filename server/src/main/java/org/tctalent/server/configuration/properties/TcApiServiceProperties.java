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

package org.tctalent.server.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for {@link org.tctalent.server.service.api.TcApiService}.
 * </p>
 * Example configuration in {@code application.yml}:
 * <pre>
 *  tc-api:
 *    apiUrl: http://localhost:8082
 *    apiKey: xxxx
 * </pre>
 *
 * @author sadatmalik
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tc-api")
public class TcApiServiceProperties {
  private String apiUrl;
  private String apiKey;
}
