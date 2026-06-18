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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.tctalent.server.configuration.EmailConfiguration;
import org.tctalent.server.exception.EmailSendFailedException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.email.EmailSender.EmailType;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
/**
 * Not true unit tests. It just forces generated emails to log the sent emails for visual
 * checking.
 */
class EmailHelperTest {

    EmailHelper emailHelper;
    User testUser;

    @BeforeEach
    void setUp() {
        EmailConfiguration config = new EmailConfiguration();
        TemplateEngine textTemplateEngine = config.textTemplateEngine();
        TemplateEngine htmlTemplateEngine = config.htmlTemplateEngine();
        EmailSender emailSender = new EmailSender();
        emailSender.setType(EmailType.STUB);
        emailSender.setDefaultEmail("from.test@example.com");
        emailSender.init();
        emailHelper = new EmailHelper(emailSender, textTemplateEngine, htmlTemplateEngine);
        emailHelper.setAdminUrl("https://tctalent.org/admin-portal");
        emailHelper.setPortalUrl("https://tctalent.org/candidate-portal");

        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setUsername("testuser");
        testUser.setEmail("to.test@example.com");
    }

    @Test
    void testSendNewChatPostsForCandidateEmail() {
        List<EmailNotificationLink> links = new ArrayList<>();
        emailHelper.sendNewChatPostsForUserEmail(testUser, true, links);
    }

    @Test
    void testSendNewChatPostsForNonCandidateEmail() throws Exception {
        List<EmailNotificationLink> links = new ArrayList<>();
        links.add( new EmailNotificationLink(
            100, new URI("https://linktoobject.com").toURL(), "Name of object"));
        links.add( new EmailNotificationLink(
            200, new URI("https://linktoobject2.com").toURL(), "Name of object2"));

        emailHelper.sendNewChatPostsForUserEmail(testUser, false, links);
    }

    @Test
    void testSendWatcherEmail() throws Exception {
        List<EmailNotificationLink> links = new ArrayList<>();
        links.add( new EmailNotificationLink(
            54, new URI("https://tctalent.org/admin-portal/search/54").toURL(), "Search 54's name"));
        links.add( new EmailNotificationLink(
            123, new URI("https://tctalent.org/admin-portal/search/123").toURL(), "Search 123's name"));

        emailHelper.sendWatcherEmail(testUser, links);
    }

    @Test
    void testSendNewChatPostsWithEmptyLinks() {
        emailHelper.sendNewChatPostsForUserEmail(testUser, false, new ArrayList<>());
    }

    @Test
    void testSendNewChatPostsWithNullLinks() {
        emailHelper.sendNewChatPostsForUserEmail(testUser, false, null);
    }

    @Test
    void testSendNewChatPostsForCandidateWithLinks() throws Exception {
        List<EmailNotificationLink> links = List.of(
            new EmailNotificationLink(
                1, new URI("https://link.com/object1").toURL(), "Candidate Object 1"));
        emailHelper.sendNewChatPostsForUserEmail(testUser, true, links);
    }

    @Test
    void testSendWatcherEmailWithEmptyLinks() {
        emailHelper.sendWatcherEmail(testUser, new ArrayList<>());
    }

    @Test
    void testSendWatcherEmailWithNullLinks() {
        emailHelper.sendWatcherEmail(testUser, null);
    }

    @Test
    void testSendWatcherEmailWithLongNames() throws Exception {
        String longName = "Very Long Name ".repeat(20);
        List<EmailNotificationLink> links = List.of(
            new EmailNotificationLink(
                999, new URI("https://tctalent.org/very-long-name").toURL(), longName));
        emailHelper.sendWatcherEmail(testUser, links);
    }
    
    @Test
    void testSendNewChatPostsWithInvalidUrl() {
        List<EmailNotificationLink> links = List.of(
            new EmailNotificationLink(100, null, "Broken Link")
        );
        emailHelper.sendNewChatPostsForUserEmail(testUser, false, links);
    }
    @Test
    void testSendAlertDelegatesToEmailSender() {
        MockedEmailHelper mocked = mockedEmailHelper();

        mocked.emailHelper.sendAlert("Something happened");

        verify(mocked.emailSender).sendAlert("Something happened");
    }

