/*
 * Copyright (c) 2024 Talent Catalog.
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.configuration.EmailConfiguration;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.email.EmailSender.EmailType;
import org.thymeleaf.TemplateEngine;

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
    void testSendNewChatPostsWithNullUser() {
        emailHelper.sendNewChatPostsForUserEmail(null, false, new ArrayList<>());
    }

    @Test
    void testSendWatcherEmailWithNullUser() {
        emailHelper.sendWatcherEmail(null, new ArrayList<>());
    }

    @Test
    void testSendNewChatPostsWithInvalidUrl() {
        List<EmailNotificationLink> links = List.of(
            new EmailNotificationLink(100, null, "Broken Link")
        );
        emailHelper.sendNewChatPostsForUserEmail(testUser, false, links);
    }

}
