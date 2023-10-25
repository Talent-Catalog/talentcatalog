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

import java.time.LocalDate;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EmailSendFailedException;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailHelper {
    private static final Logger log = LoggerFactory.getLogger(EmailHelper.class);


    private final EmailSender emailSender;
    private final TemplateEngine textTemplateEngine;
    private final TemplateEngine htmlTemplateEngine;

    @Value("${web.portal}")
    private String portalUrl;

    @Autowired
    public EmailHelper(EmailSender emailSender,
                       TemplateEngine textTemplateEngine,
                       TemplateEngine htmlTemplateEngine) {
        this.emailSender = emailSender;
        this.textTemplateEngine = textTemplateEngine;
        this.htmlTemplateEngine = htmlTemplateEngine;
    }

    public void sendAlert(String alertMessage) {
        emailSender.sendAlert(alertMessage);
    }

    public void sendAlert(String alertMessage, @Nullable Exception ex) {
        emailSender.sendAlert(alertMessage, ex);
    }

    public void sendRegistrationEmail(User user) throws EmailSendFailedException {

        String email = user.getEmail();
        String displayName = user.getDisplayName();

        String subject = null;
        String bodyText = null;
        String bodyHtml = null;
        try {
            final Context ctx = new Context();
            ctx.setVariable("displayName", displayName);
            ctx.setVariable("username", user.getUsername());
            ctx.setVariable("forgotPwdUrl", portalUrl + "/reset-password/");
            ctx.setVariable("year", currentYear());

            subject = "Talent Beyond Boundaries - Thank you for your application";
            bodyText = textTemplateEngine.process("registration", ctx);
            bodyHtml = htmlTemplateEngine.process("registration", ctx);

            emailSender.sendAsync(email, subject, bodyText, bodyHtml);
        } catch (Exception e) {
            log.error("error sending confirm registration email", e);
            throw new EmailSendFailedException(e);
        }
    }

    public void sendResetPasswordEmail(User user) throws EmailSendFailedException {

        String email = user.getEmail();
        String displayName = user.getDisplayName();
        String token = user.getResetToken();

        String subject = null;
        String bodyText = null;
        String bodyHtml = null;
        try {
            final Context ctx = new Context();
            ctx.setVariable("displayName", displayName);
            ctx.setVariable("resetUrl", portalUrl + "/reset-password/" + token);
            ctx.setVariable("year", currentYear());

            subject = "Talent Beyond Boundaries - Reset Your Password";
            bodyText = textTemplateEngine.process("reset-password", ctx);
            bodyHtml = htmlTemplateEngine.process("reset-password", ctx);

            emailSender.sendAsync(email, subject, bodyText, bodyHtml);
        } catch (Exception e) {
            log.error("error sending reset password email", e);
            throw new EmailSendFailedException(e);
        }
    }

    public void sendIncompleteApplication(User user, String message) throws EmailSendFailedException {

        String email = user.getEmail();
        String displayName = user.getDisplayName();

        String subject = null;
        String bodyText = null;
        String bodyHtml = null;
        try {
            final Context ctx = new Context();
            ctx.setVariable("displayName", displayName);
            ctx.setVariable("candidateMessage", message);
            ctx.setVariable("username", user.getUsername());
            ctx.setVariable("forgotPwdUrl", portalUrl + "/reset-password/");
            ctx.setVariable("year", currentYear());

            subject = "Talent Beyond Boundaries - Please provide more details application";
            bodyText = textTemplateEngine.process("incomplete-application", ctx);
            bodyHtml = htmlTemplateEngine.process("incomplete-application", ctx);

            emailSender.sendAsync(email, subject, bodyText, bodyHtml);
        } catch (Exception e) {
            log.error("error sending confirm registration email", e);
            throw new EmailSendFailedException(e);
        }
    }

    public void sendWatcherEmail(User user, Set<SavedSearch> savedSearches) {

        String email = user.getEmail();
        String displayName = user.getDisplayName();

        String subject = null;
        String bodyText = null;
        String bodyHtml = null;
        try {
            final Context ctx = new Context();
            ctx.setVariable("displayName", displayName);
            ctx.setVariable("searches", savedSearches);

            subject = "TBB - New candidates matching your watched searches";
            bodyText = textTemplateEngine.process("watcher-notification", ctx);
            bodyHtml = htmlTemplateEngine.process("watcher-notification", ctx);

            emailSender.sendAsync(email, subject, bodyText, bodyHtml);
        } catch (Exception e) {
            log.error("error sending watcher email", e);
            throw new EmailSendFailedException(e);
        }
    }


    private String currentYear() {
        return LocalDate.now().getYear() + "";
    }
}
