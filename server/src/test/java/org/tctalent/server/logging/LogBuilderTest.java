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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LogBuilderTest {

  private Logger mockLogger;
  private SystemMetricsImpl mockSystemMetrics;

  private static final String USER_NAME = "test_user";
  private static final String FIRST_NAME = "test";
  private static final String LAST_NAME = "user";
  private static final String EMAIL = "test.user@tbb.org";
  private static final Role ROLE = Role.admin;
  private static final User USER = new User(USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, ROLE);

  static {
    USER.setId(123L);
  }

  @BeforeEach
  void setUp() {
    mockLogger = mock(Logger.class);
    ObjectProvider<SystemMetricsImpl> mockSystemMetricsProvider = mock(ObjectProvider.class);
    mockSystemMetrics = mock(SystemMetricsImpl.class);

    LogBuilder.setSystemMetricsProvider(mockSystemMetricsProvider);
    LogBuilder.setLogCpu(true);

    when(mockSystemMetricsProvider.getIfAvailable()).thenReturn(mockSystemMetrics);
  }

  @Test
  void shouldLogInfoWithAllFieldsIncludingCpu() {
    // Arrange
    when(mockSystemMetrics.getCpuUtilization()).thenReturn("50.00%");
    LogBuilder logBuilder = LogBuilder.builder(mockLogger)
        .action("TEST_ACTION")
        .message("This is a test message")
        .user(Optional.of(USER));

    // Act
    logBuilder.logInfo();

    // Assert
    ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockLogger).info(logCaptor.capture());

    String loggedMessage = logCaptor.getValue();
    assertThat(loggedMessage).contains("action: TEST_ACTION");
    assertThat(loggedMessage).contains("msg: This is a test message");
    assertThat(loggedMessage).contains("uid: 123");
    assertThat(loggedMessage).contains("cpu: 50.00%");
  }

  @Test
  void shouldNotLogCpuUtilizationWhenLogCpuIsDisabled() {
    // Arrange
    LogBuilder.setLogCpu(false); // Disable CPU logging
    LogBuilder logBuilder = LogBuilder.builder(mockLogger)
        .action("TEST_ACTION")
        .message("This is a test message");

    // Act
    logBuilder.logInfo();

    // Assert
    ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockLogger).info(logCaptor.capture());

    String loggedMessage = logCaptor.getValue();
    assertThat(loggedMessage).contains("action: TEST_ACTION");
    assertThat(loggedMessage).contains("msg: This is a test message");
    assertThat(loggedMessage).doesNotContain("cpu");
  }

  @Test
  void shouldHandleNullOptionalUser() {
    // Arrange
    LogBuilder logBuilder = LogBuilder.builder(mockLogger)
        .user(Optional.empty());

    // Act
    logBuilder.logInfo();

    // Assert
    ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockLogger).info(logCaptor.capture());

    String loggedMessage = logCaptor.getValue();
    assertThat(loggedMessage).doesNotContain("uid");
  }

  @Test
  void shouldLogWarnWithCustomFields() {
    // Arrange
    LogBuilder logBuilder = LogBuilder.builder(mockLogger)
        .listId(42L)
        .candidateId(99L)
        .action("WARN_ACTION");

    // Act
    logBuilder.logWarn();

    // Assert
    ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockLogger).warn(logCaptor.capture());

    String loggedMessage = logCaptor.getValue();
    assertThat(loggedMessage).contains("lid: 42");
    assertThat(loggedMessage).contains("cid: 99");
    assertThat(loggedMessage).contains("action: WARN_ACTION");
  }

  @Test
  void shouldLogErrorWithException() {
    // Arrange
    Exception exception = new RuntimeException("Test exception");
    LogBuilder logBuilder = LogBuilder.builder(mockLogger)
        .action("ERROR_ACTION")
        .message("An error occurred");

    // Act
    logBuilder.logError(exception);

    // Assert
    ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Throwable> exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);

    verify(mockLogger).error(logCaptor.capture(), exceptionCaptor.capture());

    String loggedMessage = logCaptor.getValue();
    Throwable loggedException = exceptionCaptor.getValue();

    assertThat(loggedMessage).contains("action: ERROR_ACTION");
    assertThat(loggedMessage).contains("msg: An error occurred");
    assertThat(loggedException).isEqualTo(exception);
  }

  @Test
  void shouldExcludeNullFieldsFromLogMessage() {
    // Arrange
    LogBuilder logBuilder = LogBuilder.builder(mockLogger)
        .action("ACTION_WITH_NULLS")
        .message(null); // Null message field

    // Act
    logBuilder.logInfo();

    // Assert
    ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockLogger).info(logCaptor.capture());

    String loggedMessage = logCaptor.getValue();
    assertThat(loggedMessage).contains("action: ACTION_WITH_NULLS");
    assertThat(loggedMessage).doesNotContain("msg");
  }
}
