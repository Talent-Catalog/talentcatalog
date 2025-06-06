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

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link SystemMetrics} providing method implementations to retrieve formatted
 * system metrics such as CPU utilisation, memory utilisation, and available metrics using the
 * {@link MeterRegistry}.
 *
 * @author sadatmalik
 */
@Component
public class SystemMetricsImpl implements SystemMetrics {
  private final MeterRegistry meterRegistry;
  private final AtomicReference<String> lastCpuValue = new AtomicReference<>("N/A");
  private final AtomicReference<String> lastMemValue = new AtomicReference<>("N/A");

  public SystemMetricsImpl(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public String getAvailableMetrics() {
    String availableMetrics = meterRegistry.getMeters().stream()
        .map(meter -> meter.getId().getName())
        .distinct()
        .sorted()
        .collect(Collectors.joining(", "));

    return availableMetrics;
  }

  @Override
  public String getCpuUtilization() {
    try {
      RequiredSearch cpuStats = meterRegistry.get("system.cpu.usage");
      Gauge gauge = cpuStats.gauge(); // todo get this just once at startup?
      double cpuUsage = gauge.value();
      if (!Double.isNaN(cpuUsage)) {
        String formattedValue = String.format("%.2f%%", cpuUsage * 100);
        lastCpuValue.set(formattedValue);
        return formattedValue;
      }
    } catch (Exception e) {
      System.err.println("Error fetching CPU utilization: " + e.getMessage());
    }
    return lastCpuValue.get(); // Return last valid value if no metric or an error occurs
  }

  /**
   * Calculates the total memory utilisation across all JVM memory pools.
   * <p>
   * Micrometer reports JVM memory usage as separate metrics for different memory pools,
   * categorised into "heap" and "non-heap" memory areas. These memory pools include:
   * - Heap memory: G1 Eden Space, G1 Survivor Space, G1 Old Gen, etc.
   * - Non-heap memory: Metaspace, CodeHeap for JIT compilation, etc.
   * <p>
   * See the JvmMemoryUsageTest#testJvmMemoryUsedAggregation unit test for details and the
   * Micrometer JVM Memory Metrics source code:
   * <a href="https://github.com/micrometer-metrics/micrometer/blob/main/micrometer-core/src/main/java/io/micrometer/core/instrument/binder/jvm/JvmMemoryMetrics.java">
   * JvmMemoryMetrics.java</a>
   * <p>
   * Since Micrometer does not provide a pre-aggregated "total memory used" metric, this
   * method explicitly sums up the individual memory usage (`jvm.memory.used`) and maximum
   * capacity (`jvm.memory.max`) values across all memory pools.
   * <p>
   * The total memory utilisation is then calculated and returned.
   *
   * @return A formatted string representing total JVM memory utilisation as a percentage.
   */
  @Override
  public String getMemoryUtilization() {
    try {
      // Get all gauges for jvm.memory.used
      Collection<Gauge> usedMemoryGauges = meterRegistry.get("jvm.memory.used").gauges();
      double totalUsedMemory = usedMemoryGauges.stream()
          .mapToDouble(Gauge::value)
          .sum();

      // Get all gauges for jvm.memory.max
      Collection<Gauge> maxMemoryGauges = meterRegistry.get("jvm.memory.max").gauges();
      double totalMaxMemory = maxMemoryGauges.stream()
          .mapToDouble(Gauge::value)
          .sum();

      if (!Double.isNaN(totalUsedMemory) && !Double.isNaN(totalMaxMemory) && totalMaxMemory > 0) {
        double memoryUtilization = Math.min((totalUsedMemory / totalMaxMemory) * 100, 100);
        String formattedValue = String.format("%.2f%%", memoryUtilization);
        lastMemValue.set(formattedValue);
        return formattedValue;
      }
    } catch (Exception e) {
      System.err.println("Error fetching memory utilization: " + e.getMessage());
    }
    return lastMemValue.get(); // Return last valid value if no metric or an error occurs
  }

}