    @Test
    void testSendAlertWithExceptionDelegatesToEmailSender() {
        MockedEmailHelper mocked = mockedEmailHelper();
        Exception exception = new RuntimeException("Boom");

        mocked.emailHelper.sendAlert("Something happened", exception);

        verify(mocked.emailSender).sendAlert("Something happened", exception);
    }

    @Test
    void testSendRegistrationEmailForSelfRegisteredCandidate() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);
        Candidate candidate = mock(Candidate.class);
        when(candidate.getUser()).thenReturn(user);

        mocked.emailHelper.sendRegistrationEmail(candidate);

        IContext context = captureTextContext(mocked, "registration");
        verifyHtmlTemplateProcessed(mocked, "registration");
        assertCommonCandidateContext(context);
        assertNull(context.getVariable("registeredBy"));
        assertEquals("https://tctalent.org/candidate-portal/candidate-portal/",
            context.getVariable("loginUrl"));
        verifyEmailSent(
            mocked,
            "to.test@example.com",
            "Talent Catalog - Thank you for your registration",
            "registration"
        );
    }

    @Test
    void testSendRegistrationEmailForPartnerRegisteredCandidate() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);
        Candidate candidate = mock(Candidate.class);
        PartnerImpl registeredBy = mock(PartnerImpl.class);
        when(candidate.getUser()).thenReturn(user);
        when(candidate.getRegisteredBy()).thenReturn(registeredBy);

        mocked.emailHelper.sendRegistrationEmail(candidate);

        IContext context = captureTextContext(mocked, "registration");
        assertEquals(registeredBy, context.getVariable("registeredBy"));
        verifyEmailSent(
            mocked,
            "to.test@example.com",
            "Talent Catalog - Thank you for your registration",
            "registration"
        );
    }

    @Test
    void testSendRegistrationEmailWrapsTemplateFailure() {
        MockedEmailHelper mocked = mockedEmailHelperWithFailingTextTemplate();
        Candidate candidate = mock(Candidate.class);
        when(candidate.getUser()).thenReturn(completeUser(Role.user));

        assertThrows(EmailSendFailedException.class,
            () -> mocked.emailHelper.sendRegistrationEmail(candidate));
    }

    @Test
    void testSendResetPasswordEmailUsesPortalUrlForCandidateUser() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);

        mocked.emailHelper.sendResetPasswordEmail(user);

        IContext context = captureTextContext(mocked, "reset-password");
        verifyHtmlTemplateProcessed(mocked, "reset-password");
        assertEquals("https://tctalent.org/candidate-portal/reset-password/reset-token",
            context.getVariable("resetUrl"));
        assertEquals(currentYear(), context.getVariable("year"));
        verifyEmailSent(
            mocked,
            "to.test@example.com",
            "Talent Catalog - Reset Your Password",
            "reset-password"
        );
    }

    @Test
    void testSendResetPasswordEmailUsesAdminUrlForAdminUser() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.admin);

        mocked.emailHelper.sendResetPasswordEmail(user);

        IContext context = captureTextContext(mocked, "reset-password");
        assertEquals("https://tctalent.org/admin-portal/reset-password/reset-token",
            context.getVariable("resetUrl"));
        verifyEmailSent(
            mocked,
            "to.test@example.com",
            "Talent Catalog - Reset Your Password",
            "reset-password"
        );
    }

    @Test
    void testSendResetPasswordEmailWrapsTemplateFailure() {
        MockedEmailHelper mocked = mockedEmailHelperWithFailingTextTemplate();

        assertThrows(EmailSendFailedException.class,
            () -> mocked.emailHelper.sendResetPasswordEmail(completeUser(Role.user)));
    }

    @Test
    void testSendVerificationEmailRejectsNullUser() {
        MockedEmailHelper mocked = mockedEmailHelper();

        assertThrows(IllegalArgumentException.class,
            () -> mocked.emailHelper.sendVerificationEmail(null));

        verifyNoInteractions(mocked.emailSender);
    }

    @Test
    void testSendVerificationEmailRejectsNullEmail() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);
        user.setEmail(null);

        assertThrows(IllegalArgumentException.class,
            () -> mocked.emailHelper.sendVerificationEmail(user));

        verifyNoInteractions(mocked.emailSender);
    }

    @Test
    void testSendVerificationEmailRejectsEmptyEmail() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);
        user.setEmail("");

        assertThrows(IllegalArgumentException.class,
            () -> mocked.emailHelper.sendVerificationEmail(user));

        verifyNoInteractions(mocked.emailSender);
    }

    @Test
    void testSendVerificationEmailRejectsNullToken() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);
        user.setEmailVerificationToken(null);

        assertThrows(IllegalStateException.class,
            () -> mocked.emailHelper.sendVerificationEmail(user));

        verifyNoInteractions(mocked.emailSender);
    }

    @Test
    void testSendVerificationEmailRejectsEmptyToken() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);
        user.setEmailVerificationToken("");

        assertThrows(IllegalStateException.class,
            () -> mocked.emailHelper.sendVerificationEmail(user));

        verifyNoInteractions(mocked.emailSender);
    }

    @Test
    void testSendVerificationEmailSendsEmail() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);

        mocked.emailHelper.sendVerificationEmail(user);

        IContext context = captureTextContext(mocked, "verify-email");
        verifyHtmlTemplateProcessed(mocked, "verify-email");
        assertEquals("John Doe", context.getVariable("displayName"));
        assertEquals("https://tctalent.org/api/admin/user/verify-email/verification-token",
            context.getVariable("verificationUrl"));
        assertEquals(currentYear(), context.getVariable("year"));
        verifyEmailSent(
            mocked,
            "to.test@example.com",
            "Talent Catalog - Verify Your Email",
            "verify-email"
        );
    }

    @Test
    void testSendVerificationEmailWrapsTemplateFailure() {
        MockedEmailHelper mocked = mockedEmailHelperWithFailingTextTemplate();

        assertThrows(EmailSendFailedException.class,
            () -> mocked.emailHelper.sendVerificationEmail(completeUser(Role.user)));
    }

    @Test
    void testSendCompleteVerificationEmailRejectsNullUser() {
        MockedEmailHelper mocked = mockedEmailHelper();

        assertThrows(IllegalArgumentException.class,
            () -> mocked.emailHelper.sendCompleteVerificationEmail(null, true));

        verifyNoInteractions(mocked.emailSender);
    }

    @Test
    void testSendCompleteVerificationEmailRejectsNullEmail() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);
        user.setEmail(null);

        assertThrows(IllegalArgumentException.class,
            () -> mocked.emailHelper.sendCompleteVerificationEmail(user, true));

        verifyNoInteractions(mocked.emailSender);
    }

    @Test
    void testSendCompleteVerificationEmailRejectsEmptyEmail() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);
        user.setEmail("");

        assertThrows(IllegalArgumentException.class,
            () -> mocked.emailHelper.sendCompleteVerificationEmail(user, true));

        verifyNoInteractions(mocked.emailSender);
    }

    @Test
    void testSendCompleteVerificationEmailSendsSuccessEmail() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);

        mocked.emailHelper.sendCompleteVerificationEmail(user, true);

        IContext context = captureTextContext(mocked, "verify-email-success");
        verifyHtmlTemplateProcessed(mocked, "verify-email-success");
        assertEquals("John Doe", context.getVariable("displayName"));
        verifyEmailSent(
            mocked,
            "to.test@example.com",
            "Email Verified Successfully",
            "verify-email-success"
        );
    }

    @Test
    void testSendCompleteVerificationEmailSendsFailureEmail() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);

        mocked.emailHelper.sendCompleteVerificationEmail(user, false);

        IContext context = captureTextContext(mocked, "verify-email-failure");
        verifyHtmlTemplateProcessed(mocked, "verify-email-failure");
        assertEquals("John Doe", context.getVariable("displayName"));
        verifyEmailSent(
            mocked,
            "to.test@example.com",
            "Email Verification Failed",
            "verify-email-failure"
        );
    }

    @Test
    void testSendCompleteVerificationEmailWrapsTemplateFailure() {
        MockedEmailHelper mocked = mockedEmailHelperWithFailingTextTemplate();

        assertThrows(EmailSendFailedException.class,
            () -> mocked.emailHelper.sendCompleteVerificationEmail(completeUser(Role.user), true));
    }

    @Test
    void testSendIncompleteApplicationEmail() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);

        mocked.emailHelper.sendIncompleteApplication(user, "Please add more details");

        IContext context = captureTextContext(mocked, "incomplete-application");
        verifyHtmlTemplateProcessed(mocked, "incomplete-application");
        assertCommonCandidateContext(context);
        assertEquals("Please add more details", context.getVariable("candidateMessage"));
        assertEquals("https://tctalent.org/candidate-portal/candidate-portal/",
            context.getVariable("loginUrl"));
        verifyEmailSent(
            mocked,
            "to.test@example.com",
            "Talent Catalog - Please provide more registration details",
            "incomplete-application"
        );
    }

    @Test
    void testSendIncompleteApplicationWrapsTemplateFailure() {
        MockedEmailHelper mocked = mockedEmailHelperWithFailingTextTemplate();

        assertThrows(EmailSendFailedException.class,
            () -> mocked.emailHelper.sendIncompleteApplication(
                completeUser(Role.user), "Please add more details"));
    }

    @Test
    void testSendNewChatPostsWrapsTemplateFailure() {
        MockedEmailHelper mocked = mockedEmailHelperWithFailingTextTemplate();

        assertThrows(EmailSendFailedException.class,
            () -> mocked.emailHelper.sendNewChatPostsForUserEmail(
                completeUser(Role.admin), false, new ArrayList<>()));
    }

    @Test
    void testSendWatcherEmailWrapsTemplateFailure() {
        MockedEmailHelper mocked = mockedEmailHelperWithFailingTextTemplate();

        assertThrows(EmailSendFailedException.class,
            () -> mocked.emailHelper.sendWatcherEmail(completeUser(Role.admin), new ArrayList<>()));
    }

    @Test
    void testSendTaskAssignedEmail() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);

        mocked.emailHelper.sendTaskAssignedEmail(user, "Upload passport");

        IContext context = captureTextContext(mocked, "task-assigned");
        verifyHtmlTemplateProcessed(mocked, "task-assigned");
        assertEquals("John Doe", context.getVariable("displayName"));
        assertEquals("testuser", context.getVariable("username"));
        assertEquals("Upload passport", context.getVariable("taskDisplayName"));
        assertEquals("https://tctalent.org/candidate-portal/candidate-portal/",
            context.getVariable("loginUrl"));
        assertEquals(currentYear(), context.getVariable("year"));
        verifyEmailSent(
            mocked,
            "to.test@example.com",
            "Talent Catalog - You have a new task",
            "task-assigned"
        );
    }

    @Test
    void testSendTaskAssignedEmailWrapsTemplateFailure() {
        MockedEmailHelper mocked = mockedEmailHelperWithFailingTextTemplate();

        assertThrows(EmailSendFailedException.class,
            () -> mocked.emailHelper.sendTaskAssignedEmail(
                completeUser(Role.user), "Upload passport"));
    }

    @Test
    void testSendDuolingoCouponEmail() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);

        mocked.emailHelper.sendDuolingoCouponEmail(user);

        IContext context = captureTextContext(mocked, "duolingo-coupon");
        verifyHtmlTemplateProcessed(mocked, "duolingo-coupon");
        assertEquals("John Doe", context.getVariable("displayName"));
        assertEquals("https://tctalent.org/candidate-portal/candidate-portal/",
            context.getVariable("loginUrl"));
        assertEquals(currentYear(), context.getVariable("year"));
        verifyEmailSent(
            mocked,
            "to.test@example.com",
            "Action Required: Take the Duolingo English Test",
            "duolingo-coupon"
        );
    }

    @Test
    void testSendDuolingoCouponEmailWrapsTemplateFailure() {
        MockedEmailHelper mocked = mockedEmailHelperWithFailingTextTemplate();

        assertThrows(EmailSendFailedException.class,
            () -> mocked.emailHelper.sendDuolingoCouponEmail(completeUser(Role.user)));
    }

    @Test
    void testSendCandidateHiredEmail() {
        MockedEmailHelper mocked = mockedEmailHelper();
        User user = completeUser(Role.user);
        Candidate candidate = mock(Candidate.class);
        when(candidate.getUser()).thenReturn(user);
        when(candidate.getCandidateNumber()).thenReturn("12345");

        mocked.emailHelper.sendCandidateHiredEmail(candidate);

        IContext context = captureTextContext(mocked, "job-accepted");
        verifyHtmlTemplateProcessed(mocked, "job-accepted");
        assertEquals("John Doe", context.getVariable("displayName"));
        assertEquals("12345", context.getVariable("candidateNumber"));

        verify(mocked.emailSender).sendAsync(
            eq("to.test@example.com"),
            eq("membership@pathwayclub.org"),
            eq("Talent Catalog - Congratulations and next steps"),
            eq("text:job-accepted"),
            eq("html:job-accepted")
        );
    }

    @Test
    void testSendCandidateHiredEmailWrapsTemplateFailure() {
        MockedEmailHelper mocked = mockedEmailHelperWithFailingTextTemplate();
        Candidate candidate = mock(Candidate.class);
        when(candidate.getUser()).thenReturn(completeUser(Role.user));

        assertThrows(EmailSendFailedException.class,
            () -> mocked.emailHelper.sendCandidateHiredEmail(candidate));
    }

    private MockedEmailHelper mockedEmailHelper() {
        EmailSender mockedEmailSender = mock(EmailSender.class);
        TemplateEngine mockedTextTemplateEngine = mock(TemplateEngine.class);
        TemplateEngine mockedHtmlTemplateEngine = mock(TemplateEngine.class);

        when(mockedTextTemplateEngine.process(anyString(), any(IContext.class)))
            .thenAnswer(invocation -> "text:" + invocation.getArgument(0));
        when(mockedHtmlTemplateEngine.process(anyString(), any(IContext.class)))
            .thenAnswer(invocation -> "html:" + invocation.getArgument(0));

        EmailHelper mockedEmailHelper = new EmailHelper(
            mockedEmailSender, mockedTextTemplateEngine, mockedHtmlTemplateEngine);
        mockedEmailHelper.setAdminUrl("https://tctalent.org/admin-portal");
        mockedEmailHelper.setPortalUrl("https://tctalent.org/candidate-portal");

        return new MockedEmailHelper(
            mockedEmailHelper,
            mockedEmailSender,
            mockedTextTemplateEngine,
            mockedHtmlTemplateEngine
        );
    }

    private MockedEmailHelper mockedEmailHelperWithFailingTextTemplate() {
        MockedEmailHelper mocked = mockedEmailHelper();
        when(mocked.textTemplateEngine.process(anyString(), any(IContext.class)))
            .thenThrow(new RuntimeException("Template failed"));
        return mocked;
    }

    private User completeUser(Role role) {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("testuser");
        user.setEmail("to.test@example.com");
        user.setRole(role);
        user.setResetToken("reset-token");
        user.setEmailVerificationToken("verification-token");
        return user;
    }

    private IContext captureTextContext(MockedEmailHelper mocked, String template) {
        ArgumentCaptor<IContext> contextCaptor = ArgumentCaptor.forClass(IContext.class);
        verify(mocked.textTemplateEngine).process(eq(template), contextCaptor.capture());
        return contextCaptor.getValue();
    }

    private void verifyHtmlTemplateProcessed(MockedEmailHelper mocked, String template) {
        verify(mocked.htmlTemplateEngine).process(eq(template), any(IContext.class));
    }

    private void verifyEmailSent(
        MockedEmailHelper mocked, String email, String subject, String template) {
        verify(mocked.emailSender).sendAsync(
            eq(email),
            eq(subject),
            eq("text:" + template),
            eq("html:" + template)
        );
    }

    private void assertCommonCandidateContext(IContext context) {
        assertEquals("John Doe", context.getVariable("displayName"));
        assertEquals("testuser", context.getVariable("username"));
        assertEquals(currentYear(), context.getVariable("year"));
    }

    private String currentYear() {
        return String.valueOf(LocalDate.now().getYear());
    }

    private static class MockedEmailHelper {
        private final EmailHelper emailHelper;
        private final EmailSender emailSender;
        private final TemplateEngine textTemplateEngine;
        private final TemplateEngine htmlTemplateEngine;

        private MockedEmailHelper(
            EmailHelper emailHelper,
            EmailSender emailSender,
            TemplateEngine textTemplateEngine,
            TemplateEngine htmlTemplateEngine) {
            this.emailHelper = emailHelper;
            this.emailSender = emailSender;
            this.textTemplateEngine = textTemplateEngine;
            this.htmlTemplateEngine = htmlTemplateEngine;
        }
    }
}
