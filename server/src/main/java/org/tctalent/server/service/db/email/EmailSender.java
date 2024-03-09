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

import java.io.IOException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Setter
@Service
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);
    private final String alertSubject = "Talent Catalog Alert";

    public enum EmailType {
        STUB, SMTP, MANDRILL
    }

    @Value("${email.type}")
    private EmailType type;
    @Value("${email.host}")
    private String host;
    @Value("${email.port}")
    private int port;
    @Value("${email.user}")
    private String user;
    @Value("${email.password}")
    private String password;
    @Value("${email.authenticated}")
    private Boolean authenticate;
    @Value("${email.alertEmail}")
    private String alertEmail;
    @Value("${email.defaultEmail}")
    private String defaultEmail;
    @Value("${email.testOverrideEmail}")
    private String testOverrideEmail;

    private final Properties properties = new Properties();
    private Session session;
    private Transport transport;

    @PostConstruct
    public void init() {
        if (type == EmailType.SMTP) {
            log.info("email configured to use SMTP with host " + host + " and port " + port);
        } else if (type == EmailType.STUB) {
            log.info("email configured to use STUB, emails will be printed to the log file");
        } else if (type == EmailType.MANDRILL) {
            log.info("email configured to use MANDRILL, emails will be sent via Mandrill");
        }

        this.session = emailSession();
        this.transport = emailTransport();
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

    public boolean send(String emailTo,
                        String bccTo,
                        String subject,
                        String contentText,
                        String contentHtml) {
        return doSend(emailTo, subject, contentText, contentHtml);
    }

    @Async
    public void sendAsync(String emailTo,
                             String subject,
                             String contentText,
                             String contentHtml) {
        doSend(emailTo, subject, contentText, contentHtml);
    }

    private boolean doSend(String emailTo,
                           String subject,
                           String contentText,
                           String contentHtml) {

        log.info("sending email to " + emailTo + " with subject '" + subject + "'");

        MimeMessage email = new MimeMessage(session);
        try {
            email.addFrom(InternetAddress.parse(defaultEmail));
            email.addRecipients(Message.RecipientType.TO, applyTestOverrideIfRequired(emailTo));
            email.setSubject(subject);
            email.setContent(createEmailBody(contentText, contentHtml));

            send(email);

            return true;
        } catch (MessagingException e) {
            log.error("Error sending email to " + emailTo, e);
        }

        return false;
    }

    public void send(MimeMessage mimeMessage) throws MessagingException {
        if (transport == null) {
            Transport.send(mimeMessage);
            return;
        }
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
    }

    @Async
    public void sendAsync(MimeMessage mimeMessage) throws MessagingException {
        if (transport == null) {
            Transport.send(mimeMessage);
            return;
        }
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
    }

    private static Multipart createEmailBody(String contentText,
                                             String contentHtml) throws MessagingException {
        MimeMultipart body = new MimeMultipart("alternative");

        BodyPart bodyPartText = new MimeBodyPart();
        bodyPartText.setText(contentText);
        body.addBodyPart(bodyPartText);

        BodyPart bodyPartHtml = new MimeBodyPart();
        bodyPartHtml.setContent(contentHtml, "text/html; charset=utf-8");
        body.addBodyPart(bodyPartHtml);
        return body;
    }

    private String applyTestOverrideIfRequired(String email) {
        if (StringUtils.isNotBlank(testOverrideEmail) && StringUtils.isNotBlank(email)) {
            return testOverrideEmail;
        }
        return email;
    }

    /*
     * @Bean
     *
     * @ConditionalOnProperty(name = "email.type", havingValue = "MANDRILL")
     * public MandrillApi mandrillApi() { return new MandrillApi(password); }
     */

    public Session emailSession() {
        if (type == EmailType.SMTP) {
            properties.put("mail.transport.protocol", "smtp");
            properties.put("mail.from", defaultEmail);
            properties.put("mail.smtp.port", port);
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.username", user);
            properties.put("mail.debug", "false");
            properties.put("mail.smtp.auth", authenticate != null ? authenticate.toString() : "false");
            properties.put("mail.smtp.starttls.enable", authenticate != null ? authenticate : false);
            if (authenticate != null && authenticate) {
                Authenticator authenticator = new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                };
                return Session.getDefaultInstance(properties, authenticator);
            } else {
                return Session.getDefaultInstance(properties);
            }
        } else if (type == EmailType.STUB) {
            return stubMailSession();
        }
        throw new RuntimeException("Email has not been correctly configured");
    }

    public Session stubMailSession() {
        Properties testMailProperties = new Properties();
        testMailProperties.setProperty("mail.testProtocol.class", "com.covata.util.mail.MailStubber$TestTransport");
        testMailProperties.setProperty("mail.transport.protocol.rfc822", "testProtocol");
        Session session = Session.getInstance(testMailProperties);
        Provider testMailProvider = new Provider(Provider.Type.TRANSPORT, "testProtocol",
                                                 "com.covata.util.mail.MailStubber$TestTransport", "", "");
        session.addProvider(testMailProvider);
        return session;
    }

    private Transport emailTransport() {
        if (type == EmailType.STUB) {
            return new StubTransport(stubMailSession(), new URLName("test"));
        }
        return null;
    }

    public static class StubTransport extends Transport {

        private static final Queue<String> MAIL = new LinkedBlockingQueue<>(20);

        public StubTransport(Session session,
                             URLName urlname) {
            super(session, urlname);
        }

        static String getMailMessage() {
            return MAIL.poll();
        }

        static void clearMail() {
            MAIL.clear();
        }

        @Override
        public void connect() {
        }

        @Override
        public void sendMessage(Message msg,
                                Address[] addresses)
                throws MessagingException {
            try {
                BodyPart part = ((Multipart) msg.getContent()).getBodyPart(0);
                if (MAIL.size() < 20) {
                    MAIL.add(part.getContent().toString());
                }
                log.info("Email received (sent to: " + addresses[0] + "): \n\n" + part.getContent());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
