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
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JvmMemoryUsageTest {
  private MeterRegistry meterRegistry;

  @BeforeEach
  void setUp() {
    // Use a simple in-memory MeterRegistry for most tests
    meterRegistry = new SimpleMeterRegistry();

    // Bind JVM memory metrics explicitly
    new JvmMemoryMetrics().bindTo(meterRegistry);
  }

  @Test
  void testJvmMemoryUsedAggregation() {
    // Retrieve all individual gauges for jvm.memory.used
    Collection<Gauge> gauges = meterRegistry.get("jvm.memory.used").gauges();

    // Print and sum all individual gauge values
    double totalIndividualUsedMemory = 0.0;
    System.out.println("=== Individual JVM Memory Gauges ===");
    for (Gauge gauge : gauges) {
      String area = gauge.getId().getTag("area");
      String id = gauge.getId().getTag("id");
      double value = gauge.value();
      totalIndividualUsedMemory += value;

      // Print each gauge details
      System.out.printf("Metric: area=%s, id=%s, value=%.2f MB%n", area, id, value / (1024 * 1024));
    }

    // Retrieve the single gauge value
    Gauge usedMemoryGauge = meterRegistry.get("jvm.memory.used").gauge();
    double singleGaugeValue = usedMemoryGauge.value();

    // Demonstrates that the sum of individual gauges is greater than the single gauge value, as
    // expected, due to the different memory areas being reported separately by micrometer.
    // This is why we need to sum up the individual gauge values to calculate total memory usage.
    System.out.println("=== JVM Memory Used Comparison ===");
    System.out.printf("Sum of Individual Gauge Values: %.2f MB%n", totalIndividualUsedMemory / (1024 * 1024));
    System.out.printf("Single Gauge Value: %.2f MB%n", singleGaugeValue / (1024 * 1024));
  }

}
