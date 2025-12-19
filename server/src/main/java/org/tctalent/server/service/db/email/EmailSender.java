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

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import javax.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.tctalent.server.logging.LogBuilder;

/**
 * Email sender built using Spring's standard mail support.
 *
 * @author John Cameron
 */
@Service
@Slf4j
public class EmailSender {

    private final String alertSubject = "Talent Catalog Alert";

    public enum EmailType {
        STUB, SMTP
    }

    //Exposed for testing
    @Setter
    @Value("${email.type}")
    private EmailSender.EmailType type;
    @Setter
    @Value("${email.defaultEmail}")
    private String defaultEmail;


    @Value("${email.host}")
    private String host;
    @Value("${email.port}")
    private int port;
    @Value("${email.user}")
    private String user;
    @Value("${email.password}")
    private String password;
    @Value("${email.authenticated}")
    private Boolean authenticated;
    @Value("${email.alertEmail}")
    private String alertEmail;
    @Value("${email.testOverrideEmail}")
    private String testOverrideEmail;

    private JavaMailSender mailSender;

    @PostConstruct
    public void init() {
        if (type == EmailSender.EmailType.SMTP) {
            LogBuilder.builder(log)
                .action("EmailSender")
                .message("Email configured to use SMTP with host " + host + " and port " + port)
                .logInfo();

        } else if (type == EmailSender.EmailType.STUB) {
            LogBuilder.builder(log)
                .action("EmailSender")
                .message("Email configured to use STUB, emails will be printed to the log file")
                .logInfo();

        }

        this.mailSender = getJavaMailSender();
    }

    private JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(user);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", authenticated != null ? authenticated.toString() : "false");
        props.put("mail.smtp.starttls.enable", authenticated != null ? authenticated.toString() : "false");
        props.put("mail.debug", "false");

        return mailSender;
    }

    public void sendAlert(String alertMessage) {
        sendAlert(alertMessage, null);
    }

    public void sendAlert(String alertMessage, @Nullable Exception ex) {
        String s = alertMessage;
        if (ex != null) {
            s += ": " + ex;
        }
        sendAsync(alertEmail, alertSubject, s, s);
    }

    @Async
    public void sendAsync(String emailTo,
        String subject,
        String contentText,
        String contentHtml) {
        doSend(emailTo, null, subject, contentText, contentHtml);
    }

    @Async
    public void sendAsync(String emailTo, String emailCc,
        String subject,
        String contentText,
        String contentHtml) {
        doSend(emailTo, emailCc, subject, contentText, contentHtml);
    }

    private void doSend(String emailTo,
        @Nullable
        String emailCc,
        String subject,
        String contentText,
        String contentHtml) {
        LogBuilder.builder(log)
            .action("Email")
            .message("Sending email to " + emailTo +
                (emailCc != null ? " cc " + emailCc : "") +
                " with subject '" + subject + "'")
            .logInfo();

        MimeMessage email = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(email, true);
            helper.setFrom(defaultEmail);
            helper.addTo(applyTestOverrideIfRequired(emailTo));
            if (emailCc != null) {
                helper.addCc(applyTestOverrideIfRequired(emailCc));
            }
            helper.setSubject(subject);
            helper.setText(contentText, contentHtml);

            if (type.equals(EmailType.SMTP)) {
                mailSender.send(email);
            } else {
                sendStub(contentText, contentHtml, emailTo);
            }
        } catch (MessagingException e) {
            LogBuilder.builder(log)
                .action("Email")
                .message("Error sending email to " + emailTo)
                .logError(e);
        }
    }

    private void sendStub(String text, String html, String to) throws MessagingException {
        LogBuilder.builder(log)
            .action("Email")
            .message("Email received html (sent to: " + to + "): \n\n" + html)
            .logInfo();
        LogBuilder.builder(log)
            .action("Email")
            .message("Email received text (sent to: " + to + "): \n\n" + text)
            .logInfo();
    }

    private String applyTestOverrideIfRequired(String email) {
        if (!ObjectUtils.isEmpty(testOverrideEmail) && !ObjectUtils.isEmpty(email)) {
            return testOverrideEmail;
        }
        return email;
    }
}
