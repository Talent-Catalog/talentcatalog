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
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EmailSendFailedException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Setter
@RequiredArgsConstructor
@Slf4j
public class EmailHelper {

    private final EmailSender emailSender;
    private final TemplateEngine textTemplateEngine;
    private final TemplateEngine htmlTemplateEngine;

    @Value("${web.portal}")
    private String portalUrl;
    @Value("${web.admin}")
    private String adminUrl;

    public void sendAlert(String alertMessage) {
        emailSender.sendAlert(alertMessage);
    }

    public void sendAlert(String alertMessage, @Nullable Exception ex) {
        emailSender.sendAlert(alertMessage, ex);
    }

    public void sendRegistrationEmail(User user) throws EmailSendFailedException {

        String email = user.getEmail();
        Partner partner = user.getPartner();
        String displayName = user.getDisplayName();

        String subject;
        String bodyText;
        String bodyHtml;
        try {
            final Context ctx = new Context();
            ctx.setVariable("partner", partner);
            ctx.setVariable("displayName", displayName);
            ctx.setVariable("username", user.getUsername());
            ctx.setVariable("loginUrl", portalUrl + "/candidate-portal/");
            ctx.setVariable("year", currentYear());

            subject = "Talent Catalog - Thank you for your registration";
            bodyText = textTemplateEngine.process("registration", ctx);
            bodyHtml = htmlTemplateEngine.process("registration", ctx);

            emailSender.sendAsync(email, subject, bodyText, bodyHtml);
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("RegistrationEmail")
                .message("error sending confirm registration email")
                .logError(e);

            throw new EmailSendFailedException(e);
        }
    }

    public void sendResetPasswordEmail(User user) throws EmailSendFailedException {

        String email = user.getEmail();
        String displayName = user.getDisplayName();
        String token = user.getResetToken();
        Partner partner = user.getPartner();

        String subject;
        String bodyText;
        String bodyHtml;
        try {
            final Context ctx = new Context();
            ctx.setVariable("partner", partner);
            ctx.setVariable("displayName", displayName);

            String resetUrl = user.getRole() == Role.user ? portalUrl : adminUrl;
            ctx.setVariable("resetUrl", resetUrl + "/reset-password/" + token);
            ctx.setVariable("year", currentYear());

            subject = "Talent Catalog - Reset Your Password";
            bodyText = textTemplateEngine.process("reset-password", ctx);
            bodyHtml = htmlTemplateEngine.process("reset-password", ctx);

            emailSender.sendAsync(email, subject, bodyText, bodyHtml);
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("ResetPasswordEmail")
                .message("error sending reset password email")
                .logError(e);

            throw new EmailSendFailedException(e);
        }
    }

    public void sendIncompleteApplication(User user, String message) throws EmailSendFailedException {

        String email = user.getEmail();
        String displayName = user.getDisplayName();
        Partner partner = user.getPartner();

        String subject;
        String bodyText;
        String bodyHtml;
        try {
            final Context ctx = new Context();
            ctx.setVariable("partner", partner);
            ctx.setVariable("displayName", displayName);
            ctx.setVariable("candidateMessage", message);
            ctx.setVariable("username", user.getUsername());
            ctx.setVariable("loginUrl", portalUrl + "/candidate-portal/");
            ctx.setVariable("year", currentYear());

            subject = "Talent Catalog - Please provide more registration details";
            bodyText = textTemplateEngine.process("incomplete-application", ctx);
            bodyHtml = htmlTemplateEngine.process("incomplete-application", ctx);

            emailSender.sendAsync(email, subject, bodyText, bodyHtml);
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("IncompleteApplicationEmail")
                .message("error sending incomplete application email")
                .logError(e);

            throw new EmailSendFailedException(e);
        }
    }

    public void sendNewChatPostsForUserEmail(
        User user, boolean isCandidateUser, @NonNull List<EmailNotificationLink> links) {

        String email = user.getEmail();
        Partner partner = user.getPartner();
        String displayName = user.getDisplayName();

        String emailTemplate;
        String subject;
        String bodyText;
        String bodyHtml;
        try {
            final Context ctx = new Context();
            ctx.setVariable("displayName", displayName);
            ctx.setVariable("username", user.getUsername());
            ctx.setVariable("links", links);

            if (isCandidateUser) {
                emailTemplate = "candidate-chat-notification";
                ctx.setVariable("loginUrl", portalUrl);
                ctx.setVariable("partner", partner);
            } else {
                emailTemplate = "admin-chat-notification";
                ctx.setVariable("loginUrl", adminUrl);
            }

            subject = "Talent Catalog - New chat posts";

            bodyText = textTemplateEngine.process(emailTemplate, ctx);
            bodyHtml = htmlTemplateEngine.process(emailTemplate, ctx);

            LogBuilder.builder(log)
                .action("ChatPostsEmail")
                .message("Sending email to " + email)
                .logInfo();

            LogBuilder.builder(log)
                .action("ChatPostsEmail")
                .message("Subject: " + subject)
                .logInfo();

            LogBuilder.builder(log)
                .action("ChatPostsEmail")
                .message("Text\n" + bodyText)
                .logInfo();

            LogBuilder.builder(log)
                .action("ChatPostsEmail")
                .message("Html\n" + bodyHtml)
                .logInfo();

            emailSender.sendAsync(email, subject, bodyText, bodyHtml);
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("ChatPostsEmail")
                .message("error sending chat notification email to " + email)
                .logError();

            throw new EmailSendFailedException(e);
        }
    }

    public void sendWatcherEmail(User user, List<EmailNotificationLink> links) {

        String email = user.getEmail();
        Partner partner = user.getPartner();
        String displayName = user.getDisplayName();

        String subject;
        String bodyText;
        String bodyHtml;
        try {
            final Context ctx = new Context();
            ctx.setVariable("partner", partner);
            ctx.setVariable("displayName", displayName);
            ctx.setVariable("links", links);

            subject = "Talent Catalog - New candidates matching your watched searches";
            bodyText = textTemplateEngine.process("watcher-notification", ctx);
            bodyHtml = htmlTemplateEngine.process("watcher-notification", ctx);

            LogBuilder.builder(log)
                .action("WatcherEmail")
                .message("Sending email to " + email)
                .logInfo();

            LogBuilder.builder(log)
                .action("WatcherEmail")
                .message("Subject: " + subject)
                .logInfo();

            LogBuilder.builder(log)
                .action("WatcherEmail")
                .message("Text\n" + bodyText)
                .logInfo();

            LogBuilder.builder(log)
                .action("WatcherEmail")
                .message("Html\n" + bodyHtml)
                .logInfo();

            emailSender.sendAsync(email, subject, bodyText, bodyHtml);

        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("WatcherEmail")
                .message("error sending watcher email")
                .logError(e);

            throw new EmailSendFailedException(e);
        }
    }


    private String currentYear() {
        return LocalDate.now().getYear() + "";
    }
}
