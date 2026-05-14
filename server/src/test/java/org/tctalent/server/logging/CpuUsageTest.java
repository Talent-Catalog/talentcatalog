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

package org.tctalent.server.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CpuUsageTest {
  private MeterRegistry meterRegistry;

  @BeforeEach
  void setUp() {
    // Use a simple in-memory registry for testing
    meterRegistry = new SimpleMeterRegistry();

    // Bind CPU usage metrics explicitly
    new ProcessorMetrics().bindTo(meterRegistry);
  }

  @Test
  void testSystemCpuUsageGauge() {
    // Retrieve all gauges for system.cpu.usage
    Collection<Gauge> gauges = meterRegistry.get("system.cpu.usage").gauges();

    // Ensure only one CPU usage gauge exists
    // Micrometer always exposes a single system.cpu.usage gauge regardless of number of processors
    // There is not need to aggregate individual gauges to determine total CPU usage
    assertEquals(1, gauges.size(), "Expected a single system.cpu.usage gauge");

    // Retrieve the single gauge
    Gauge cpuUsageGauge = meterRegistry.get("system.cpu.usage").gauge();
    assertNotNull(cpuUsageGauge, "CPU usage gauge should not be null");

    // Get and print the gauge value
    double cpuUsageValue = cpuUsageGauge.value();
    System.out.printf("CPU Usage Gauge Value: %.2f%%%n", cpuUsageValue * 100);

    // Validate the CPU usage value is within expected bounds (0.0 - 1.0)
    assertTrue(cpuUsageValue >= 0.0 && cpuUsageValue <= 1.0,
        "CPU usage value should be between 0.0 and 1.0");
  }
}
