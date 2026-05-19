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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class LogBuilderPropertiesTest {

  @Test
  void shouldBindIncludeCpuUtilizationProperty() {
    // Define properties for testing
    Map<String, String> properties = Map.of(
        "logbuilder.includeCpuUtilization", "true"
    );
    MapConfigurationPropertySource propertySource = new MapConfigurationPropertySource(properties);
    Binder binder = new Binder(propertySource);

    // Bind properties to LogBuilderProperties
    LogBuilderProperties boundProperties = binder.bind("logbuilder", LogBuilderProperties.class).get();

    // Verify property
    assertThat(boundProperties.isIncludeCpuUtilization()).isTrue();
  }
}
