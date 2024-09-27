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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EmailSendFailedException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
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

    public void sendNewChatPostsForCandidateUserEmail(User user, Set<JobChat> chats) {

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

            //TODO JC Currently not using the chats passed in. Could pull out job if any, otherwise
            //the source partner
            ctx.setVariable("chats", chats);
            ctx.setVariable("loginUrl", portalUrl);
            ctx.setVariable("username", user.getUsername());

            subject = "Talent Catalog - New chat posts";
            bodyText = textTemplateEngine.process("candidate-chat-notification", ctx);
            bodyHtml = htmlTemplateEngine.process("candidate-chat-notification", ctx);

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
                .message("error sending candidate chat notification email to " + email)
                .logError();

            throw new EmailSendFailedException(e);
        }
    }

    public void sendWatcherEmail(User user, Set<SavedSearch> savedSearches) {

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
            ctx.setVariable("searches", savedSearches);

            subject = "Talent Catalog - New candidates matching your watched searches";
            bodyText = textTemplateEngine.process("watcher-notification", ctx);
            bodyHtml = htmlTemplateEngine.process("watcher-notification", ctx);

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
