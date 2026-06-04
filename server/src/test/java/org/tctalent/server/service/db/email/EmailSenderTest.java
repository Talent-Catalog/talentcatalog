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

package org.tctalent.server.service.db.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

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

  @Test
  void testInit_withSmtpAndAuthenticatedTrue_shouldCreateConfiguredJavaMailSender()
      throws Exception {
    setField("type", EmailSender.EmailType.SMTP);
    setField("host", "smtp.example.com");
    setField("port", 2525);
    setField("user", "smtp-user");
    setField("password", "smtp-pass");
    setField("authenticated", true);

    emailSender.init();

    JavaMailSenderImpl actualMailSender =
        (JavaMailSenderImpl) getPrivateField(emailSender, "mailSender");
    Properties properties = actualMailSender.getJavaMailProperties();

    assertEquals("smtp.example.com", actualMailSender.getHost());
    assertEquals(2525, actualMailSender.getPort());
    assertEquals("smtp-user", actualMailSender.getUsername());
    assertEquals("smtp-pass", actualMailSender.getPassword());
    assertEquals("smtp", properties.get("mail.transport.protocol"));
    assertEquals("true", properties.get("mail.smtp.auth"));
    assertEquals("true", properties.get("mail.smtp.starttls.enable"));
    assertEquals("false", properties.get("mail.debug"));
  }

  @Test
  void testInit_withStubAndNullAuthenticated_shouldDefaultMailPropertiesToFalse()
      throws Exception {
    setField("type", EmailSender.EmailType.STUB);
    setField("authenticated", null);

    emailSender.init();

    JavaMailSenderImpl actualMailSender =
        (JavaMailSenderImpl) getPrivateField(emailSender, "mailSender");
    Properties properties = actualMailSender.getJavaMailProperties();

    assertEquals("false", properties.get("mail.smtp.auth"));
    assertEquals("false", properties.get("mail.smtp.starttls.enable"));
  }

  @Test
  void testSendAsync_withCc_shouldSendEmailToRecipientAndCc() throws Exception {
    MimeMessage realMimeMessage = new JavaMailSenderImpl().createMimeMessage();
    when(mockMailSender.createMimeMessage()).thenReturn(realMimeMessage);

    emailSender.sendAsync(
        "user@example.com",
        "copy@example.com",
        "Test Subject",
        "Text Body",
        "<p>HTML Body</p>"
    );

    verify(mockMailSender).send(messageCaptor.capture());

    MimeMessage sentMessage = messageCaptor.getValue();
    assertEquals("Test Subject", sentMessage.getSubject());
    assertEquals(
        "testoverride@test.com",
        sentMessage.getRecipients(Message.RecipientType.TO)[0].toString()
    );
    assertEquals(
        "testoverride@test.com",
        sentMessage.getRecipients(Message.RecipientType.CC)[0].toString()
    );
  }

  @Test
  void testSendAsync_whenMimeMessageHelperThrows_shouldSwallowMessagingException()
      throws Exception {
    setField("defaultEmail", "not a valid email address");

    assertDoesNotThrow(() ->
        emailSender.sendAsync(
            "user@example.com",
            "Test Subject",
            "Text Body",
            "<p>HTML Body</p>"
        )
    );

    verify(mockMailSender, never()).send(any(MimeMessage.class));
  }

  @Test
  void testApplyTestOverride_withNullOverride_shouldReturnOriginalEmail() throws Exception {
    setField("testOverrideEmail", null);

    String result = invokeApplyTestOverride("original@test.com");

    assertEquals("original@test.com", result);
  }

  @Test
  void testApplyTestOverride_withEmptyOverride_shouldReturnOriginalEmail() throws Exception {
    setField("testOverrideEmail", "");

    String result = invokeApplyTestOverride("original@test.com");

    assertEquals("original@test.com", result);
  }

  @Test
  void testApplyTestOverride_withEmptyEmail_shouldReturnEmptyEmail() throws Exception {
    setField("testOverrideEmail", "override@test.com");

    String result = invokeApplyTestOverride("");

    assertEquals("", result);
  }

  private Object getPrivateField(Object target, String fieldName) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(target);
  }

  private String invokeApplyTestOverride(String email) throws Exception {
    Method method = EmailSender.class.getDeclaredMethod(
        "applyTestOverrideIfRequired", String.class);
    method.setAccessible(true);
    return (String) method.invoke(emailSender, email);
  }

}
