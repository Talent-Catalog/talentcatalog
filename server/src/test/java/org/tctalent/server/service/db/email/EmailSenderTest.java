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

package org.tctalent.server.service.db.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EmailSenderTest {

  @InjectMocks
  private EmailSender emailSender;

  @Mock
  private JavaMailSender mockMailSender;

  @Mock
  private MimeMessage mockMimeMessage;

  @Captor
  private ArgumentCaptor<MimeMessage> messageCaptor;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

    setField("host", "smtp.test.com");
    setField("port", 587);
    setField("user", "test-user");
    setField("password", "test-pass");
    setField("authenticated", true);
    setField("defaultEmail", "noreply@test.com");
    setField("alertEmail", "alerts@test.com");
    setField("testOverrideEmail", "testoverride@test.com");
    setField("type", EmailSender.EmailType.SMTP);

    // Inject mockMailSender manually since init() creates a new instance
    setPrivateField(emailSender, "mailSender", mockMailSender);

    when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = EmailSender.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(emailSender, value);
  }

  private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void testSendAsync_withSmtp_shouldSendEmail() throws Exception {
    emailSender.sendAsync("user@example.com", "Test Subject", "Text Body", "<p>HTML Body</p>");
    verify(mockMailSender).send(any(MimeMessage.class));
  }

  @Test
  void testSendAsync_withStub_shouldLogInsteadOfSending() throws Exception {
    setField("type", EmailSender.EmailType.STUB);
    emailSender.sendAsync("user@example.com", "Test Subject", "Text Body", "<p>HTML Body</p>");
    verify(mockMailSender, never()).send(any(MimeMessage.class));
  }

  @Test
  void testSendAlert_shouldSendEmailToAlertAddress() {
    emailSender.sendAlert("Test alert");
    verify(mockMailSender).send(any(MimeMessage.class));
  }

  @Test
  void testSendAlert_withException_shouldIncludeExceptionMessage() {
    Exception ex = new RuntimeException("Alert error");
    emailSender.sendAlert("Test alert", ex);
    verify(mockMailSender).send(any(MimeMessage.class));
  }

  @Test
  void testApplyTestOverride_shouldReplaceEmailWithOverride() throws Exception {
    Field method = EmailSender.class.getDeclaredField("testOverrideEmail");
    method.setAccessible(true);
    method.set(emailSender, "override@test.com");

    setField("type", EmailSender.EmailType.SMTP);

    emailSender.sendAsync("original@test.com", "Test Subject", "text", "html");
    verify(mockMailSender).send(any(MimeMessage.class));
  }

}
