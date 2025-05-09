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

package org.tctalent.server.util.background.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.service.db.email.EmailHelper;

public class BackLoggerFactoryTest {
  private EmailHelper emailHelper;
  private BackLoggerFactory backLoggerFactory;
  private final String jobName = "TestJob";

  @BeforeEach
  void setUp() {
    emailHelper = mock(EmailHelper.class);
    backLoggerFactory = new BackLoggerFactory(emailHelper);
  }

  @Test
  void createWithEmailAlertsTrueShouldReturnBackLoggerWithEmailFailureAlert() {
    BackLogger backLogger = backLoggerFactory.create(jobName, true);

    assertThat(backLogger).isInstanceOf(BackLoggerWithEmailFailureAlert.class);
    assertThat(backLogger).extracting("jobName").isEqualTo(jobName);
  }

  @Test
  void createWithEmailAlertsFalseShouldReturnDefaultBackLogger() {
    BackLogger backLogger = backLoggerFactory.create(jobName, false);

    assertThat(backLogger).isInstanceOf(DefaultBackLogger.class);
    assertThat(backLogger).extracting("jobName").isEqualTo(jobName);
  }

}
