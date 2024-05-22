/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import java.nio.charset.StandardCharsets;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.email.EmailSender.EmailType;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

public class EmailTester {

    public static void main(String[] args) {
//        testStubSend();
//        testSmtpSend();
    }

    private static void testStubSend() {
        EmailSender emailSender = stubEmailSender();
        EmailHelper helper = new EmailHelper(emailSender, textTemplateEngine(), htmlTemplateEngine());
        helper.sendResetPasswordEmail(user());
    }

    private static User user() {
        User user = new User();
        user.setEmail("test@dp.test.com");
        user.setFirstName("sadfs");
        user.setLastName("dsrfgdg");
        user.setResetToken("sdjfsljf sjflkjsdlkfjlsdkjflksdjf");
        return user;
    }

    private static EmailSender stubEmailSender() {
        EmailSender sender = new EmailSender();
        sender.setType(EmailType.STUB);
        sender.setHost("");
        sender.setPort(0);
        sender.setUser("");
        sender.setPassword("");
        sender.setAuthenticated(false);
        sender.setDefaultEmail("test-from@dp.test.com");
        sender.setTestOverrideEmail("");
        sender.init();
        return sender;
    }

    public static TemplateEngine textTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(textTemplateResolver());
        return templateEngine;
    }

    public static TemplateEngine htmlTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlTemplateResolver());
        return templateEngine;
    }

    private static ITemplateResolver textTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(2);
        templateResolver.setPrefix("mail/");
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    private static ITemplateResolver htmlTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(2);
        templateResolver.setPrefix("mail/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false);
        return templateResolver;
    }

}
