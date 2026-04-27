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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.ObjectProvider;


class LogBuilderConfigTest {

  private ObjectProvider<SystemMetricsImpl> mockSystemMetricsProvider;
  private LogBuilderProperties mockLogBuilderProperties;

  @BeforeEach
  void setUp() {
    mockSystemMetricsProvider = mock(ObjectProvider.class);
    mockLogBuilderProperties = mock(LogBuilderProperties.class);
  }

  @Test
  void shouldSetSystemMetricsProviderAndLogCpuFlag() {
    // Arrange
    when(mockLogBuilderProperties.isIncludeCpuUtilization()).thenReturn(true);

    try (MockedStatic<LogBuilder> logBuilderMockedStatic = mockStatic(LogBuilder.class)) {
      // Act
      new LogBuilderConfig(mockSystemMetricsProvider, mockLogBuilderProperties);

      // Assert
      // Verify static method calls
      logBuilderMockedStatic.verify(() -> LogBuilder.setSystemMetricsProvider(mockSystemMetricsProvider), times(1));
      logBuilderMockedStatic.verify(() -> LogBuilder.setLogCpu(true), times(1));
    }
  }

  @Test
  void shouldDisableCpuLoggingWhenFlagIsFalse() {
    // Arrange
    when(mockLogBuilderProperties.isIncludeCpuUtilization()).thenReturn(false);

    // Mock static methods
    try (MockedStatic<LogBuilder> logBuilderMockedStatic = mockStatic(LogBuilder.class)) {
      // Act
      new LogBuilderConfig(mockSystemMetricsProvider, mockLogBuilderProperties);

      // Assert
      logBuilderMockedStatic.verify(() -> LogBuilder.setSystemMetricsProvider(mockSystemMetricsProvider), times(1));
      logBuilderMockedStatic.verify(() -> LogBuilder.setLogCpu(false), times(1));
    }
  }
}
