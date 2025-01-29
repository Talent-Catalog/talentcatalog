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

package org.tctalent.server.logging;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the {@code LogBuilder}.
 * <p>
 * The configuration properties are sourced from {@link LogBuilderProperties}.
 *
 * @author sadatmalik
 */
@Configuration
public class LogBuilderConfig {

  public LogBuilderConfig(ObjectProvider<SystemMetricsImpl> systemMetricsProvider,
      LogBuilderProperties logBuilderProperties) {
    LogBuilder.setSystemMetricsProvider(systemMetricsProvider);
    LogBuilder.setLogCpu(logBuilderProperties.isIncludeCpuUtilization());
    LogBuilder.setLogMemory(logBuilderProperties.isIncludeMemoryUtilization());
  }
}
