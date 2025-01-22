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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

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

  @Override
  public String getMemoryUtilization() {
    try {
      // Get used memory
      RequiredSearch usedMemorySearch = meterRegistry.get("jvm.memory.used");
      Gauge usedMemoryGauge = usedMemorySearch.gauge();
      double usedMemory = usedMemoryGauge.value();

      // Get max memory
      RequiredSearch maxMemorySearch = meterRegistry.get("jvm.memory.max");
      Gauge maxMemoryGauge = maxMemorySearch.gauge();
      double maxMemory = maxMemoryGauge.value();

      if (!Double.isNaN(usedMemory) && !Double.isNaN(maxMemory) && maxMemory > 0) {
        double memoryUtilization = (usedMemory / maxMemory) * 100;
        String formattedValue = String.format("%.2f%%", memoryUtilization);
        lastMemValue.set(formattedValue);
        return formattedValue;
      }
    } catch (Exception e) {
      System.err.println("Error fetching memory utilization: " + e.getMessage());
    }
    return lastCpuValue.get(); // Return last valid value if no metric or an error occurs
  }

}
