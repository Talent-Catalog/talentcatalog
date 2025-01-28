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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MetricsConfigTest {

  private MeterRegistry meterRegistry;

  @BeforeEach
  void setUp() {
    // Use a simple in-memory meter registry for testing
    meterRegistry = new SimpleMeterRegistry();
  }

  @Test
  void shouldRegisterExpectedMetrics() {
    // Act: Initialize the MetricsConfig with the test registry
    new MetricsConfig(meterRegistry);

    // Assert: Verify that the expected metrics are registered
    assertMetricExists("system.cpu.usage");
    assertMetricExists("jvm.memory.used");
  }

  private void assertMetricExists(String metricName) {
    RequiredSearch metricSearch = meterRegistry.get(metricName);
    assertNotNull(metricSearch.gauge(), "Metric '" + metricName + "' should be registered in the MeterRegistry");
  }
}
