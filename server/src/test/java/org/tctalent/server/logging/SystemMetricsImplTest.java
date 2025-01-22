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

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

class SystemMetricsImplTest {

  private MeterRegistry meterRegistry;
  private SystemMetricsImpl systemMetrics;

  @BeforeEach
  void setUp() {
    // Use a simple in-memory MeterRegistry for most tests
    meterRegistry = new SimpleMeterRegistry();
    systemMetrics = new SystemMetricsImpl(meterRegistry);
  }

  @Test
  void testGetAvailableMetrics() {
    // Register some test metrics
    AtomicReference<Double> testMetric1 = new AtomicReference<>(0.1);
    AtomicReference<Double> testMetric2 = new AtomicReference<>(0.2);

    Gauge.builder("test.metric.1", testMetric1, AtomicReference::get).register(meterRegistry);
    Gauge.builder("test.metric.2", testMetric2, AtomicReference::get).register(meterRegistry);

    // Fetch available metrics
    String availableMetrics = systemMetrics.getAvailableMetrics();

    // Check registered metrics are listed
    assertTrue(availableMetrics.contains("test.metric.1"), "Available metrics should include 'test.metric.1'");
    assertTrue(availableMetrics.contains("test.metric.2"), "Available metrics should include 'test.metric.2'");
  }

  @Test
  void testGetCpuUtilization() {
    // Mock the Gauge for CPU utilization
    Gauge mockGauge = mock(Gauge.class);
    when(mockGauge.value()).thenReturn(0.5); // 50% CPU usage

    MeterRegistry mockRegistry = mock(MeterRegistry.class);
    RequiredSearch mockSearch = mock(RequiredSearch.class);
    when(mockRegistry.get("system.cpu.usage")).thenReturn(mockSearch);
    when(mockSearch.gauge()).thenReturn(mockGauge);

    SystemMetricsImpl mockMetrics = new SystemMetricsImpl(mockRegistry);

    // Get CPU utilization
    String cpuUtilization = mockMetrics.getCpuUtilization();

    // Verify the correct formatted value
    assertEquals("50.00%", cpuUtilization, "CPU utilization should be formatted as '50.00%'");
  }

  @Test
  void testGetCpuUtilizationHandlesNaN() {
    // Mock the Gauge to return NaN
    Gauge mockGauge = mock(Gauge.class);
    when(mockGauge.value()).thenReturn(Double.NaN);

    MeterRegistry mockRegistry = mock(MeterRegistry.class);
    RequiredSearch mockSearch = mock(RequiredSearch.class);
    when(mockRegistry.get("system.cpu.usage")).thenReturn(mockSearch);
    when(mockSearch.gauge()).thenReturn(mockGauge);

    SystemMetricsImpl mockMetrics = new SystemMetricsImpl(mockRegistry);

    // Get CPU utilization when Gauge returns NaN
    String cpuUtilization = mockMetrics.getCpuUtilization();

    // Verify it returns "N/A"
    assertEquals("N/A", cpuUtilization, "CPU utilization should be 'N/A' when Gauge returns NaN");
  }

  @Test
  void testGetMemoryUtilization() {
    // Arrange: Register memory metrics
    Gauge.builder("jvm.memory.used", () -> 512.0).register(meterRegistry); // 512 MB used
    Gauge.builder("jvm.memory.max", () -> 1024.0).register(meterRegistry); // 1024 MB max

    // Fetch memory utilization
    String memoryUtilization = systemMetrics.getMemoryUtilization();

    // Verify memory utilization is correctly calculated
    assertEquals("50.00%", memoryUtilization, "Memory utilization should be 50.00% for 512/1024 MB");
  }

  @Test
  void testGetMemoryUtilizationHandlesNaN() {
    // Arrange: Register memory metrics with NaN values
    Gauge.builder("jvm.memory.used", () -> Double.NaN).register(meterRegistry);
    Gauge.builder("jvm.memory.max", () -> Double.NaN).register(meterRegistry);

    // Fetch memory utilization
    String memoryUtilization = systemMetrics.getMemoryUtilization();

    // Verify it returns "N/A" for NaN values
    assertEquals("N/A", memoryUtilization, "Memory utilization should return 'N/A' when metrics are NaN");
  }

  @Test
  void testGetMemoryUtilizationHandlesMissingMetrics() {
    // Fetch memory utilization without registering metrics
    String memoryUtilization = systemMetrics.getMemoryUtilization();

    // Verify it returns "N/A" when metrics are missing
    assertEquals("N/A", memoryUtilization, "Memory utilization should return 'N/A' when metrics are missing");
  }
}
